package com.kotlinfoundation.koko.domain.model.generation

import com.kotlinfoundation.koko.domain.model.generation.GenerationParam.Companion.DEFAULT_FILE_PARAM_KEY

/**
 * The input to an AI generation request: the user's [userInput] prompt plus any typed
 * [params] (text, numbers, file attachments). Build it with the [generationInput] DSL.
 */
data class GenerationInput(
    val userInput: String,
    val params: Map<String, GenerationParam> = emptyMap(),
    val canAcceptMultipleFiles: Boolean = DEFAULT_CAN_ACCEPT_MULTIPLE_FILES,
) {

    /** The text sent to the model: the (app-defined) system prompt followed by the user input. */
    fun fullPrompt(): String = buildString {
        val systemPrompt = getSystemPrompt()
        if (systemPrompt.isNotBlank()) {
            appendLine(systemPrompt.trim())
        }
        append(userInput.trim())
    }

    fun getFileAttachments(key: String = DEFAULT_FILE_PARAM_KEY): List<GenerationFile> = params[key]?.typedValue(emptyList()) ?: emptyList()

    fun getAllFileAttachments(): List<GenerationFile> = params.values
        .filter { it.type == GenerationParam.ParamType.FILE }
        .flatMap { it.typedValue<List<GenerationFile>>(emptyList()) }

    fun withFileAttachments(
        key: String = DEFAULT_FILE_PARAM_KEY,
        files: List<GenerationFile>,
    ): GenerationInput {
        val newParams = params.toMutableMap().apply {
            this[key] = GenerationParam.ofFiles(key, files)
        }
        return copy(params = newParams)
    }

    companion object {
        // Prepended to every prompt. Empty by default — fill in per app to steer the model.
        fun getSystemPrompt(): String = """

        """.trimIndent()
        val DEFAULT_CAN_ACCEPT_MULTIPLE_FILES: Boolean = true // For nano banana this is always true
    }
}
