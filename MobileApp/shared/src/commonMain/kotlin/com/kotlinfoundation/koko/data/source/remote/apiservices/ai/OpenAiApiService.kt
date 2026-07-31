package com.kotlinfoundation.koko.data.source.remote.apiservices.ai

import com.kotlinfoundation.koko.common.BuildConfig
import com.kotlinfoundation.koko.data.source.remote.request.ai.openai.OpenAiCreateChatRequest
import com.kotlinfoundation.koko.data.source.remote.request.ai.openai.OpenAiCreateChatRequestBuilder
import com.kotlinfoundation.koko.data.source.remote.request.ai.openai.OpenAiCreateImageRequest
import com.kotlinfoundation.koko.data.source.remote.response.ai.AiApiBaseResponse
import com.kotlinfoundation.koko.data.source.remote.response.ai.openai.OpenAiCreateChatResponse
import com.kotlinfoundation.koko.data.source.remote.response.ai.openai.OpenAiCreateImageResponse
import com.kotlinfoundation.koko.root.AppConfiguration
import com.kotlinfoundation.koko.util.Platform
import com.kotlinfoundation.koko.util.getPlatform
import io.ktor.http.HttpMethod

class OpenAiApiService(private val aiTransport: AiTransport) {

    // Web can't call api.openai.com directly (no CORS); it goes through the same-origin dev-server proxy
    // (see webApp/build.gradle.kts). Everyone else hits the provider directly.
    private val baseUrl: String
        get() = if (getPlatform() == Platform.Web) "http://localhost:8080/openai" else "https://api.openai.com"

    private fun directSpec(url: String) = AiDirectSpec(url = url, apiKey = BuildConfig.OPENAI_API_KEY)

    /**
     * Creates a chat completion using the OpenAI API.
     *
     * @param builder A builder function to configure the chat request.
     * @return The response from the OpenAI API as an [AiApiBaseResponse] containing [OpenAiCreateChatResponse].
     *
     * Example:
     * ```
     * val response = createChat {
     *     model = "gpt-4o"
     *     messages {
     *         assistantText("Act as an KMP expert")
     *         userContentItems {
     *             text("What is KMPStarterKit?")
     *             image("https://kappmaker.com/images/logo-light.png")
     *             text("What do you see in the image, is it a logo of KMPStarterKit?")
     *         }
     *     }
     * }
     * ```
     */
    suspend fun createChat(builder: OpenAiCreateChatRequestBuilder.() -> Unit): AiApiBaseResponse<OpenAiCreateChatResponse> = createChat(OpenAiCreateChatRequest(builder))

    /**
     * Creates a chat completion using the OpenAI API with a request body.
     *
     * @param requestBody The request data for the chat completion.
     * @return The response from the OpenAI API as an [AiApiBaseResponse] containing [OpenAiCreateChatResponse].
     */
    suspend fun createChat(requestBody: OpenAiCreateChatRequest): AiApiBaseResponse<OpenAiCreateChatResponse> = aiTransport.execute(
        method = HttpMethod.Post,
        proxyUrl = "${AppConfiguration.CLOUD_FUNCTIONS_URL}/openAiCreateTextCompletion",
        direct = directSpec("$baseUrl/v1/chat/completions"),
        body = requestBody,
    )

    /**
     * Creates an image using the OpenAI API with DALL-E.
     *
     * @param requestBody of [OpenAiCreateImageRequest] The request data for image generation.
     * @return The response from the OpenAI API as an [AiApiBaseResponse] containing [OpenAiCreateImageResponse].
     */
    suspend fun createImage(requestBody: OpenAiCreateImageRequest): AiApiBaseResponse<OpenAiCreateImageResponse> = aiTransport.execute(
        method = HttpMethod.Post,
        proxyUrl = "${AppConfiguration.CLOUD_FUNCTIONS_URL}/openAiCreateImage",
        direct = directSpec("$baseUrl/v1/images/generations"),
        body = requestBody,
    )
}
