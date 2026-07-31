package com.kotlinfoundation.koko.data.source.remote.response.ai.replicate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

// [input] is a concrete JsonObject rather than a type parameter — a generic @Serializable class
// crashes kotlinx.serialization's reflective serializer lookup on Kotlin/Wasm.
@Serializable
data class ReplicatePredictionResponse(
    @SerialName("id") val id: String? = null,
    @SerialName("model") val model: String? = null,
    @SerialName("version") val version: String? = null,
    @SerialName("input") val input: JsonObject? = null,
    @SerialName("logs") val logs: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("output") val output: String? = null,
    @SerialName("error") val error: String? = null,
    @SerialName("data_removed") val dataRemoved: Boolean? = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
) {

    val isInProgress: Boolean get() = status == "starting" || status == "processing"

    val isCompleted: Boolean get() = isSucceeded || isFailed || isCanceled

    val isSucceeded: Boolean get() = status == "succeeded"
    val isFailed: Boolean get() = status == "failed"
    val isCanceled: Boolean get() = status == "canceled"
}
