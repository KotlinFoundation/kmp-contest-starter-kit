package com.kotlinfoundation.koko.data.source.remote.request.ai.replicate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents  an example Replicate Input Request. Needs to be updated if needed
 *
 **/

@Serializable
data class ReplicateInputRequest(
    @SerialName("prompt") val prompt: String? = null,
)
