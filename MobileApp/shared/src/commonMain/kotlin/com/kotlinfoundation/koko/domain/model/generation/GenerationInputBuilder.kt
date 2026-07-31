package com.kotlinfoundation.koko.domain.model.generation

// DSL entry point to create a GenerationInput, e.g. generationInput { stringParam("prompt", text) }
fun generationInput(block: GenerationInputBuilder.() -> Unit): GenerationInput = GenerationInputBuilder().apply(block).build()

/** Type-safe builder for [GenerationInput] params. `file`/`image`/`video` append (or [replace]) attachments. */
class GenerationInputBuilder {
    var userInput: String = ""
    private val _params = mutableMapOf<String, GenerationParam>()

    val params: Map<String, GenerationParam> get() = _params

    fun file(
        key: String,
        fileNameWithExtension: String,
        type: GenerationFile.Type,
        replace: Boolean = false,
    ) {
        val updatedFiles = if (replace) {
            listOf(GenerationFile(fileNameWithExtension = fileNameWithExtension, type = type))
        } else {
            val existingFiles =
                _params[key]?.typedValue<List<GenerationFile>>(emptyList()) ?: emptyList()
            existingFiles + GenerationFile(fileNameWithExtension, type)
        }

        _params[key] = GenerationParam.ofFiles(key, updatedFiles)
    }

    fun image(key: String, fileNameWithExtension: String, replace: Boolean = false) {
        file(
            key = key,
            fileNameWithExtension = fileNameWithExtension,
            type = GenerationFile.Type.IMAGE,
            replace = replace,
        )
    }

    fun video(key: String, fileNameWithExtension: String, replace: Boolean = false) {
        file(
            key = key,
            fileNameWithExtension = fileNameWithExtension,
            type = GenerationFile.Type.VIDEO,
            replace = replace,
        )
    }

    fun stringParam(key: String, value: String? = null) {
        _params[key] = GenerationParam.ofString(key, value)
    }

    fun intParam(key: String, value: Number? = null) {
        _params[key] = GenerationParam.ofNumber(key, value)
    }

    fun floatParam(key: String, value: Float? = null) {
        _params[key] = GenerationParam.ofFloat(key, value)
    }

    fun booleanParam(key: String, value: Boolean? = null) {
        _params[key] = GenerationParam.ofBoolean(key, value)
    }

    fun build(): GenerationInput = GenerationInput(
        userInput = userInput,
        params = params,
    )
}
