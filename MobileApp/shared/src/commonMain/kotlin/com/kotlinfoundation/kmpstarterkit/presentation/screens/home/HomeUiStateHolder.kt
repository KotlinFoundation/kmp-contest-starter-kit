package com.kotlinfoundation.kmpstarterkit.presentation.screens.home

import com.kotlinfoundation.kmpstarterkit.data.repository.CreditRepository
import com.kotlinfoundation.kmpstarterkit.data.repository.GenerationRepository
import com.kotlinfoundation.kmpstarterkit.designsystem.components.addorchosefilecontainer.FileItemUiState
import com.kotlinfoundation.kmpstarterkit.domain.exceptions.CreditRequiredException
import com.kotlinfoundation.kmpstarterkit.domain.exceptions.PurchaseRequiredException
import com.kotlinfoundation.kmpstarterkit.domain.exceptions.UnAuthorizedException
import com.kotlinfoundation.kmpstarterkit.domain.model.generation.GenerationInput
import com.kotlinfoundation.kmpstarterkit.domain.model.generation.generationInput
import com.kotlinfoundation.kmpstarterkit.generated.resources.Res
import com.kotlinfoundation.kmpstarterkit.generated.resources.home_error_choosing_file
import com.kotlinfoundation.kmpstarterkit.generated.resources.home_error_file_size
import com.kotlinfoundation.kmpstarterkit.root.AppGlobalUiState
import com.kotlinfoundation.kmpstarterkit.util.Constants
import com.kotlinfoundation.kmpstarterkit.util.UiMessage
import com.kotlinfoundation.kmpstarterkit.util.UiStateHolder
import com.kotlinfoundation.kmpstarterkit.util.file.FileManager
import com.kotlinfoundation.kmpstarterkit.util.file.absolutePathCommon
import com.kotlinfoundation.kmpstarterkit.util.uiStateHolderScope
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeUiStateHolder(
    private val generationRepository: GenerationRepository,
    private val creditRepository: CreditRepository,
    private val fileManager: FileManager,
) : UiStateHolder() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeCreditBalance()
    }

    fun onUiEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.OnClickGenerate -> generate()

            HomeUiEvent.OnClickToolbarCredits -> {
                _uiState.update { it.copy(isMoreCreditsRequired = true) }
            }

            is HomeUiEvent.OnPromptChanged -> {
                _uiState.update { it.copy(prompt = event.value) }
            }

            is HomeUiEvent.OnReferenceImageRemoved -> {
                _uiState.update { it.copy(referenceImage = it.referenceImage.removeFile(event.file)) }
            }

            is HomeUiEvent.OnReferenceImageSelected -> {
                saveFileLocallyAndUpdateState(event.file)
            }
        }
    }

    fun onPremiumRequiredHandled() {
        _uiState.update { it.copy(isPremiumRequired = false) }
    }

    fun onMoreCreditsRequiredHandled() {
        _uiState.update { it.copy(isMoreCreditsRequired = false) }
    }

    fun onGenerationResultHandled() {
        _uiState.update { it.copy(generatedResult = null) }
    }

    fun onAuthRequiredHandled() {
        _uiState.update { it.copy(isAuthRequired = false) }
    }

    private fun observeCreditBalance() = uiStateHolderScope.launch {
        creditRepository.balance.collectLatest { creditBalance ->
            _uiState.update { it.copy(creditBalance = creditBalance) }
        }
    }

    private fun saveFileLocallyAndUpdateState(file: PlatformFile?) = uiStateHolderScope.launch {
        if (file == null) return@launch

        if (file.size() > Constants.MAX_FILE_UPLOAD_SIZE) {
            AppGlobalUiState.showUiMessage(UiMessage.Resource(Res.string.home_error_file_size))
            return@launch
        }
        val originalFileAbsolutePath = file.absolutePathCommon()
        val fileNameWithExtension =
            fileManager.createNewUniqueFileNameWithExtension(file.extension)

        _uiState.update {
            it.copy(
                referenceImage = it.referenceImage.addFile(
                    FileItemUiState(
                        path = originalFileAbsolutePath,
                        nameWithExtension = fileNameWithExtension,
                        isUploading = true,
                    ),
                ),
            )
        }

        fileManager.copyFileToInternalDirectory(
            originalFileAbsolutePath = originalFileAbsolutePath,
            newFileName = fileNameWithExtension,
        ).onSuccess { copiedFileNameWithExtension ->
            _uiState.update {
                it.copy(
                    referenceImage = it.referenceImage.updateFile(
                        FileItemUiState(
                            path = fileManager.getAbsoluteFilePathRelativeToInternal(
                                copiedFileNameWithExtension,
                            ),
                            nameWithExtension = fileNameWithExtension,
                            isUploading = false,
                        ),
                    ),
                )
            }
        }.onFailure {
            AppGlobalUiState.showUiMessage(UiMessage.Resource(Res.string.home_error_choosing_file))
            _uiState.update {
                it.copy(
                    referenceImage = it.referenceImage.removeFile(
                        FileItemUiState(nameWithExtension = fileNameWithExtension, path = ""),
                    ),
                )
            }
        }
    }

    private fun generate() = uiStateHolderScope.launch {
        _uiState.update { it.copy(isGenerationInProgress = true) }
        generationRepository.generate(_uiState.value.buildGenerationInput())
            .onSuccess { generationOutput ->
                _uiState.update {
                    it.copy(
                        generatedResult = generationOutput,
                        isGenerationInProgress = false,
                        prompt = "",
                        referenceImage = it.referenceImage.removeAllFiles(),
                    )
                }
            }.onFailure { error -> onError(error) }
    }

    private fun HomeUiState.buildGenerationInput(): GenerationInput = generationInput {
        stringParam(key = KEY_PROMPT, value = prompt)
        referenceImage.files.forEach { fileItem ->
            image(key = KEY_REFERENCE_IMAGE, fileNameWithExtension = fileItem.nameWithExtension)
        }
    }

    private fun onError(error: Throwable) {
        when (error) {
            is UnAuthorizedException -> {
                _uiState.update {
                    it.copy(isAuthRequired = true, isGenerationInProgress = false)
                }
            }

            is PurchaseRequiredException -> {
                _uiState.update {
                    it.copy(isPremiumRequired = true, isGenerationInProgress = false)
                }
            }

            is CreditRequiredException -> {
                _uiState.update {
                    it.copy(isMoreCreditsRequired = true, isGenerationInProgress = false)
                }
            }

            else -> {
                _uiState.update { it.copy(isGenerationInProgress = false) }
                AppGlobalUiState.showUiMessage(UiMessage.Message(error.message))
            }
        }
    }

    companion object {
        const val KEY_PROMPT = "prompt"
        const val KEY_REFERENCE_IMAGE = "reference_image"
    }
}
