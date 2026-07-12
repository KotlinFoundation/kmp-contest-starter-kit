package com.kotlinfoundation.koko.data.source.remote.response.ai.openai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenAiCreateChatResponse(
    @SerialName("id") val id: String?,
    @SerialName("created") val created: Long?,
    @SerialName("model") val model: String?, // gpt-4o, gpt-4o-mini, gpt-4, o1, o1-mini or other models,
    @SerialName("choices") val choices: List<Choice>?,
    @SerialName("usage") val usage: Usage?,
    @SerialName("system_fingerprint") val systemFingerprint: String? = null,
) {

    val firstResponseMessageOrNull get() = choices?.firstOrNull()?.message?.content

    @Serializable
    data class Choice(
        @SerialName("index") val index: Int?,
        @SerialName("message") val message: Message?,
        @SerialName("finish_reason") val finishReason: String?,
    )

    @Serializable
    data class Usage(
        @SerialName("completion_tokens") val completionTokens: Int?,
        @SerialName("prompt_tokens") val promptTokens: Int?,
        @SerialName("total_tokens") val totalTokens: Int?,
    )

    @Serializable
    data class Message(
        @SerialName("role") val role: String?,
        @SerialName("content") val content: String?,
        @SerialName("refusal") val refusal: String? = null,
    )
}
