package com.kotlinfoundation.kmpstarterkit.data.source.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.kotlinfoundation.kmpstarterkit.util.Constants
import okio.Path.Companion.toPath
import java.io.File

class PreferencesDataStoreProviderImpl : PreferencesDataStoreProvider {
    override fun providePreferencesDataStore(): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            // Not java.io.tmpdir — the OS may wipe it, silently resetting user preferences.
            val appDataDir = File(System.getProperty("user.home"), ".kmpstarterkit")
            appDataDir.mkdirs()
            File(appDataDir, Constants.PREFERENCES_STORAGE_NAME)
                .absolutePath
                .toPath()
        },
    )
}
