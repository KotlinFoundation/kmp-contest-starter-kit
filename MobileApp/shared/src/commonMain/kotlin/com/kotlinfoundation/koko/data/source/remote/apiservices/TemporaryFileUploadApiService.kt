package com.kotlinfoundation.koko.data.source.remote.apiservices

import com.kotlinfoundation.koko.data.source.remote.response.file.TemporaryFileUploadResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

/**
 * Uploads a file to [tempfile.org](https://tempfile.org) — an anonymous host that needs **no API
 * key** — and returns the raw [TemporaryFileUploadResponse] (repositories map it via
 * [TemporaryFileUploadResponse.asDownloadUrl] and wrap in Result). A plain multipart POST; swap the
 * host by changing [UPLOAD_URL] + the response DTO.
 *
 * Takes the multipart-safe client from `HttpClientFactory.fileUpload()` (no default json content type).
 */
class TemporaryFileUploadApiService(private val httpClient: HttpClient) {
    suspend fun upload(
        bytes: ByteArray,
        fileNameWithExtension: String,
        contentType: String,
    ): TemporaryFileUploadResponse = httpClient.submitFormWithBinaryData(
        url = UPLOAD_URL,
        formData = formData {
            append(
                key = "files",
                value = bytes,
                headers = Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileNameWithExtension\"")
                    append(HttpHeaders.ContentType, contentType)
                },
            )
        },
    ).body()

    private companion object {
        const val UPLOAD_URL = "https://tempfile.org/api/upload/local"
    }
}
