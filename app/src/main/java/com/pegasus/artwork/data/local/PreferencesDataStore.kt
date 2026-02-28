package com.pegasus.artwork.data.local

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        private val KEY_ROM_DIRECTORY_URI = stringPreferencesKey("rom_directory_uri")
        private val KEY_SS_USERNAME = stringPreferencesKey("ss_username")
        private val KEY_SS_PASSWORD = stringPreferencesKey("ss_password")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_NOT_FOUND_ROMS = stringSetPreferencesKey("not_found_roms")
        private val KEY_MAX_THREADS = intPreferencesKey("max_threads")
    }

    val romDirectoryUri: Flow<Uri?> = dataStore.data.map { prefs ->
        prefs[KEY_ROM_DIRECTORY_URI]?.let { Uri.parse(it) }
    }

    val ssUsername: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_SS_USERNAME] ?: ""
    }

    val ssPassword: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_SS_PASSWORD] ?: ""
    }

    val isOnboardingCompleted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val maxThreads: Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_MAX_THREADS] ?: 1
    }

    suspend fun saveMaxThreads(threads: Int) {
        dataStore.edit { prefs ->
            prefs[KEY_MAX_THREADS] = threads
        }
    }

    suspend fun saveRomDirectoryUri(uri: Uri) {
        dataStore.edit { prefs ->
            prefs[KEY_ROM_DIRECTORY_URI] = uri.toString()
        }
    }

    suspend fun saveCredentials(username: String, password: String) {
        dataStore.edit { prefs ->
            prefs[KEY_SS_USERNAME] = username
            prefs[KEY_SS_PASSWORD] = password
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun clearCredentials() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_SS_USERNAME)
            prefs.remove(KEY_SS_PASSWORD)
        }
    }

    suspend fun addNotFoundRom(key: String) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_NOT_FOUND_ROMS] ?: emptySet()
            prefs[KEY_NOT_FOUND_ROMS] = current + key
        }
    }

    suspend fun isRomNotFound(key: String): Boolean {
        return dataStore.data.first()[KEY_NOT_FOUND_ROMS]?.contains(key) == true
    }

    suspend fun clearNotFoundRoms() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_NOT_FOUND_ROMS)
        }
    }
}
