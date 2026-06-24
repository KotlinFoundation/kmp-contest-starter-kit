package com.kotlinfoundation.kmpstarterkit.data.source.remote

import com.kotlinfoundation.kmpstarterkit.auth.api.AuthServiceProvider
import com.kotlinfoundation.kmpstarterkit.util.logging.AppLogger
import io.ktor.client.HttpClient
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
    fun default(authServiceProvider: AuthServiceProvider) = HttpClient {
        defaultRequest {
            url("BASE_URL") // TODO replace with your API base URL
            header(HttpHeaders.ContentType, "application/json")
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60000 // Total request timeout: 60 seconds
            connectTimeoutMillis = 10000 // Connection establishment timeout: 10 seconds
            socketTimeoutMillis = 60000 // Inactivity timeout: 60 seconds
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
    }.also {
        it.plugin(HttpSend).intercept { request ->
            // For all requests you can send user token here, for example
            val userToken = authServiceProvider.getCurrentUserToken(forceRefresh = true)
            request.header("Authorization", "Bearer $userToken")
            execute(request)
        }
    }
}
