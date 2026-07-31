package com.kotlinfoundation.koko.data.source.remote.request.ai.openai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Creates an image request using DALL-E API.
 * @param prompt The text prompt for image generation.
 * @param model The model to use for image generation. Default is "dall-e-2", but can be "dall-e-3".
 */
@Serializable
data class OpenAiCreateImageRequest(
    @SerialName("prompt") val prompt: String,
    @SerialName("model") val model: String = "dall-e-2", // dall-e-2 or dall-e-3
    @SerialName("n") val n: Int? = 1, // Number of images to generate. Must be between 1 and 10. For dall-e-3, only n=1 is supported.
    @SerialName("size") val size: String? = "1024x1024", // Image size to generate.
    @SerialName("quality") val quality: String = "standard", // standard or hd
    @SerialName("style") val style: String? = "vivid", // vivid or natural
    @SerialName("response_format") val responseFormat: String? = "url", // url or b64_json
    @SerialName("output_format") val outputFormat: String? = null, // png, jpeg, or webp. Supported only for gpt-image-1
)
