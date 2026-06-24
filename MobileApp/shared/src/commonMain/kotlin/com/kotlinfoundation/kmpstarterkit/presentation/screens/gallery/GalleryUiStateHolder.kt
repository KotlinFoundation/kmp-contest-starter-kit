package com.kotlinfoundation.kmpstarterkit.presentation.screens.gallery

import com.kotlinfoundation.kmpstarterkit.data.repository.GenerationRepository
import com.kotlinfoundation.kmpstarterkit.util.UiStateHolder
import com.kotlinfoundation.kmpstarterkit.util.logging.AppLogger
import com.kotlinfoundation.kmpstarterkit.util.uiStateHolderScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

class GalleryUiStateHolder(generationRepository: GenerationRepository) : UiStateHolder() {
    val uiState: StateFlow<GalleryUiState> = generationRepository.observeAllGenerationOutput()
        .onEach { result ->
            if (result.isFailure) {
                AppLogger.e(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
        .map { result ->
            val list = result.getOrNull() ?: emptyList()
            GalleryUiState(generations = list, isLoading = false)
        }.stateIn(
            uiStateHolderScope,
            SharingStarted.WhileSubscribed(5000),
            GalleryUiState(isLoading = true),
        )

    fun onUiEvent(event: GalleryUiEvent) {
        when (event) {
            is GalleryUiEvent.OnClickItem -> Unit
            GalleryUiEvent.OnClickGenerate -> Unit
        }
    }
}
