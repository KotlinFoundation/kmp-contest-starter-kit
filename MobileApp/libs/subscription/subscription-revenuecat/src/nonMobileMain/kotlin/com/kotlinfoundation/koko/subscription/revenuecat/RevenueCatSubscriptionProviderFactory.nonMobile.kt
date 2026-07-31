package com.kotlinfoundation.koko.subscription.revenuecat

import com.kotlinfoundation.koko.subscription.api.NoOpSubscriptionProvider
import com.kotlinfoundation.koko.subscription.api.NoOpSubscriptionProviderUi
import com.kotlinfoundation.koko.subscription.api.SubscriptionProvider
import com.kotlinfoundation.koko.subscription.api.SubscriptionProviderFactory
import com.kotlinfoundation.koko.subscription.api.SubscriptionProviderUi

internal actual val subscriptionProviderFactory: SubscriptionProviderFactory
    get() =
        object : SubscriptionProviderFactory {
            override fun createProvider(): SubscriptionProvider = NoOpSubscriptionProvider

            override fun createProviderUi(): SubscriptionProviderUi = NoOpSubscriptionProviderUi
        }
