@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package com.kotlinfoundation.koko.domain.model.generation

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * The result of a generation request: the original [input] plus the produced [output] (text)
 * and/or [outputFile] (image/video/file), with the current [status].
 */
data class GenerationOutput(
    val id: String = Uuid.random().toString(),
    val input: GenerationInput,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val output: String? = null,
    val outputFile: GenerationFile? = null,
    val status: Status? = null,
) {
    enum class Status {
        IN_PROGRESS,
        COMPLETED,
        FAILED,
    }
}
