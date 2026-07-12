package com.kotlinfoundation.koko.data.source.remote.apiservices.ai

import com.kotlinfoundation.koko.data.source.remote.request.ai.openai.OpenAiCreateChatRequest
import com.kotlinfoundation.koko.data.source.remote.request.ai.openai.OpenAiCreateChatRequestBuilder
import com.kotlinfoundation.koko.data.source.remote.request.ai.openai.OpenAiCreateImageRequest
import com.kotlinfoundation.koko.data.source.remote.response.ai.AiApiBaseResponse
import com.kotlinfoundation.koko.data.source.remote.response.ai.openai.OpenAiCreateChatResponse
import com.kotlinfoundation.koko.data.source.remote.response.ai.openai.OpenAiCreateImageResponse
import com.kotlinfoundation.koko.util.Constants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class OpenAiApiService(private val httpClient: HttpClient) {

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
    suspend fun createChat(requestBody: OpenAiCreateChatRequest): AiApiBaseResponse<OpenAiCreateChatResponse> = httpClient.post("${Constants.CLOUD_FUNCTIONS_URL}/openAiCreateTextCompletion") {
        setBody(requestBody)
    }.body()

    /**
     * Creates an image using the OpenAI API with DALL-E.
     *
     * @param requestBody of [OpenAiCreateImageRequest] The request data for image generation.
     * @return The response from the OpenAI API as an [AiApiBaseResponse] containing [OpenAiCreateImageResponse].
     */
    suspend fun createImage(requestBody: OpenAiCreateImageRequest): AiApiBaseResponse<OpenAiCreateImageResponse> = httpClient.post("${Constants.CLOUD_FUNCTIONS_URL}/openAiCreateImage") {
        setBody(requestBody)
    }.body()
}
