package com.kotlinfoundation.kmpstarterkit.presentation.screens.subscriptions

import com.kotlinfoundation.kmpstarterkit.domain.model.Subscription

data class SubscriptionsUiState(
    val isLoading: Boolean = false,
    val showUpgradePremiumBanner: Boolean = true,
    val currentPlan: Subscription? = null,
)

sealed class SubscriptionsUiEvent
