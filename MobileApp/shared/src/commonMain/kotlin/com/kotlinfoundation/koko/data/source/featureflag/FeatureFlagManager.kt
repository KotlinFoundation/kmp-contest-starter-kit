package com.kotlinfoundation.koko.data.source.featureflag

/**
 * Remote feature flags (backed by Firebase Remote Config on Android/iOS). Flags fall back to
 * [DEFAULT_VALUES] until [syncsFlagsAsync] fetches fresh values, and on platforms without an
 * implementation (see [NoImplFeatureFlagManager]).
 */
interface FeatureFlagManager {

    object Keys {
        const val IS_ADS_ENABLED = "is_ads_enabled"
        const val IS_ANALYTICS_ENABLED = "is_analytics_enabled"
        const val SHOW_REMOTE_PAYWALL = "show_remote_paywall"
    }

    companion object {
        // Add Optional Default Feature Flag Values Here
        val DEFAULT_VALUES: Map<String, Comparable<Nothing>> = mapOf(
            Keys.IS_ADS_ENABLED to false,
            Keys.IS_ANALYTICS_ENABLED to true,
            Keys.SHOW_REMOTE_PAYWALL to false, // Set to true to use the provider's built-in remote paywall (Adapty/RevenueCat UI) instead of the custom one.
        )
    }

    fun syncsFlagsAsync()
    fun getBoolean(key: String): Boolean
    fun getString(key: String): String
    fun getLong(key: String): Long
    fun getDouble(key: String): Double
}
