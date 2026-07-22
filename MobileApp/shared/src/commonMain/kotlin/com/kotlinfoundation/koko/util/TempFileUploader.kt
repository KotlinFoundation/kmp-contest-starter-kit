package com.kotlinfoundation.koko.util

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Uploads a file to [tempfile.org](https://tempfile.org) — an anonymous host that needs **no API
 * key** — and returns a direct-download URL an AI provider can fetch. A plain multipart POST; no
 * storage-abstraction library. Swap the host by changing [UPLOAD_URL] + the response parsing.
 */
class TempFileUploader(
    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    },
) {
    suspend fun upload(bytes: ByteArray, fileNameWithExtension: String, contentType: String): String {
        val response = httpClient.submitFormWithBinaryData(
            url = UPLOAD_URL,
            formData = formData {
                append(
                    key = "files",
                    value = bytes,
                    headers = io.ktor.http.Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileNameWithExtension\"")
                        append(HttpHeaders.ContentType, contentType)
                    },
                )
            },
        )
        val viewerUrl = response.body<TempFileUploadResponse>().files.firstOrNull()?.url
            ?: throw Exception("tempfile.org upload returned no file")
        // The response gives a viewer URL (…/{id}/); the direct-download link is that URL + "download".
        return "${viewerUrl.trimEnd('/')}/download"
    }

    private companion object {
        const val UPLOAD_URL = "https://tempfile.org/api/upload/local"
    }
}

@Serializable
private data class TempFileUploadResponse(
    @SerialName("files") val files: List<TempFileData> = emptyList(),
    @SerialName("message") val message: String? = null,
)

@Serializable
private data class TempFileData(
    @SerialName("url") val url: String,
)
