package com.kotlinfoundation.koko.data.source.remote.response.ai.openai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenAiCreateImageResponse(
    @SerialName("created") val created: Long?,
    @SerialName("data") val data: List<ImageData>?,
) {
    @Serializable
    data class ImageData(
        @SerialName("url") val url: String?,
        @SerialName("b64_json") val b64Json: String? = null, // If the response format is b64_json
    )
}
