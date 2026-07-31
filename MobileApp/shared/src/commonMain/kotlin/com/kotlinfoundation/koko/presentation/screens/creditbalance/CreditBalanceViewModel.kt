package com.kotlinfoundation.koko.presentation.screens.creditbalance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlinfoundation.koko.data.repository.CreditRepository
import com.kotlinfoundation.koko.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreditBalanceViewModel(
    private val creditRepository: CreditRepository,
    private val subscriptionRepository: SubscriptionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreditBalanceUiState())
    val uiState: StateFlow<CreditBalanceUiState> = _uiState.asStateFlow()

    init {
        observeChanges()
    }

    private fun observeChanges() {
        creditRepository.balance
            .onEach { creditBalance ->
                val recurringCredits = creditRepository.getRecurringCredits()
                _uiState.update { currentUiState ->
                    currentUiState.copy(
                        creditBalance = creditBalance,
                        recurringCredits = recurringCredits,
                    )
                }
            }
            .launchIn(viewModelScope)

        subscriptionRepository.currentSubscriptionFlow
            .onEach { subscription ->
                _uiState.update { currentUiState ->
                    currentUiState.copy(
                        isPremiumUser = subscription != null,
                    )
                }
            }
            .launchIn(viewModelScope)

        creditRepository.getRecentTransactionsFlow()
            .onEach { lastTransactions ->
                _uiState.update { currentUiState ->
                    currentUiState.copy(lastTransactions = lastTransactions)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onUiEvent(event: CreditBalanceUiEvent) = viewModelScope.launch {
        when (event) {
            CreditBalanceUiEvent.UpgradeToPremium -> {
                _uiState.update { it.copy(isPremiumRequired = true) }
            }

            CreditBalanceUiEvent.BuyCreditPack -> {
                _uiState.update { it.copy(isMoreCreditRequired = true) }
            }
        }
    }

    fun onPremiumRequiredHandled() {
        _uiState.update { it.copy(isPremiumRequired = false) }
    }

    fun onMoreCreditRequiredHandled() {
        _uiState.update { it.copy(isMoreCreditRequired = false) }
    }
}
