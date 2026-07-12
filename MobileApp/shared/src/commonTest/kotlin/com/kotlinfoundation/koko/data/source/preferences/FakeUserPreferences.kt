package com.kotlinfoundation.koko.data.source.preferences

/**
 * In-memory [UserPreferences] for tests — no DataStore, no disk. Reusable from
 * any commonTest that needs to stub stored flags (onboarding shown, paywall
 * dismiss count, ...).
 */
class FakeUserPreferences : UserPreferences {

    private val values = mutableMapOf<String, Any>()

    override suspend fun getString(key: String, defaultValue: String?): String? = values[key] as? String ?: defaultValue

    override suspend fun getInt(key: String, defaultValue: Int?): Int? = values[key] as? Int ?: defaultValue

    override suspend fun getLong(key: String, defaultValue: Long?): Long? = values[key] as? Long ?: defaultValue

    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean = values[key] as? Boolean ?: defaultValue

    override suspend fun putString(key: String, value: String) {
        values[key] = value
    }

    override suspend fun putInt(key: String, value: Int) {
        values[key] = value
    }

    override suspend fun putLong(key: String, value: Long) {
        values[key] = value
    }

    override suspend fun putBoolean(key: String, value: Boolean) {
        values[key] = value
    }

    override suspend fun remove(key: String) {
        values.remove(key)
    }

    override suspend fun clear() {
        values.clear()
    }
}
