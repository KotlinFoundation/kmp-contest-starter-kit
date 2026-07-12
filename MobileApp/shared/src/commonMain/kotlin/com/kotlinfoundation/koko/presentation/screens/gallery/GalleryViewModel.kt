package com.kotlinfoundation.koko.presentation.screens.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlinfoundation.koko.data.repository.GenerationRepository
import com.kotlinfoundation.koko.util.logging.AppLogger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

class GalleryViewModel(generationRepository: GenerationRepository) : ViewModel() {
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
            viewModelScope,
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
