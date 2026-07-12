package com.kotlinfoundation.koko.data.source.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

// DataStore Preferences-backed UserPreferences. Each get/put resolves the typed key for the name.
internal class UserPreferencesImpl(private val dataStore: DataStore<Preferences>) : UserPreferences {

    override suspend fun getString(key: String, defaultValue: String?): String? = dataStore.data.first()[stringPreferencesKey(key)] ?: defaultValue

    override suspend fun getInt(key: String, defaultValue: Int?): Int? = dataStore.data.first()[intPreferencesKey(key)] ?: defaultValue

    override suspend fun getLong(key: String, defaultValue: Long?): Long? = dataStore.data.first()[longPreferencesKey(key)] ?: defaultValue

    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean = dataStore.data.first()[booleanPreferencesKey(key)] ?: defaultValue

    override suspend fun putString(key: String, value: String) {
        dataStore.edit { it[stringPreferencesKey(key)] = value }
    }

    override suspend fun putInt(key: String, value: Int) {
        dataStore.edit { it[intPreferencesKey(key)] = value }
    }

    override suspend fun putLong(key: String, value: Long) {
        dataStore.edit { it[longPreferencesKey(key)] = value }
    }

    override suspend fun putBoolean(key: String, value: Boolean) {
        dataStore.edit { it[booleanPreferencesKey(key)] = value }
    }

    override suspend fun remove(key: String) {
        // The interface is name-keyed while DataStore keys are (name, type) pairs —
        // removing every type variant of the name is the intended behavior.
        dataStore.edit { preferences ->
            preferences.asMap().keys
                .filter { it.name == key }
                .forEach { preferences.remove(it) }
        }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
