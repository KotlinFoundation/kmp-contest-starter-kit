package com.kotlinfoundation.koko.data.source.remote

import com.kotlinfoundation.koko.util.logging.AppLogger
import com.mmk.kmpauth.core.KMPAuth
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Builds the app's shared Ktor [HttpClient] — JSON, timeouts, logging, and a bearer-token interceptor. */
object HttpClientFactory {
    /** The app/proxy client: attaches the Firebase ID token as a bearer to every request. */
    fun default() = jsonClient().also {
        it.plugin(HttpSend).intercept { request ->
            val userToken = KMPAuth.currentUserIdToken(forceRefresh = true).getOrNull()
            request.header("Authorization", "Bearer $userToken")
            execute(request)
        }
    }

    /**
     * Like [default] but WITHOUT the Firebase bearer interceptor — for DIRECT provider calls
     * (OpenAI/Replicate), where the `Authorization` header (the on-device API key) is set per request
     * by [com.kotlinfoundation.koko.data.source.remote.apiservices.ai.AiTransport].
     */
    fun noAuth() = jsonClient()

    /**
     * Client for multipart file uploads (e.g. hosting the reference image). Same logging/JSON as the
     * others but WITHOUT the default `application/json` content type — a multipart request sets its own,
     * and a stray default header corrupts the body — and a longer timeout, since image uploads can be slow.
     */
    fun fileUpload() = HttpClient { installCommonPlugins(timeoutMillis = 5 * 60000) }

    private fun jsonClient() = HttpClient {
        defaultRequest {
            url("BASE_URL") // TODO replace with your API base URL (AI calls pass absolute URLs)
            header(HttpHeaders.ContentType, "application/json")
        }
        installCommonPlugins()
    }

    private fun HttpClientConfig<*>.installCommonPlugins(timeoutMillis: Long = 60000) {
        install(HttpTimeout) {
            requestTimeoutMillis = timeoutMillis // Total request timeout
            connectTimeoutMillis = 10000 // Connection establishment timeout: 10 seconds
            socketTimeoutMillis = timeoutMillis // Inactivity timeout
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    AppLogger.d("NetworkRequest: $message")
                }
            }
            level = LogLevel.ALL
        }

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    explicitNulls = false
                },
            )
        }
    }
}
