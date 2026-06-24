package com.kotlinfoundation.kmpstarterkit.presentation.screens.creditbalance

import com.kotlinfoundation.kmpstarterkit.data.repository.CreditRepository
import com.kotlinfoundation.kmpstarterkit.data.repository.SubscriptionRepository
import com.kotlinfoundation.kmpstarterkit.util.UiStateHolder
import com.kotlinfoundation.kmpstarterkit.util.uiStateHolderScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreditBalanceUiStateHolder(
    private val creditRepository: CreditRepository,
    private val subscriptionRepository: SubscriptionRepository,
) : UiStateHolder() {
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
            .launchIn(uiStateHolderScope)

        subscriptionRepository.currentSubscriptionFlow
            .onEach { subscription ->
                _uiState.update { currentUiState ->
                    currentUiState.copy(
                        isPremiumUser = subscription != null,
                    )
                }
            }
            .launchIn(uiStateHolderScope)

        creditRepository.getRecentTransactionsFlow()
            .onEach { lastTransactions ->
                _uiState.update { currentUiState ->
                    currentUiState.copy(lastTransactions = lastTransactions)
                }
            }
            .launchIn(uiStateHolderScope)
    }

    fun onUiEvent(event: CreditBalanceUiEvent) = uiStateHolderScope.launch {
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
