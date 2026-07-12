package com.kotlinfoundation.koko.domain.model.generation

import com.kotlinfoundation.koko.domain.model.generation.GenerationParam.ParamType

/**
 * A single AI-generation input parameter as a type-tagged key/value pair.
 *
 * The [value] is always stored as a `String` (files are serialized too) so the whole param
 * map can be flattened to one string for local persistence — see [serialize]/[deserialize].
 * Read it back in its real type with the [typedValue] extension, which reinterprets [value]
 * according to [type].
 */
class GenerationParam(
    val key: String,
    val type: ParamType,
    val value: String?,
) {

    enum class ParamType(val value: String) {
        STRING("string"),
        INTEGER("integer"),
        FLOAT("float"),
        BOOLEAN("boolean"),
        FILE("file"),
        ;

        companion object {
            fun fromValue(value: String): ParamType = entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: STRING
        }
    }

    inline fun mapFilePaths(mapper: (String) -> String): GenerationParam = if (type == ParamType.FILE) {
        val updatedFiles = typedValue<List<GenerationFile>>(emptyList()).map { file ->
            file.copy(fileNameWithExtension = mapper(file.fileNameWithExtension))
        }
        ofFiles(key, updatedFiles)
    } else {
        this
    }

    inline fun mapFileUploadUrls(mapper: (String) -> String): GenerationParam = if (type == ParamType.FILE) {
        val updatedFiles = typedValue<List<GenerationFile>>(emptyList()).map { file ->
            file.copy(uploadedFileUrl = mapper(file.fileNameWithExtension))
        }
        ofFiles(key, updatedFiles)
    } else {
        this
    }

    companion object {

        private const val LIST_SEPARATOR = "|||"
        private const val ITEM_SEPARATOR = ":::"
        const val DEFAULT_FILE_PARAM_KEY = "file_attachments"

        fun ofString(key: String, value: String? = null) = GenerationParam(key = key, type = ParamType.STRING, value = value)

        fun ofNumber(key: String, value: Number? = null) = GenerationParam(key = key, type = ParamType.INTEGER, value = value?.toString())

        fun ofBoolean(key: String, value: Boolean? = null) = GenerationParam(key = key, type = ParamType.BOOLEAN, value = value?.toString())

        fun ofFloat(key: String, value: Float? = null) = GenerationParam(key = key, type = ParamType.FLOAT, value = value?.toString())

        fun ofFiles(key: String, files: List<GenerationFile>): GenerationParam {
            val value = GenerationFile.serialize(files)
            return GenerationParam(key, ParamType.FILE, value)
        }

        fun serialize(params: Map<String, GenerationParam>): String = params.entries.joinToString(LIST_SEPARATOR) { (key, param) ->
            "${param.key}$ITEM_SEPARATOR${param.type.value}$ITEM_SEPARATOR${param.value}"
        }

        fun deserialize(serialized: String): Map<String, GenerationParam> = if (serialized.isEmpty()) {
            emptyMap()
        } else {
            serialized.split(LIST_SEPARATOR).associate {
                val (key, typeStr, value) = it.split(ITEM_SEPARATOR)
                key to GenerationParam(
                    key = key,
                    type = ParamType.fromValue(typeStr),
                    value = value,
                )
            }
        }
    }
}

/** Reinterpret a param's stored string [value] in its real type [T], falling back to [default]. */
inline fun <reified T> GenerationParam?.typedValue(default: T): T {
    if (this == null) return default

    return when (type) {
        ParamType.STRING -> (value as? T) ?: default

        ParamType.INTEGER -> (value?.toIntOrNull() as? T) ?: default

        ParamType.FLOAT -> (value?.toFloatOrNull() as? T) ?: default

        ParamType.BOOLEAN -> (value?.toBooleanStrictOrNull() as? T) ?: default

        ParamType.FILE -> {
            val files = GenerationFile.deserialize(value ?: "")
            if (T::class == List::class) files as T else default
        }
    }
}
