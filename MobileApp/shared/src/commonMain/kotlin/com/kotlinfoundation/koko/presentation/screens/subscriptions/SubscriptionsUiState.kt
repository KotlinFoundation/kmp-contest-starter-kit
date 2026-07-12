package com.kotlinfoundation.koko.presentation.screens.subscriptions

import com.kotlinfoundation.koko.domain.model.Subscription

data class SubscriptionsUiState(
    val isLoading: Boolean = false,
    val showUpgradePremiumBanner: Boolean = true,
    val currentPlan: Subscription? = null,
)

sealed class SubscriptionsUiEvent
