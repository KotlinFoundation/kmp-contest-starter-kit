package com.kotlinfoundation.koko.data.source.remote.response.file

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response from tempfile.org's upload endpoint. */
@Serializable
data class TemporaryFileUploadResponse(
    @SerialName("files") val files: List<TemporaryFileResponse> = emptyList(),
    @SerialName("message") val message: String? = null,
) {
    /**
     * The direct-download URL a remote fetcher (e.g. an AI provider) can pull the file from. The API
     * returns a viewer URL (`…/{id}/`); the direct link is that URL + `download`. Null if the upload
     * returned no file.
     */
    fun asDownloadUrl(): String? = files.firstOrNull()?.url?.let { "${it.trimEnd('/')}/download" }
}

@Serializable
data class TemporaryFileResponse(
    @SerialName("url") val url: String,
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("size") val size: Long? = null,
    @SerialName("expiryTime") val expiryTime: Long? = null,
)
