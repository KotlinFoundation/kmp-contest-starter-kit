package com.kotlinfoundation.koko.util

import com.mmk.kmpstorage.http.SimpleHttpStorageProvider
import com.mmk.kmpstorage.http.SimpleHttpUploadResponse
import io.ktor.client.call.body
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * KMPStorage provider backed by [tempfile.org](https://tempfile.org) — an anonymous file host that
 * needs **no API key**. A thin [SimpleHttpStorageProvider] config; the app hosts the reference image
 * here so the AI provider can fetch it by URL, with zero configuration or friction.
 *
 * Upload returns a viewer URL (`https://tempfile.org/{id}/`); the direct-download link a remote
 * fetcher needs is that URL plus `download`. Files auto-expire (default host retention), which is
 * fine — the URL only has to live long enough for the AI provider to pull the image.
 */
fun tempFileStorageProvider(): SimpleHttpStorageProvider {
    val config = SimpleHttpStorageProvider.Config(
        uploadFileKey = "files",
        uploadUrl = "https://tempfile.org/api/upload/local",
        uploadResponseParser = { response ->
            val uploadResponse = response.body<TempFileUploadResponse>()
            val viewerUrl = uploadResponse.files.firstOrNull()?.url
                ?: throw Exception(
                    "tempfile.org upload failed: ${uploadResponse.message ?: "no file in response"}",
                )
            object : SimpleHttpUploadResponse {
                override val downloadUrl: String = "${viewerUrl.trimEnd('/')}/download"
            }
        },
    )
    return SimpleHttpStorageProvider(config = config)
}

@Serializable
data class TempFileUploadResponse(
    @SerialName("success") val success: Boolean = false,
    @SerialName("files") val files: List<TempFileData> = emptyList(),
    @SerialName("message") val message: String? = null,
)

@Serializable
data class TempFileData(
    @SerialName("id") val id: String,
    @SerialName("url") val url: String,
    @SerialName("name") val name: String? = null,
    @SerialName("size") val size: Long? = null,
    @SerialName("expiryTime") val expiryTime: Long? = null,
)
