package com.kotlinfoundation.koko.data.source.remote.request.ai.openai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Creates an example OpenAI chat request using the [OpenAiCreateChatRequest] builder.
 *
 * This example demonstrates how to create a request for a chat completion with specified
 * `model` and `messages`. It includes a mix of assistant and user content, such as text and images.
 *
 * Example usage:
 * ```
 * val request = OpenAiCreateChatRequest {
 *     model = "gpt-4o" // Replace with the desired model, such as "o1-mini", "gpt-4o-mini", or others.
 *     messages {
 *         assistantText("Act as an UI/UX expert")
 *         userContentItems {
 *             text("What primary colors are used in the logo of kappmaker.com?")
 *             image("https://kappmaker.com/images/logo-light.png")
 *         }
 *     }
 * }
 * ```
 */

@Serializable
data class OpenAiCreateChatRequest(
    @SerialName("model") val model: String, // gpt-4o, gpt-4o-mini, gpt-4, o1, o1-mini or other models,
    @SerialName("messages") val messages: List<Message>,
    @SerialName("temperature") val temperature: Double? = null,
    @SerialName("top_p") val topP: Double? = null,
    @SerialName("n") val n: Int? = null,
    @SerialName("stream") val stream: Boolean? = null,
    @SerialName("stop") val stop: String? = null,
    @SerialName("max_completion_tokens") val maxCompletionTokens: Int? = null,
    @SerialName("response_format") val responseFormat: ResponseFormat? = ResponseFormat.Text,
) {

    @Serializable
    data class Message(
        @SerialName("role") val role: String = "user", // user or developer
        @SerialName("content") val content: List<ContentItem>,
    ) {

        @Serializable
        sealed class ContentItem {
            @Serializable
            @SerialName("text")
            data class Text(@SerialName("text") val text: String) : ContentItem()

            @Serializable
            @SerialName("image_url")
            data class Image(@SerialName("image_url") val imageUrl: Data) : ContentItem() {
                @Serializable
                data class Data(
                    @SerialName("url") val url: String,
                    @SerialName("detail") val detail: String = "auto", // auto, low, high
                )
            }
        }
    }

    @Serializable
    sealed class ResponseFormat {
        @Serializable
        @SerialName("text")
        data object Text : ResponseFormat()

        @Serializable
        @SerialName("json_object")
        data object JsonObject : ResponseFormat()
    }
}

fun OpenAiCreateChatRequest(
    block: OpenAiCreateChatRequestBuilder.() -> Unit,
): OpenAiCreateChatRequest {
    val builder = OpenAiCreateChatRequestBuilder()
    builder.block()
    return builder.build()
}

class OpenAiCreateChatRequestBuilder {
    var model: String = ""
    private val messages = mutableListOf<OpenAiCreateChatRequest.Message>()

    var temperature: Double? = null
    var topP: Double? = null
    var n: Int? = null
    var stream: Boolean? = null
    var stop: String? = null
    var maxCompletionTokens: Int? = null
    var responseFormat: OpenAiCreateChatRequest.ResponseFormat =
        OpenAiCreateChatRequest.ResponseFormat.Text

    fun messages(block: MessagesBuilder.() -> Unit) {
        val builder = MessagesBuilder()
        builder.block()
        messages.addAll(builder.build())
    }

    fun build(): OpenAiCreateChatRequest {
        if (model.isEmpty()) {
            throw IllegalStateException(
                "Model is required for OpenAI API and must be set. " +
                    "Example models: gpt-4o, gpt-4o-mini, gpt-4, o1, o1-mini or other models",
            )
        }
        return OpenAiCreateChatRequest(
            model = model,
            messages = messages,
            temperature = temperature,
            topP = topP,
            n = n,
            stream = stream,
            stop = stop,
            maxCompletionTokens = maxCompletionTokens,
            responseFormat = responseFormat,
        )
    }

    class MessagesBuilder {
        private val messages = mutableListOf<OpenAiCreateChatRequest.Message>()

        fun assistantText(text: String) {
            val builder = ContentItemsBuilder()
            builder.text(text)
            messages.add(
                OpenAiCreateChatRequest.Message(
                    role = "developer",
                    content = builder.build(),
                ),
            )
        }

        fun userContentItems(block: ContentItemsBuilder.() -> Unit) {
            val builder = ContentItemsBuilder()
            builder.block()
            messages.add(OpenAiCreateChatRequest.Message(role = "user", content = builder.build()))
        }

        fun build(): List<OpenAiCreateChatRequest.Message> = messages
    }

    class ContentItemsBuilder {
        private val contentItems = mutableListOf<OpenAiCreateChatRequest.Message.ContentItem>()

        fun text(text: String) {
            contentItems.add(OpenAiCreateChatRequest.Message.ContentItem.Text(text))
        }

        fun image(url: String, detail: String = "auto") {
            contentItems.add(
                OpenAiCreateChatRequest.Message.ContentItem.Image(
                    OpenAiCreateChatRequest.Message.ContentItem.Image.Data(
                        url,
                        detail,
                    ),
                ),
            )
        }

        fun build(): List<OpenAiCreateChatRequest.Message.ContentItem> = contentItems
    }
}
