package com.kotlinfoundation.koko.subscription.adapty

import com.kotlinfoundation.koko.subscription.api.SubscriptionProviderFactory

val SubscriptionProviderFactory.Companion.Adapty: SubscriptionProviderFactory
    get() = subscriptionProviderFactory

internal expect val subscriptionProviderFactory: SubscriptionProviderFactory
