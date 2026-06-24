package com.kotlinfoundation.kmpstarterkit.subscription.revenuecat

import com.kotlinfoundation.kmpstarterkit.subscription.api.SubscriptionProviderFactory

val SubscriptionProviderFactory.Companion.RevenueCat: SubscriptionProviderFactory
    get() = subscriptionProviderFactory

internal expect val subscriptionProviderFactory: SubscriptionProviderFactory
