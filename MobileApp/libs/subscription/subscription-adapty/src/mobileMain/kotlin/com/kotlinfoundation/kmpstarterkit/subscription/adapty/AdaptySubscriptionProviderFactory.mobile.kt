package com.kotlinfoundation.kmpstarterkit.subscription.adapty

import com.kotlinfoundation.kmpstarterkit.subscription.api.SubscriptionProvider
import com.kotlinfoundation.kmpstarterkit.subscription.api.SubscriptionProviderFactory
import com.kotlinfoundation.kmpstarterkit.subscription.api.SubscriptionProviderUi

internal actual val subscriptionProviderFactory: SubscriptionProviderFactory
    get() =
        object : SubscriptionProviderFactory {
            override fun createProvider(): SubscriptionProvider = AdaptySubscriptionProvider()

            override fun createProviderUi(): SubscriptionProviderUi = AdaptySubscriptionProviderUi()
        }
