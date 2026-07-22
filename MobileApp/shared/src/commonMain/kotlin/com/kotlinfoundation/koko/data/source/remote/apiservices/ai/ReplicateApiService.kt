package com.kotlinfoundation.koko.data.source.remote.apiservices.ai

import com.kotlinfoundation.koko.common.BuildConfig
import com.kotlinfoundation.koko.data.source.remote.request.ai.replicate.ReplicatePredictionRequest
import com.kotlinfoundation.koko.data.source.remote.response.ai.AiApiBaseResponse
import com.kotlinfoundation.koko.data.source.remote.response.ai.replicate.ReplicatePredictionResponse
import com.kotlinfoundation.koko.root.AppConfiguration
import com.kotlinfoundation.koko.util.Platform
import com.kotlinfoundation.koko.util.getPlatform
import io.ktor.http.HttpMethod

/**
 * A service class for interacting with the Replicate API.
 * You can check different models on Replicate from [here](https://replicate.com/explore).
 *
 */
class ReplicateApiService(val aiTransport: AiTransport) {

    // Web can't call api.replicate.com directly (no CORS); it goes through the same-origin dev-server
    // proxy (see webApp/build.gradle.kts). Everyone else hits the provider directly.
    @PublishedApi
    internal val baseUrl: String
        get() = if (getPlatform() == Platform.Web) "http://localhost:8080/replicate" else "https://api.replicate.com"

    @PublishedApi
    internal fun directSpec(url: String) = AiDirectSpec(
        url = url,
        apiKey = BuildConfig.REPLICATE_API_KEY,
        extraHeaders = mapOf("Prefer" to "wait"),
    )

    /**
     * Creates a prediction request for a specific model using the Replicate API.
     *
     * This function sends a request to create a model prediction. If the model's documentation
     * mentions "models" in the URL, such as
     * `https://api.replicate.com/v1/models/black-forest-labs/flux-schnell/predictions`,
     * it means that the request is for a model prediction.
     *
     * Example usage:
     * ```
     *
     * data class FluxInput(val prompt:String, val output_format:String, val num_outputs:Int)
     *
     * val response = replicateApiService.createModelPrediction(
     *     ReplicatePredictionRequest(
     *         modelOwner = "black-forest-labs",
     *         modelName = "flux-1.1-pro",
     *         input = FluxInput(
     *             prompt = "Creative, abstract logo design for a startup company",
     *             output_format = "png",
     *             num_outputs = 1
     *         )
     *     )
     * )
     * ```
     *
     * @param modelOwner The owner of the model on Replicate (e.g., `"black-forest-labs"`).
     * @param modelName The name of the model (e.g., `"flux-1.1-pro""`).
     * @param requestBody The body of the request,
     * @return The response from the Replicate API as an [AiApiBaseResponse] containing [ReplicatePredictionResponse].
     */
    suspend fun createModelPrediction(
        modelOwner: String,
        modelName: String,
        requestBody: ReplicatePredictionRequest,
    ): AiApiBaseResponse<ReplicatePredictionResponse> = aiTransport.execute(
        method = HttpMethod.Post,
        proxyUrl = "${AppConfiguration.CLOUD_FUNCTIONS_URL}/replicateCreateModelPrediction",
        direct = directSpec("$baseUrl/v1/models/$modelOwner/$modelName/predictions"),
        proxyQueryParams = mapOf("model_owner" to modelOwner, "model_name" to modelName),
        body = requestBody,
    )

    /**
     * Creates a prediction request for non-official models, using the Replicate API, that has version
     *
     * Example usage:
     * ```
     *
     * data class TextInput(val text:String)
     *
     * val response = replicateApiService.createPrediction(
     *     ReplicatePredictionRequest(
     *         version = "5c7d5dc6dd8bf75c1acaa8565735e7986bc5b66206b55cca93cb72c9bf15ccaa",
     *         input = TextInput("KMPStarterKit")
     *     )
     * )
     * ```
     *
     * @param requestBody The body of the request, containing the model version and input parameters.
     * @return The response from the Replicate API as an [AiApiBaseResponse] containing [ReplicatePredictionResponse].
     */
    suspend fun createPrediction(requestBody: ReplicatePredictionRequest): AiApiBaseResponse<ReplicatePredictionResponse> = aiTransport.execute(
        method = HttpMethod.Post,
        proxyUrl = "${AppConfiguration.CLOUD_FUNCTIONS_URL}/replicateCreatePrediction",
        direct = directSpec("$baseUrl/v1/predictions"),
        body = requestBody,
    )

    /**
     * Retrieves the status of an existing prediction using the Replicate API.
     *
     * @param id The unique identifier of the prediction.
     * @return The response from the Replicate API as an [AiApiBaseResponse] containing [ReplicatePredictionResponse].
     *
     * For input type you can just pass `Unit` if you are not interested in input
     * Example usage:
     * ```
     * val response = replicateApiService.getPredictionStatus<Unit>(
     *     id = "9egch6zt35rga0cm2bk813gz3c"
     * )
     * ```
     */
    suspend fun getPredictionStatus(id: String): AiApiBaseResponse<ReplicatePredictionResponse> = aiTransport.execute(
        method = HttpMethod.Get,
        proxyUrl = "${AppConfiguration.CLOUD_FUNCTIONS_URL}/replicateGetPredictionStatus",
        direct = directSpec("$baseUrl/v1/predictions/$id"),
        proxyQueryParams = mapOf("id" to id),
    )

    /**
     * Cancels existing prediction
     *
     * @param id The unique identifier of the prediction.
     * @return The response from the Replicate API as an [AiApiBaseResponse] containing [ReplicatePredictionResponse].
     *
     * For input type you can just pass `Unit` if you are not interested in input
     * Example usage:
     * ```
     * val response = replicateApiService.getPredictionStatus<Unit>(
     *     id = "9egch6zt35rga0cm2bk813gz3c"
     * )
     * ```
     */
    suspend fun cancelPrediction(id: String): AiApiBaseResponse<ReplicatePredictionResponse> = aiTransport.execute(
        method = HttpMethod.Post,
        proxyUrl = "${AppConfiguration.CLOUD_FUNCTIONS_URL}/replicateCancelPrediction",
        direct = directSpec("$baseUrl/v1/predictions/$id/cancel"),
        proxyQueryParams = mapOf("id" to id),
    )
}
