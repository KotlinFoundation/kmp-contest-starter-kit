package com.kotlinfoundation.koko.data.source.remote.apiservices.ai

import com.kotlinfoundation.koko.data.source.remote.response.ai.AiApiBaseResponse
import com.kotlinfoundation.koko.root.AppConfiguration
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

enum class AiMode { PROXY, DIRECT }

/**
 * Everything the direct (no-proxy) path needs for one provider call: the provider [url] and the on-device
 * [apiKey]. The key both drives auto-detection (direct only when it's set) and becomes the
 * `Authorization: Bearer <key>` header — the transport builds that, so callers pass the key once. Add
 * provider extras like Replicate's `Prefer: wait` via [extraHeaders].
 */
class AiDirectSpec(
    val url: String,
    val apiKey: String,
    val extraHeaders: Map<String, String> = emptyMap(),
)

/**
 * Routes an AI HTTP call to either the Firebase Cloud Functions **proxy** (production — key in Secret
 * Manager, never on device) or **directly** to the provider API (prototyping — key from
 * `local.properties`, no Firebase). Both API services ([OpenAiApiService], [ReplicateApiService]) call
 * [execute].
 *
 * The transport is deliberately **provider-agnostic** — like the proxy, adding a new provider needs no
 * change here: the calling service supplies its own [AiDirectSpec] (provider url + key + headers). The
 * transport only decides the mode and adapts the response.
 *
 * The trick that keeps DTOs + generation providers + [AiApiBaseResponse.handleAsResult] untouched: in
 * PROXY mode the HTTP body already IS the `{statusCode, errorMessage, data}` envelope; in DIRECT mode it
 * is the raw provider object — which is exactly what `data` holds — so we parse the body as `T` and wrap
 * it in a synthetic [AiApiBaseResponse]. Same `T`, same downstream code.
 *
 * DIRECT is auto-selected whenever [AppConfiguration.CLOUD_FUNCTIONS_URL] is blank (the proxy is useless
 * without a URL); [AppConfiguration.USE_AI_PROXY_SERVER] overrides. Prototyping only — the key ships in the binary.
 */
class AiTransport(
    @PublishedApi internal val proxyClient: HttpClient, // carries the Firebase bearer-token interceptor
    @PublishedApi internal val directClient: HttpClient, // no interceptor; provider auth set per-request
) {

    /**
     * One generic call behind every AI endpoint.
     *
     * @param proxyUrl full Cloud Functions URL (proxy mode).
     * @param direct the provider URL + key + headers for the direct path (see [AiDirectSpec]).
     * @param proxyQueryParams query params sent in proxy mode only (direct URLs carry owner/name/id in the path).
     */
    suspend inline fun <reified T> execute(
        method: HttpMethod,
        proxyUrl: String,
        direct: AiDirectSpec,
        proxyQueryParams: Map<String, String> = emptyMap(),
        body: Any? = null,
    ): AiApiBaseResponse<T> {
        val mode = resolveMode()
        if (mode == AiMode.DIRECT && direct.apiKey.isBlank()) {
            throw Exception("AI provider API key is missing. Set OPENAI_API_KEY / REPLICATE_API_KEY in local.properties, or configure the Cloud Functions proxy (CLOUD_FUNCTIONS_URL).")
        }
        val response = rawExecute(mode, method, proxyUrl, direct, proxyQueryParams, body)
        return when (mode) {
            AiMode.PROXY -> response.body()

            // body already is AiApiBaseResponse<T>
            AiMode.DIRECT ->
                if (response.status.isSuccess()) {
                    AiApiBaseResponse(statusCode = response.status.value, data = response.body<T>())
                } else {
                    AiApiBaseResponse(
                        statusCode = response.status.value,
                        errorMessage = extractDirectError(response.bodyAsText()),
                    )
                }
        }
    }

    @PublishedApi
    internal suspend fun rawExecute(
        mode: AiMode,
        method: HttpMethod,
        proxyUrl: String,
        direct: AiDirectSpec,
        proxyQueryParams: Map<String, String>,
        body: Any?,
    ): HttpResponse {
        val client = if (mode == AiMode.PROXY) proxyClient else directClient
        val url = if (mode == AiMode.PROXY) proxyUrl else direct.url
        return client.request(url) {
            this.method = method
            if (mode == AiMode.PROXY) proxyQueryParams.forEach { (k, v) -> parameter(k, v) }
            if (mode == AiMode.DIRECT) {
                header(HttpHeaders.Authorization, "Bearer ${direct.apiKey}")
                direct.extraHeaders.forEach { (k, v) -> header(k, v) }
            }
            if (body != null && method != HttpMethod.Get) setBody(body)
        }
    }

    @PublishedApi
    internal fun resolveMode(): AiMode {
        AppConfiguration.USE_AI_PROXY_SERVER?.let { return if (it) AiMode.PROXY else AiMode.DIRECT }
        // Auto: the proxy only works with a proxy URL, so use it only when one is set; otherwise direct.
        return if (AppConfiguration.CLOUD_FUNCTIONS_URL.isBlank()) AiMode.DIRECT else AiMode.PROXY
    }

    /**
     * Best-effort human message from a NON-2xx provider error body. OpenAI uses `{"error":{"message":…}}`;
     * some errors use a top-level `error`/`detail` string. Falls back to the raw body if none match.
     *
     * Note: a Replicate *prediction failure* is NOT an HTTP error — it returns HTTP 2xx with
     * `{"status":"failed","error":"…"}` in the prediction object, so it's read from
     * `ReplicatePredictionResponse.error`/`status`, not here.
     */
    @PublishedApi
    internal fun extractDirectError(rawBody: String): String = runCatching {
        val obj = Json.parseToJsonElement(rawBody) as? JsonObject ?: return@runCatching rawBody
        (obj["error"] as? JsonObject)?.get("message")?.jsonPrimitive?.contentOrNull // OpenAI {error:{message}}
            ?: (obj["error"] as? JsonPrimitive)?.contentOrNull // top-level error string
            ?: (obj["detail"] as? JsonPrimitive)?.contentOrNull // some 4xx bodies
            ?: rawBody
    }.getOrDefault(rawBody)
}
