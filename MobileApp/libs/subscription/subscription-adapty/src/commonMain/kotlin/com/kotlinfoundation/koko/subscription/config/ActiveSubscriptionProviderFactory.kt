package com.kotlinfoundation.koko.subscription.config

import com.kotlinfoundation.koko.subscription.adapty.Adapty
import com.kotlinfoundation.koko.subscription.api.SubscriptionProviderFactory

/**
 * The subscription provider factory for this build.
 *
 * Which implementation backs this symbol is decided at build time by the
 * `SUBSCRIPTION_PROVIDER` Gradle property (see `gradle.properties`), which selects
 * whether `subscription-adapty` or `subscription-revenuecat` is on the classpath.
 * Exactly one provider module is ever linked, so consumers can reference this single
 * symbol and the provider choice lives in one place — the Gradle property.
 */
val activeSubscriptionProviderFactory: SubscriptionProviderFactory
    get() = SubscriptionProviderFactory.Adapty
