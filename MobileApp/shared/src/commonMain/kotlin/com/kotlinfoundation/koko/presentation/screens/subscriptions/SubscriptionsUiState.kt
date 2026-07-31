package com.kotlinfoundation.koko.presentation.screens.subscriptions

import com.kotlinfoundation.koko.domain.model.Subscription

data class SubscriptionsUiState(
    val isLoading: Boolean = false,
    val showUpgradePremiumBanner: Boolean = true,
    val currentPlan: Subscription? = null,
    val isMock: Boolean = false,
)

sealed interface SubscriptionsUiEvent {
    data object OnCancelMockSubscription : SubscriptionsUiEvent
}
