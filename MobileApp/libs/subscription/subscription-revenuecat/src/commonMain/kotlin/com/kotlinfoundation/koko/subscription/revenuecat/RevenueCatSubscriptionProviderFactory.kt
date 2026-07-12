package com.kotlinfoundation.koko.subscription.revenuecat

import com.kotlinfoundation.koko.subscription.api.SubscriptionProviderFactory

val SubscriptionProviderFactory.Companion.RevenueCat: SubscriptionProviderFactory
    get() = subscriptionProviderFactory

internal expect val subscriptionProviderFactory: SubscriptionProviderFactory
