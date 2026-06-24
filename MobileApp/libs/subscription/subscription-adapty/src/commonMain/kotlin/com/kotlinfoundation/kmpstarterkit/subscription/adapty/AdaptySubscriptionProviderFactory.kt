package com.kotlinfoundation.kmpstarterkit.subscription.adapty

import com.kotlinfoundation.kmpstarterkit.subscription.api.SubscriptionProviderFactory

val SubscriptionProviderFactory.Companion.Adapty: SubscriptionProviderFactory
    get() = subscriptionProviderFactory

internal expect val subscriptionProviderFactory: SubscriptionProviderFactory
