package com.kotlinfoundation.koko.data.source.ai

import com.kotlinfoundation.koko.data.source.remote.apiservices.ai.ReplicateApiService
import com.kotlinfoundation.koko.data.source.remote.request.ai.replicate.ReplicatePredictionRequest
import com.kotlinfoundation.koko.data.source.remote.response.ai.AiApiBaseResponse
import com.kotlinfoundation.koko.data.source.remote.response.ai.replicate.ReplicatePredictionResponse
import com.kotlinfoundation.koko.domain.model.generation.GenerationFile
import com.kotlinfoundation.koko.domain.model.generation.GenerationInput
import com.kotlinfoundation.koko.domain.model.generation.GenerationOutput
import com.kotlinfoundation.koko.domain.model.generation.GenerationParam
import com.kotlinfoundation.koko.domain.model.generation.typedValue
import com.kotlinfoundation.koko.domain.usecase.AiGenerationProvider
import com.kotlinfoundation.koko.util.file.FileManager
import com.kotlinfoundation.koko.util.logging.AppLogger
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.Duration.Companion.seconds

/**
 * [AiGenerationProvider] backed by Replicate. Maps a [GenerationInput] to the model's request
 * body, runs the prediction, downloads the output file locally, and returns a [GenerationOutput].
 * The default provider wired in DI. Configure the model via the companion constants.
 */
class ReplicateGenerationProvider(
    private val replicateApiService: ReplicateApiService,
    private val fileManager: FileManager,
) : AiGenerationProvider {

    private companion object {
        const val IS_OFFICIAL_MODEL = true
        const val PROMPT_KEY_PARAM = "prompt"

        // Swapping the model? Changing the constants below is NOT enough — first read
        // "Swapping the Replicate model" in skills/integrate-web-proxy/SKILL.md and check the new
        // model's schema at https://replicate.com/<owner>/<name>/api. In particular: (1) input field
        // keys must match the model's schema exactly, and (2) `ReplicatePredictionResponse.output`
        // is typed String? for this default model — many models return an ARRAY of URIs instead.

        // The request is sent with `Prefer: wait`, which Replicate honours for at most ~60s. A model
        // that needs longer (cold start, heavy image-to-image, video) comes back HTTP 200 with
        // status "starting"/"processing" and a null output, which means "poll me", not "failed" —
        // `awaitOutput()` below does that. Tune these if your model routinely outruns the budget.
        val POLL_INITIAL_DELAY = 5.seconds
        val POLL_MAX_INTERVAL = 15.seconds
        const val POLL_MAX_ATTEMPTS = 20

        // TODO needs to be changed if needed
        const val MODEL_OWNER = "google"

        // TODO needs to be changed if needed
        const val MODEL_NAME = "nano-banana"

        // TODO needs to be changed if needed for community (non-official) models
        val MODEL_VERSION: String? = null
    }

    override suspend fun generate(input: GenerationInput): Result<GenerationOutput> {
        val userInputParam = input.params[PROMPT_KEY_PARAM]
        val updatedGenerationInput = input.copy(userInput = userInputParam?.value ?: "")
        val requestBody = updatedGenerationInput.toReplicateRequestBody()

        val replicateResponse = when {
            IS_OFFICIAL_MODEL -> {
                createOfficialModelsPrediction(
                    modelOwner = MODEL_OWNER,
                    modelName = MODEL_NAME,
                    input = requestBody,
                )
            }

            else -> createCommunityModelsPrediction(version = MODEL_VERSION, input = requestBody)
        }.awaitOutput()

        return replicateResponse.handleAsResult { response ->
            val output = response?.outputUrl()
                ?: return@handleAsResult Result.failure(Exception(response.failureReason()))

            val outputFileName =
                fileManager.downloadFileFromNetworkToInternalDirectory(
                    url = output,
                    fileExtension = null,
                ).getOrNull()
                    ?: return@handleAsResult Result.failure(Exception("Could not save file."))

            val outputFileType = when {
                outputFileName.startsWith("IMG") -> GenerationFile.Type.IMAGE

                outputFileName.startsWith("VID") -> GenerationFile.Type.VIDEO

                //                outputFileName.startsWith("AUD") -> GenerationFile.Type.AUDIO
                else -> GenerationFile.Type.FILE
            }

            val generationOutput = GenerationOutput(
                input = updatedGenerationInput,
                outputFile = GenerationFile(
                    fileNameWithExtension = outputFileName,
                    type = outputFileType,
                ),
                status = GenerationOutput.Status.COMPLETED,
            )

            Result.success(generationOutput)
        }
    }

    /**
     * Polls `getPredictionStatus` until the prediction leaves the starting/processing state, then
     * returns the final response. Returns [this] untouched when there is nothing to wait for — the
     * call failed, carries no id, or already finished within the `Prefer: wait` window. If the
     * budget runs out the last in-progress response is returned, and [failureReason] reports it as
     * a timeout rather than a generic failure.
     */
    private suspend fun AiApiBaseResponse<ReplicatePredictionResponse>.awaitOutput(): AiApiBaseResponse<ReplicatePredictionResponse> {
        val predictionId = data?.id
        if (!isSuccessful || predictionId == null || data?.isAwaitingOutput() != true) return this

        delay(POLL_INITIAL_DELAY)

        var response = this
        repeat(POLL_MAX_ATTEMPTS) { attempt ->
            response = replicateApiService.getPredictionStatus(predictionId)
            val prediction = response.data
            if (!response.isSuccessful || prediction?.isAwaitingOutput() != true) return response

            AppLogger.d(
                "Replicate prediction $predictionId is ${prediction.status} " +
                    "(poll ${attempt + 1}/$POLL_MAX_ATTEMPTS)",
            )
            delay(minOf(POLL_MAX_INTERVAL, (1L shl attempt).seconds))
        }
        return response
    }

    /** True while Replicate is still working and has produced no output yet. */
    private fun ReplicatePredictionResponse.isAwaitingOutput(): Boolean = isInProgress && outputUrl() == null

    /**
     * The output URL, or null when absent. Replicate occasionally serialises a missing output as the
     * literal string `"null"`, which is not a URL — treat it as absent.
     */
    private fun ReplicatePredictionResponse.outputUrl(): String? = output?.takeIf { it.isNotBlank() && it != "null" }

    /** Why no output came back, kept specific enough to diagnose without reading Replicate's logs. */
    private fun ReplicatePredictionResponse?.failureReason(): String = when {
        this == null -> "AI generation failed: no prediction returned"
        isFailed -> "AI generation failed: ${error ?: "no error detail"}"
        isCanceled -> "AI generation was canceled"
        isInProgress -> "AI generation timed out: still $status after $POLL_MAX_ATTEMPTS polls"
        else -> "AI generation returned no output (status=$status, error=${error ?: "none"})"
    }

    fun GenerationInput.toReplicateRequestBody(): JsonObject {
        val requestBody = mutableMapOf<String, JsonElement>()

        requestBody[PROMPT_KEY_PARAM] = JsonPrimitive(fullPrompt())

        // Add other params
        params.forEach { (key, param) ->
            if (key == PROMPT_KEY_PARAM) return@forEach
            when (param.type) {
                GenerationParam.ParamType.STRING -> requestBody[key] =
                    JsonPrimitive(param.typedValue<String?>(null))

                GenerationParam.ParamType.INTEGER -> requestBody[key] =
                    JsonPrimitive(param.typedValue<Int?>(null))

                GenerationParam.ParamType.FLOAT -> requestBody[key] =
                    JsonPrimitive(param.typedValue<Double?>(null))

                GenerationParam.ParamType.BOOLEAN -> requestBody[key] =
                    JsonPrimitive(param.typedValue<Boolean?>(null))

                GenerationParam.ParamType.FILE -> {
                    val files = param.typedValue<List<GenerationFile>>(emptyList())
                    val fileUrls = files.map { it.uploadedFileUrl }

                    if (canAcceptMultipleFiles || files.size > 1) {
                        requestBody[key] = JsonArray(fileUrls.map { JsonPrimitive(it) })
                    } else {
                        requestBody[key] = JsonPrimitive(fileUrls.firstOrNull())
                    }
                }
            }
        }

        return JsonObject(requestBody)
    }

    /**
     * Creates a prediction request for a specific official models using the Replicate API.
     * @param modelOwner The owner of the model on Replicate (e.g., `"google"`).
     * @param modelName The name of the model (e.g., `"nano-banana""`).
     *
     */
    private suspend fun createOfficialModelsPrediction(
        modelOwner: String,
        modelName: String,
        input: JsonObject,
    ) = replicateApiService.createModelPrediction(
        modelOwner = modelOwner,
        modelName = modelName,
        requestBody = ReplicatePredictionRequest(
            version = null,
            input = input,
        ),
    )

    /**
     * Creates a prediction request for a specific community models using the Replicate API.
     * @param version The version of the model to use.
     */
    private suspend fun createCommunityModelsPrediction(
        version: String?,
        input: JsonObject,
    ) = replicateApiService.createPrediction(
        requestBody = ReplicatePredictionRequest(
            version = version,
            input = input,
        ),
    )
}
