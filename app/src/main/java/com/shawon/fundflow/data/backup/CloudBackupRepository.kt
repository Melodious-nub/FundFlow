package com.shawon.fundflow.data.backup

import com.shawon.fundflow.data.local.UserPreferences
import com.shawon.fundflow.data.remote.google.GoogleDriveManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudBackupRepository @Inject constructor(
    private val googleDriveManager: GoogleDriveManager,
    private val backupRepository: BackupRepository,
    private val userPreferences: UserPreferences,
    private val serializer: BackupSerializer
) {
    suspend fun backupToCloud(): Result<Unit> {
        return runCatching {
            val data = backupRepository.createBackup()
            val jsonString = serializer.serialize(data)
            googleDriveManager.uploadBackup(jsonString).getOrThrow()
            
            userPreferences.updateCloudBackupTime(System.currentTimeMillis())
        }
    }

    suspend fun restoreFromCloud(): Result<Unit> {
        return runCatching {
            val jsonString = googleDriveManager.downloadBackup().getOrThrow()
            backupRepository.restoreFromJson(jsonString)
        }
    }
}
