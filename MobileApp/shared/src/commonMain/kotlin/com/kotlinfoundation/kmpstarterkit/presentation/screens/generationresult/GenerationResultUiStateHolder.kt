package com.kotlinfoundation.kmpstarterkit.presentation.screens.generationresult

import com.kotlinfoundation.kmpstarterkit.data.repository.GenerationRepository
import com.kotlinfoundation.kmpstarterkit.generated.resources.Res
import com.kotlinfoundation.kmpstarterkit.generated.resources.ai_content_report_submitted_msg
import com.kotlinfoundation.kmpstarterkit.generated.resources.msg_save_failure
import com.kotlinfoundation.kmpstarterkit.generated.resources.msg_save_success
import com.kotlinfoundation.kmpstarterkit.root.AppGlobalUiState
import com.kotlinfoundation.kmpstarterkit.util.UiMessage
import com.kotlinfoundation.kmpstarterkit.util.UiStateHolder
import com.kotlinfoundation.kmpstarterkit.util.analytics.Analytics
import com.kotlinfoundation.kmpstarterkit.util.file.FileManager
import com.kotlinfoundation.kmpstarterkit.util.logging.AppLogger
import com.kotlinfoundation.kmpstarterkit.util.uiStateHolderScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GenerationResultUiStateHolder(
    private val id: String,
    private val generationRepository: GenerationRepository,
    private val fileManager: FileManager,
    private val analytics: Analytics,
) : UiStateHolder() {
    private val _uiState = MutableStateFlow(GenerationResultUiState())
    val uiState: StateFlow<GenerationResultUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun onUiEvent(event: GenerationResultUiEvent) = uiStateHolderScope.launch {
        when (event) {
            GenerationResultUiEvent.OnClickDownload -> saveFileToGallery()

            GenerationResultUiEvent.OnClickShare -> shareOutput()

            GenerationResultUiEvent.OnClickReport -> {
                _uiState.update { it.copy(isReportDialogVisible = true) }
            }

            GenerationResultUiEvent.OnDismissReportDialog -> {
                _uiState.update { it.copy(isReportDialogVisible = false) }
            }

            is GenerationResultUiEvent.OnSubmitReport -> {
                reportContent(event.reason)
                _uiState.update { it.copy(isReportDialogVisible = false) }
            }
        }
    }

    private suspend fun saveFileToGallery() {
        val filePath = _uiState.value.generatedOutput?.output ?: return
        _uiState.update { it.copy(isSaveToGalleryInProgress = true) }

        fileManager.saveFileByPickingUpLocation(filePath)
            .onSuccess {
                AppGlobalUiState.showUiMessage(UiMessage.Resource(Res.string.msg_save_success))
            }.onFailure { error ->
                AppLogger.e("Error saving file to user picked up location: ${error.message}")
                AppGlobalUiState.showUiMessage(UiMessage.Resource(Res.string.msg_save_failure))
            }

        _uiState.update { it.copy(isSaveToGalleryInProgress = false) }
    }

    private suspend fun shareOutput() {
        val filePath = _uiState.value.generatedOutput?.output ?: return
        fileManager.shareFile(filePath)
    }

    private fun loadInitialData() = uiStateHolderScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        val result = generationRepository.getGenerationOutputById(id = id)
        result.onSuccess { generatedOutput ->
            _uiState.update { currentUiState ->
                currentUiState.copy(generatedOutput = generatedOutput, isLoading = false)
            }
        }.onFailure { error ->
            AppGlobalUiState.showUiMessage(UiMessage.Message(error.message))
            _uiState.update { currentUiState -> currentUiState.copy(isLoading = false) }
        }
    }

    private fun reportContent(reason: String) {
        analytics.logEvent(event = Analytics.EVENT_CLICKED_REPORT_AI_CONTENT)
        AppLogger.e("Reporting AI content. Reason: $reason")
        AppGlobalUiState.showUiMessage(
            UiMessage.Resource(Res.string.ai_content_report_submitted_msg),
        )
    }
}
