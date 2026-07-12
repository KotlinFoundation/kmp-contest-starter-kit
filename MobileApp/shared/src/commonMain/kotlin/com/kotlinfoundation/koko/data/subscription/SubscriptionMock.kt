package com.kotlinfoundation.koko.data.subscription

import com.kotlinfoundation.koko.common.BuildConfig
import com.kotlinfoundation.koko.util.isAndroid

/** Preference key backing the mock provider's simulated Premium unlock. */
internal const val KEY_MOCK_PREMIUM_PURCHASED = "KEY_MOCK_PREMIUM_PURCHASED"

/** Placeholder value the build substitutes for an unset key (see `shared/build.gradle.kts`). */
private const val PLACEHOLDER_KEY = "testValue"

/**
 * True when the active-platform subscription SDK key is still a placeholder, i.e. no real
 * Adapty/RevenueCat account is wired yet. While true the app runs `MockSubscriptionProvider` so the
 * whole paywall → purchase → unlock flow is explorable with zero keys. Auto-off once a real key is set.
 */
internal fun isSubscriptionMockActive(): Boolean {
    val key =
        if (isAndroid) {
            BuildConfig.SUBSCRIPTION_PROVIDER_ANDROID_API_KEY
        } else {
            BuildConfig.SUBSCRIPTION_PROVIDER_IOS_API_KEY
        }
    return key.isBlank() || key == PLACEHOLDER_KEY
}
