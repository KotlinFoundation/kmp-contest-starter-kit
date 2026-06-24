package com.kotlinfoundation.kmpstarterkit.presentation.screens.gallery

import com.kotlinfoundation.kmpstarterkit.domain.model.generation.GenerationOutput

data class GalleryUiState(
    val generations: List<GenerationOutput> = emptyList(),
    val isLoading: Boolean = false,
)

sealed class GalleryUiEvent {
    data class OnClickItem(val item: GenerationOutput) : GalleryUiEvent()
    data object OnClickGenerate : GalleryUiEvent()
}
