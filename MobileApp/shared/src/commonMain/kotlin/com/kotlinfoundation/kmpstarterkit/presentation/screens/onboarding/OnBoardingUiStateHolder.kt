package com.kotlinfoundation.kmpstarterkit.presentation.screens.onboarding

import com.kotlinfoundation.kmpstarterkit.data.source.preferences.UserPreferences
import com.kotlinfoundation.kmpstarterkit.util.UiStateHolder
import com.kotlinfoundation.kmpstarterkit.util.uiStateHolderScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnBoardingUiStateHolder(
    private val userPreferences: UserPreferences,
) : UiStateHolder() {
    private val _uiState = MutableStateFlow(OnBoardingUiState(isLoading = true))
    val uiState: StateFlow<OnBoardingUiState> = _uiState.asStateFlow()

    init {
        checkIfOnBoardIsShown()
    }

    private fun checkIfOnBoardIsShown() = uiStateHolderScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        if (userPreferences.getBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN)) {
            _uiState.update { it.copy(onBoardIsShown = true) }
        } else {
            _uiState.update { it.copy(onBoardIsShown = false, isLoading = false) }
        }
    }

    fun onUiEvent(event: OnBoardingUiEvent) = uiStateHolderScope.launch {
        when (event) {
            OnBoardingUiEvent.OnClickStart -> {
                onBoardShown()
            }

            OnBoardingUiEvent.OnClickGetPremiumAccess -> {
                _uiState.update { it.copy(isPremiumRequired = true) }
            }
        }
    }

    fun onPaywallEventHandled() = uiStateHolderScope.launch {
        _uiState.update { it.copy(isPremiumRequired = false) }
        onBoardShown()
    }

    private suspend fun onBoardShown() {
        _uiState.update { it.copy(isLoading = true) }
        userPreferences.putBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN, true)
        _uiState.update { it.copy(onBoardIsShown = true, isLoading = false) }
    }
}
