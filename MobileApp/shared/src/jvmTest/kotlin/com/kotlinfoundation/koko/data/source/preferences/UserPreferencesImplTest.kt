package com.kotlinfoundation.koko.data.source.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Exercises the DataStore-backed [UserPreferencesImpl] against a real
 * preferences file in the system temp dir. Each test uses its own file —
 * DataStore forbids two instances on the same file.
 */
class UserPreferencesImplTest {

    private fun newPreferences(): UserPreferences {
        // createTempFile guarantees a unique path; DataStore requires the
        // ".preferences_pb" extension.
        val file = File.createTempFile("user_preferences_test_", ".preferences_pb")
        file.deleteOnExit()
        val dataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { file.absolutePath.toPath() },
        )
        return UserPreferencesImpl(dataStore)
    }

    @Test
    fun `string int long and boolean values round-trip`() = runTest {
        val preferences = newPreferences()

        preferences.putString("string", "hello")
        preferences.putInt("int", 42)
        preferences.putLong("long", 10_000_000_000L)
        preferences.putBoolean("boolean", true)

        assertEquals("hello", preferences.getString("string"))
        assertEquals(42, preferences.getInt("int"))
        assertEquals(10_000_000_000L, preferences.getLong("long"))
        assertTrue(preferences.getBoolean("boolean"))
    }

    @Test
    fun `missing keys fall back to the provided defaults`() = runTest {
        val preferences = newPreferences()

        assertNull(preferences.getString("absent"))
        assertEquals("fallback", preferences.getString("absent", "fallback"))
        assertEquals(7, preferences.getInt("absent", 7))
        assertEquals(7L, preferences.getLong("absent", 7L))
        assertFalse(preferences.getBoolean("absent"))
        assertTrue(preferences.getBoolean("absent", defaultValue = true))
    }

    @Test
    fun `remove deletes a single key and keeps the rest`() = runTest {
        val preferences = newPreferences()
        preferences.putString("keep", "kept")
        preferences.putString("drop", "dropped")

        preferences.remove("drop")

        assertNull(preferences.getString("drop"))
        assertEquals("kept", preferences.getString("keep"))
    }

    @Test
    fun `clear wipes every stored value`() = runTest {
        val preferences = newPreferences()
        preferences.putString("string", "hello")
        preferences.putBoolean("boolean", true)

        preferences.clear()

        assertNull(preferences.getString("string"))
        assertFalse(preferences.getBoolean("boolean"))
    }

    @Test
    fun `overwriting a key keeps only the latest value`() = runTest {
        val preferences = newPreferences()

        preferences.putInt("counter", 1)
        preferences.putInt("counter", 2)

        assertEquals(2, preferences.getInt("counter"))
    }
}
