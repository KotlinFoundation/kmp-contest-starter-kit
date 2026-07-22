package com.kotlinfoundation.koko.data.source.remote.request.ai.replicate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Represents a request to create a prediction on a Replicate model.
 * Some models require version of the model to be specified, while others don't.
 * If you used create model prediction, then you need to pass null for version.
 *
 * [input] is a [JsonObject] (not a type parameter) so the serializer stays concrete — a generic
 * `@Serializable` class crashes kotlinx.serialization's reflective lookup on Kotlin/Wasm.
 */
@Serializable
data class ReplicatePredictionRequest(
    @SerialName("version") val version: String? = null,
    @SerialName("input") val input: JsonObject,
)
