package com.kotlinfoundation.koko.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlinfoundation.koko.data.source.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnBoardingViewModel(
    private val userPreferences: UserPreferences,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnBoardingUiState(isLoading = true))
    val uiState: StateFlow<OnBoardingUiState> = _uiState.asStateFlow()

    init {
        checkIfOnBoardIsShown()
    }

    fun onUiEvent(event: OnBoardingUiEvent) = viewModelScope.launch {
        when (event) {
            OnBoardingUiEvent.OnClickStart -> {
                userPreferences.putBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN, true)
                _uiState.update { it.copy(isOnBoardingFinished = true, isNewUser = true) }
            }
        }
    }

    fun onFinishHandled() = _uiState.update { it.copy(isOnBoardingFinished = false) }

    private fun checkIfOnBoardIsShown() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        if (userPreferences.getBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN)) {
            _uiState.update { it.copy(isOnBoardingFinished = true, isNewUser = false) }
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
