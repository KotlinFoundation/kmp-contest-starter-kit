package com.kotlinfoundation.koko.presentation.screens.generationresult

import com.kotlinfoundation.koko.domain.model.generation.GenerationOutput

data class GenerationResultUiState(
    val generatedOutput: GenerationOutput? = null,
    val isLoading: Boolean = false,
    val isSaveToGalleryInProgress: Boolean = false,
    val isReportDialogVisible: Boolean = false,
)

sealed interface GenerationResultUiEvent {
    data object OnClickShare : GenerationResultUiEvent
    data object OnClickDownload : GenerationResultUiEvent
    data object OnClickReport : GenerationResultUiEvent
    data class OnSubmitReport(val reason: String) : GenerationResultUiEvent
    data object OnDismissReportDialog : GenerationResultUiEvent
}
