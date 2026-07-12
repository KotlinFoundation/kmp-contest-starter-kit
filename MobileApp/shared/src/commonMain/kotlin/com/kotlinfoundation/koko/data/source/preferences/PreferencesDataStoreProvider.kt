package com.kotlinfoundation.koko.data.source.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

// Per-platform supplier of the DataStore instance (file-based on Android/iOS/JVM, WebLocalStorage
// on js/wasmJs). Not a Koin binding itself — UserPreferencesImpl obtains it through this provider.
interface PreferencesDataStoreProvider {
    fun providePreferencesDataStore(): DataStore<Preferences>
}
