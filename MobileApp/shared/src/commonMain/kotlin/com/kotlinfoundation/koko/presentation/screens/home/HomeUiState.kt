package com.kotlinfoundation.koko.presentation.screens.home

import com.kotlinfoundation.koko.designsystem.components.addorchosefilecontainer.AddOrChooseFileContainerUiState
import com.kotlinfoundation.koko.designsystem.components.addorchosefilecontainer.FileItemUiState
import com.kotlinfoundation.koko.designsystem.components.addorchosefilecontainer.FileSelectionMode
import com.kotlinfoundation.koko.designsystem.components.addorchosefilecontainer.FileUiType
import com.kotlinfoundation.koko.domain.model.generation.GenerationOutput
import io.github.vinceglb.filekit.PlatformFile

/**
 * Demo Home screen state — a concrete, hand-written example form (a text prompt plus an
 * optional reference image). Replace these fields with whatever inputs your app needs;
 * this is intentionally plain Compose, not a generic form engine.
 */
data class HomeUiState(
    val prompt: String = "",
    val referenceImage: AddOrChooseFileContainerUiState = AddOrChooseFileContainerUiState(
        mode = FileSelectionMode.Single(FileUiType.IMAGE),
    ),
    val isMoreCreditsRequired: Boolean = false,
    val creditBalance: Int = 0,
    val isPremiumRequired: Boolean = false,
    val isAuthRequired: Boolean = false,
    val isGenerationInProgress: Boolean = false,
    val generatedResult: GenerationOutput? = null,
) {
    val isGenerationButtonEnabled: Boolean
        get() = isGenerationInProgress.not() && prompt.isNotBlank()
}

sealed interface HomeUiEvent {
    data object OnClickGenerate : HomeUiEvent
    data object OnClickToolbarCredits : HomeUiEvent

    data class OnPromptChanged(val value: String) : HomeUiEvent
    data class OnReferenceImageSelected(val file: PlatformFile?) : HomeUiEvent
    data class OnReferenceImageRemoved(val file: FileItemUiState) : HomeUiEvent
}
