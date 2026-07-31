package com.kotlinfoundation.koko.data.source.featureflag

// Fallback used on platforms (desktop/web) without a remote-config backend: always returns the
// compile-time DEFAULT_VALUES.
object NoImplFeatureFlagManager : FeatureFlagManager {

    override fun syncsFlagsAsync() {
        println("Implementation is not available in this platform")
    }

    override fun getBoolean(key: String): Boolean = FeatureFlagManager.DEFAULT_VALUES[key] as? Boolean ?: false

    override fun getString(key: String): String = FeatureFlagManager.DEFAULT_VALUES[key] as? String ?: ""

    override fun getLong(key: String): Long = FeatureFlagManager.DEFAULT_VALUES[key] as? Long ?: 0L

    override fun getDouble(key: String): Double = FeatureFlagManager.DEFAULT_VALUES[key] as? Double ?: 0.0
}
