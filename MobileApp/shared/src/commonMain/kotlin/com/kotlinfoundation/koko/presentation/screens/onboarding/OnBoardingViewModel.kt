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

    private fun checkIfOnBoardIsShown() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        if (userPreferences.getBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN)) {
            _uiState.update { it.copy(onBoardIsShown = true) }
        } else {
            _uiState.update { it.copy(onBoardIsShown = false, isLoading = false) }
        }
    }

    fun onUiEvent(event: OnBoardingUiEvent) = viewModelScope.launch {
        when (event) {
            OnBoardingUiEvent.OnClickStart -> {
                onBoardShown()
            }

            OnBoardingUiEvent.OnClickGetPremiumAccess -> {
                _uiState.update { it.copy(isPremiumRequired = true) }
            }
        }
    }

    fun onPaywallEventHandled() = viewModelScope.launch {
        _uiState.update { it.copy(isPremiumRequired = false) }
        onBoardShown()
    }

    private suspend fun onBoardShown() {
        _uiState.update { it.copy(isLoading = true) }
        userPreferences.putBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN, true)
        _uiState.update { it.copy(onBoardIsShown = true, isLoading = false) }
    }
}
