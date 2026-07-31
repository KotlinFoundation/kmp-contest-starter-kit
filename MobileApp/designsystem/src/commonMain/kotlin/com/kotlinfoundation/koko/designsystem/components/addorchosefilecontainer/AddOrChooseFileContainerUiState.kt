package com.kotlinfoundation.koko.designsystem.components.addorchosefilecontainer

import com.kotlinfoundation.koko.designsystem.util.UiText

data class AddOrChooseFileContainerUiState(
    val mode: FileSelectionMode,
    val emptyStateTitleUiText: UiText? = null, // Default one will be used if null
    val emptyStateDescriptionUiText: UiText? = null, // Default one will be used if null
    val nonEmptyStateTitleUiText: UiText? = null, // Default one will be used if null
    val nonEmptyStateDescriptionUiText: UiText? = null, // Default one will be used if null
    val isHintTextVisible: Boolean = true,
    val hintUiText: UiText? = null, // Default one will be used if null
) {
    fun addFile(file: FileItemUiState): AddOrChooseFileContainerUiState = copy(mode = mode.addFile(file))

    fun removeFile(file: FileItemUiState): AddOrChooseFileContainerUiState = copy(mode = mode.removeFile(file))

    fun removeAllFiles(): AddOrChooseFileContainerUiState = copy(mode = mode.removeAllFiles())

    fun updateFile(file: FileItemUiState): AddOrChooseFileContainerUiState = copy(mode = mode.updateFile(file))

    val files
        get() = when (mode) {
            is FileSelectionMode.Single -> listOfNotNull(mode.selectedFile)
            is FileSelectionMode.Multiple -> mode.selectedFiles
        }
    val maxAllowed
        get() = when (mode) {
            is FileSelectionMode.Single -> 1
            is FileSelectionMode.Multiple -> mode.maxAllowedFiles
        }

    val noFilesSelected get() = files.isEmpty()
    val containsAtLeastOneFile get() = files.isNotEmpty()
}

enum class FileUiType {
    IMAGE,
    VIDEO,
}

data class FileItemUiState(
    val path: String,
    val nameWithExtension: String,
    val isUploading: Boolean = false,
)

sealed interface FileSelectionMode {
    val type: FileUiType

    data class Single(
        override val type: FileUiType,
        val selectedFile: FileItemUiState? = null,
    ) : FileSelectionMode

    data class Multiple(
        override val type: FileUiType,
        val selectedFiles: List<FileItemUiState> = emptyList(),
        val maxAllowedFiles: Int = 4,
    ) : FileSelectionMode
}

fun FileSelectionMode.removeAllFiles(): FileSelectionMode = when (this) {
    is FileSelectionMode.Single -> copy(selectedFile = null)
    is FileSelectionMode.Multiple -> copy(selectedFiles = emptyList())
}

fun FileSelectionMode.removeFile(file: FileItemUiState): FileSelectionMode = when (this) {
    is FileSelectionMode.Single -> copy(selectedFile = null)
    is FileSelectionMode.Multiple -> copy(selectedFiles = selectedFiles.filter { it.nameWithExtension != file.nameWithExtension })
}

fun FileSelectionMode.addFile(file: FileItemUiState): FileSelectionMode = when (this) {
    is FileSelectionMode.Single -> copy(selectedFile = file)

    is FileSelectionMode.Multiple -> copy(
        selectedFiles = (selectedFiles + file).take(maxAllowedFiles),
    )
}

fun FileSelectionMode.updateFile(updatedFile: FileItemUiState): FileSelectionMode = when (this) {
    is FileSelectionMode.Single -> if (updatedFile.nameWithExtension == selectedFile?.nameWithExtension) {
        copy(
            selectedFile = updatedFile,
        )
    } else {
        this
    }

    is FileSelectionMode.Multiple -> copy(selectedFiles = selectedFiles.map { if (it.nameWithExtension == updatedFile.nameWithExtension) updatedFile else it })
}
