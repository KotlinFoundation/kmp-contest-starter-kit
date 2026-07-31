package com.kotlinfoundation.koko.domain.model.generation

/**
 * A file attached to or produced by a generation: its local [fileNameWithExtension], [type],
 * and (once uploaded for the AI request) its [uploadedFileUrl]. String-serializable so it can
 * be stored inside a [GenerationParam] value.
 */
data class GenerationFile(
    val fileNameWithExtension: String,
    val type: Type,
    val uploadedFileUrl: String? = null,
) {

    companion object {

        // Custom delimiters to pack a list of files into one string (see GenerationParam).
        private const val LIST_SEPARATOR = "|;|"
        private const val ITEM_SEPARATOR = "|:|"
        fun serialize(files: List<GenerationFile>): String = files.joinToString(LIST_SEPARATOR) { "${it.fileNameWithExtension}$ITEM_SEPARATOR${it.type.value}$ITEM_SEPARATOR${it.uploadedFileUrl}" }

        fun deserialize(serialized: String): List<GenerationFile> = if (serialized.isEmpty()) {
            emptyList()
        } else {
            serialized.split(LIST_SEPARATOR).map {
                val (path, typeStr, uploadFileUrl) = it.split(ITEM_SEPARATOR)
                GenerationFile(
                    fileNameWithExtension = path,
                    type = Type.fromValue(typeStr),
                    uploadedFileUrl = uploadFileUrl,
                )
            }
        }
    }

    enum class Type(val value: String) {
        IMAGE("image"),
        VIDEO("video"),
        FILE("file"),
        ;

        companion object {
            fun fromValue(value: String): Type = entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: FILE
        }
    }
}
