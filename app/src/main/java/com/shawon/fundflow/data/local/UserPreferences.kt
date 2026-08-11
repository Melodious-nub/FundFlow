package com.shawon.fundflow.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    private val CURRENCY_CODE = stringPreferencesKey("currency_code")
    private val THEME_MODE = stringPreferencesKey("theme_mode")
    private val LAST_BACKUP_TIME = stringPreferencesKey("last_backup_time")
    private val LAST_BACKUP_SIZE = stringPreferencesKey("last_backup_size")
    private val LAST_CLOUD_BACKUP_TIME = stringPreferencesKey("last_cloud_backup_time")

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    val lastBackupInfo: Flow<Pair<Long, Long>> = context.dataStore.data
        .map { preferences ->
            val time = preferences[LAST_BACKUP_TIME]?.toLongOrNull() ?: 0L
            val size = preferences[LAST_BACKUP_SIZE]?.toLongOrNull() ?: 0L
            Pair(time, size)
        }

    suspend fun updateBackupInfo(time: Long, size: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_BACKUP_TIME] = time.toString()
            preferences[LAST_BACKUP_SIZE] = size.toString()
        }
    }

    val lastCloudBackupTime: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_CLOUD_BACKUP_TIME]?.toLongOrNull() ?: 0L
        }

    suspend fun updateCloudBackupTime(time: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_CLOUD_BACKUP_TIME] = time.toString()
        }
    }

    val currencyCode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[CURRENCY_CODE] ?: "TK"
        }

    suspend fun setCurrencyCode(code: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENCY_CODE] = code
        }
    }
}
