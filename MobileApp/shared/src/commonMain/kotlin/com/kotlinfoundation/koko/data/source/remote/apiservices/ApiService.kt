package com.kotlinfoundation.koko.data.source.remote.apiservices

import com.kotlinfoundation.koko.data.source.remote.request.ExampleRequest
import com.kotlinfoundation.koko.data.source.remote.response.ExampleResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

/** Example API service showing the convention: take the shared [HttpClient], return raw DTOs (repositories wrap in Result + map to domain). */
class ApiService(private val httpClient: HttpClient) {

    suspend fun getExampleData(): ExampleResponse = httpClient.post("/example") {
        setBody(ExampleRequest())
    }.body()
}
