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
    private val LAST_AUTO_BACKUP_TIME = stringPreferencesKey("last_auto_backup_time")
    private val LAST_UPDATE_CHECK_TIME = stringPreferencesKey("last_update_check_time")
    private val INSTALLED_VERSION_NAME = stringPreferencesKey("installed_version_name")
    private val LAST_APP_UPDATE_TIME = stringPreferencesKey("last_app_update_time")
    private val LATEST_RELEASE_TAG = stringPreferencesKey("latest_release_tag")
    private val LATEST_RELEASE_BODY = stringPreferencesKey("latest_release_body")
    private val LATEST_RELEASE_APK_URL = stringPreferencesKey("latest_release_apk_url")
    private val LATEST_RELEASE_APK_NAME = stringPreferencesKey("latest_release_apk_name")

    private val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
    private val AUTO_BACKUP_TARGET = stringPreferencesKey("auto_backup_target")
    private val BACKUP_NETWORK_TYPE = stringPreferencesKey("backup_network_type")
    private val BACKUP_FREQUENCY = stringPreferencesKey("backup_frequency")

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

    val lastAutoBackupTime: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_AUTO_BACKUP_TIME]?.toLongOrNull() ?: 0L
        }

    suspend fun updateAutoBackupTime(time: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_AUTO_BACKUP_TIME] = time.toString()
        }
    }

    val lastUpdateCheckTime: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_UPDATE_CHECK_TIME]?.toLongOrNull() ?: 0L
        }

    suspend fun updateLastUpdateCheckTime(time: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_UPDATE_CHECK_TIME] = time.toString()
        }
    }

    val lastAppUpdateTime: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_APP_UPDATE_TIME]?.toLongOrNull() ?: 0L
        }

    suspend fun checkAndTrackVersionChange(currentVersion: String) {
        context.dataStore.edit { preferences ->
            val lastSavedVersion = preferences[INSTALLED_VERSION_NAME]
            if (lastSavedVersion != null && lastSavedVersion != currentVersion) {
                preferences[LAST_APP_UPDATE_TIME] = System.currentTimeMillis().toString()
                // Clear old release info if app was actually updated
                preferences.remove(LATEST_RELEASE_TAG)
                preferences.remove(LATEST_RELEASE_BODY)
                preferences.remove(LATEST_RELEASE_APK_URL)
                preferences.remove(LATEST_RELEASE_APK_NAME)
            }
            preferences[INSTALLED_VERSION_NAME] = currentVersion
        }
    }

    val latestReleaseInfo: Flow<Map<String, String>> = context.dataStore.data
        .map { preferences ->
            mapOf(
                "tag" to (preferences[LATEST_RELEASE_TAG] ?: ""),
                "body" to (preferences[LATEST_RELEASE_BODY] ?: ""),
                "url" to (preferences[LATEST_RELEASE_APK_URL] ?: ""),
                "name" to (preferences[LATEST_RELEASE_APK_NAME] ?: "")
            )
        }

    suspend fun saveLatestRelease(tag: String, body: String, url: String, name: String) {
        context.dataStore.edit { preferences ->
            preferences[LATEST_RELEASE_TAG] = tag
            preferences[LATEST_RELEASE_BODY] = body
            preferences[LATEST_RELEASE_APK_URL] = url
            preferences[LATEST_RELEASE_APK_NAME] = name
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

    val themeMode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_MODE] ?: "SYSTEM"
        }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    val autoBackupSettings: Flow<AutoBackupSettings> = context.dataStore.data
        .map { preferences ->
            AutoBackupSettings(
                enabled = preferences[AUTO_BACKUP_ENABLED] ?: false,
                target = preferences[AUTO_BACKUP_TARGET] ?: "BOTH",
                networkType = preferences[BACKUP_NETWORK_TYPE] ?: "BOTH",
                frequency = preferences[BACKUP_FREQUENCY] ?: "DAILY"
            )
        }

    suspend fun updateAutoBackupSettings(settings: AutoBackupSettings) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_BACKUP_ENABLED] = settings.enabled
            preferences[AUTO_BACKUP_TARGET] = settings.target
            preferences[BACKUP_NETWORK_TYPE] = settings.networkType
            preferences[BACKUP_FREQUENCY] = settings.frequency
        }
    }
}

data class AutoBackupSettings(
    val enabled: Boolean,
    val target: String, // LOCAL, CLOUD, BOTH
    val networkType: String, // WIFI, CELLULAR, BOTH
    val frequency: String // DAILY, WEEKLY, 15_DAYS, MONTHLY
)
