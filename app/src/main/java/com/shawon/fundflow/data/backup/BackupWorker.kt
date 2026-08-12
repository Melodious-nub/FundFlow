package com.shawon.fundflow.data.backup

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shawon.fundflow.core.notifications.NotificationHelper
import com.shawon.fundflow.data.local.UserPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val backupRepository: BackupRepository,
    private val cloudBackupRepository: CloudBackupRepository,
    private val userPreferences: UserPreferences,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("BackupWorker", "Worker started")
        val settings = userPreferences.autoBackupSettings.first()
        Log.d("BackupWorker", "Settings: enabled=${settings.enabled}, target=${settings.target}")
        
        if (!settings.enabled) {
            Log.d("BackupWorker", "Auto backup disabled, skipping")
            return Result.success()
        }

        return try {
            when (settings.target) {
                "LOCAL" -> {
                    Log.d("BackupWorker", "Starting LOCAL backup")
                    backupRepository.autoExportBackup().getOrThrow()
                }
                "CLOUD" -> {
                    Log.d("BackupWorker", "Starting CLOUD backup")
                    cloudBackupRepository.backupToCloud().getOrThrow()
                }
                "BOTH" -> {
                    Log.d("BackupWorker", "Starting BOTH (Local + Cloud) backup")
                    backupRepository.autoExportBackup().getOrThrow()
                    cloudBackupRepository.backupToCloud().getOrThrow()
                }
            }
            Log.d("BackupWorker", "Backup successful, updating time")
            userPreferences.updateAutoBackupTime(System.currentTimeMillis())
            Result.success()
        } catch (e: Exception) {
            Log.e("BackupWorker", "Backup failed: ${e.message}", e)
            if (runAttemptCount < 3) {
                Log.d("BackupWorker", "Retrying... attempt $runAttemptCount")
                Result.retry()
            } else {
                Log.e("BackupWorker", "Max retries reached, giving up")
                notificationHelper.showBackupFailedNotification(e.message ?: "Critical failure during automatic backup.")
                Result.failure()
            }
        }
    }
}
