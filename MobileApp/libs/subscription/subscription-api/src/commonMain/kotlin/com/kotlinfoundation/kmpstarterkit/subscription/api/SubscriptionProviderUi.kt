package com.kotlinfoundation.kmpstarterkit.subscription.api

import androidx.compose.runtime.Composable

interface SubscriptionProviderUi {
    companion object {
        fun get(factory: SubscriptionProviderFactory): SubscriptionProviderUi = factory.createProviderUi()
    }

    @Composable
    fun RemotePaywall(
        placementId: String? = null,
        listener: PurchaseEventsListener,
    )
}
