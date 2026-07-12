package com.kotlinfoundation.koko.data.source.remote.request.ai.replicate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a request to create a prediction on a Replicate model.
 * Some models require version of the model to be specified, while others don't.
 * If you used create model prediction, then you need to pass null for version.
 */

@Serializable
data class ReplicatePredictionRequest<Input>(
    @SerialName("version") val version: String? = null,
    @SerialName("input") val input: Input,
)
