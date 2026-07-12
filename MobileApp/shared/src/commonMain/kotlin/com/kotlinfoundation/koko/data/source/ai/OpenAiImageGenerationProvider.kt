package com.kotlinfoundation.koko.data.source.ai

import com.kotlinfoundation.koko.data.source.remote.apiservices.ai.OpenAiApiService
import com.kotlinfoundation.koko.data.source.remote.request.ai.openai.OpenAiCreateImageRequest
import com.kotlinfoundation.koko.domain.model.generation.GenerationFile
import com.kotlinfoundation.koko.domain.model.generation.GenerationInput
import com.kotlinfoundation.koko.domain.model.generation.GenerationOutput
import com.kotlinfoundation.koko.domain.model.generation.typedValue
import com.kotlinfoundation.koko.domain.usecase.AiGenerationProvider
import com.kotlinfoundation.koko.util.file.FileManager

/**
 * [AiGenerationProvider] backed by OpenAI image generation (gpt-image-1). Alternative to
 * [ReplicateGenerationProvider] — swap the DI binding in `root/Di.kt` (dataModule) to use it.
 */
class OpenAiImageGenerationProvider(
    private val openAiApiService: OpenAiApiService,
    private val fileManager: FileManager,
) : AiGenerationProvider {
    companion object {
        private const val OPENAI_IMAGE_MODEL = "gpt-image-1"
        private const val DEFAULT_IMAGE_QUALITY = "low"
        private const val DEFAULT_IMAGE_SIZE = "auto"
        private const val DEFAULT_IMAGE_OUTPUT_FORMAT = "png"
        const val OPENAI_IMAGE_PARAM_QUALITY = "openai_image_param_quality"
        const val OPENAI_IMAGE_PARAM_SIZE = "openai_image_param_size"
        const val OPENAI_IMAGE_PARAM_STYLE = "openai_image_param_style"
        const val OPENAI_IMAGE_PARAM_RESPONSE_FORMAT = "openai_image_param_response_format"
        const val OPENAI_IMAGE_PARAM_OUTPUT_FORMAT = "openai_image_param_output_format"
    }

    override suspend fun generate(input: GenerationInput): Result<GenerationOutput> {
        val quality =
            (input.params[OPENAI_IMAGE_PARAM_QUALITY].typedValue<String>(DEFAULT_IMAGE_QUALITY))
        val size = (input.params[OPENAI_IMAGE_PARAM_SIZE].typedValue<String>(DEFAULT_IMAGE_SIZE))
        val style = (input.params[OPENAI_IMAGE_PARAM_STYLE].typedValue<String?>(null))
        val responseFormat =
            (input.params[OPENAI_IMAGE_PARAM_RESPONSE_FORMAT].typedValue<String?>(null))
        val outputFormat = (
            input.params[OPENAI_IMAGE_PARAM_OUTPUT_FORMAT].typedValue<String>(
                DEFAULT_IMAGE_OUTPUT_FORMAT,
            )
            )

        val openAiImageResponse = openAiApiService.createImage(
            requestBody = OpenAiCreateImageRequest(
                prompt = input.fullPrompt(),
                n = 1,
                quality = quality,
                size = size,
                style = style,
                responseFormat = responseFormat,
                model = OPENAI_IMAGE_MODEL,
                outputFormat = outputFormat,
            ),
        )

        return openAiImageResponse.handleAsResult { response ->
            val base64Image = response?.data?.firstOrNull()?.b64Json
                ?: return@handleAsResult Result.failure(Exception("Image generation failed"))

            val outputFileName =
                fileManager.saveBase64ImageToInternalDirectory(
                    base64Image = base64Image,
                    imageExtension = outputFormat,
                )
                    ?: return@handleAsResult Result.failure(Exception("Image generation failed. Could not save image to file."))

            val generationOutput = GenerationOutput(
                input = input,
                outputFile = GenerationFile(
                    fileNameWithExtension = outputFileName,
                    type = GenerationFile.Type.IMAGE,
                ),
                status = GenerationOutput.Status.COMPLETED,
            )

            Result.success(generationOutput)
        }
    }
}
