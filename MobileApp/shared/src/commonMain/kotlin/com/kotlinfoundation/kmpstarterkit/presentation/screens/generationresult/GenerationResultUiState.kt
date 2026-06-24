package com.kotlinfoundation.kmpstarterkit.presentation.screens.generationresult

import com.kotlinfoundation.kmpstarterkit.domain.model.generation.GenerationOutput

data class GenerationResultUiState(
    val generatedOutput: GenerationOutput? = null,
    val isLoading: Boolean = false,
    val isSaveToGalleryInProgress: Boolean = false,
    val isReportDialogVisible: Boolean = false,
)

sealed class GenerationResultUiEvent {
    data object OnClickShare : GenerationResultUiEvent()
    data object OnClickDownload : GenerationResultUiEvent()
    data object OnClickReport : GenerationResultUiEvent()
    data class OnSubmitReport(val reason: String) : GenerationResultUiEvent()
    data object OnDismissReportDialog : GenerationResultUiEvent()
}
