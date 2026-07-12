package com.kotlinfoundation.koko.data.source.remote.apiservices

import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

/**
 * Demonstrates testing an API service with Ktor's [MockEngine] — no server, no
 * network. The mock engine intercepts the request, lets the test assert on the
 * outgoing method/path/body, and returns a canned response that exercises the
 * service's deserialization.
 */
class ApiServiceTest {

    private fun apiService(engine: MockEngine): ApiService {
        val client = HttpClient(engine) {
            defaultRequest {
                url("https://api.test.local")
                header(HttpHeaders.ContentType, "application/json")
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
        return ApiService(client)
    }

    @Test
    fun `getExampleData posts to the example endpoint and parses the response`() = runTest {
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("/example", request.url.encodedPath)
            respond(
                content = "{}",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val response = apiService(engine).getExampleData()

        assertNotNull(response)
        assertEquals(1, engine.requestHistory.size)
    }

    @Test
    fun `non-json error response surfaces as an exception for the repository to wrap`() = runTest {
        val engine = MockEngine {
            respondError(HttpStatusCode.InternalServerError, content = "boom")
        }

        // Services return raw data and let repositories (BackgroundExecutor) wrap
        // failures into Result — so at this layer the failure is an exception.
        assertFailsWith<NoTransformationFoundException> {
            apiService(engine).getExampleData()
        }
    }
}
