package com.shawon.fundflow.core.backup

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.shawon.fundflow.data.backup.BackupWorker
import com.shawon.fundflow.data.local.AutoBackupSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleBackup(settings: AutoBackupSettings) {
        Log.d("BackupScheduler", "Scheduling backup: enabled=${settings.enabled}, freq=${settings.frequency}, target=${settings.target}")
        
        if (!settings.enabled) {
            cancelBackup()
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                when (settings.networkType) {
                    "WIFI" -> NetworkType.UNMETERED
                    "CELLULAR" -> NetworkType.CONNECTED
                    else -> NetworkType.CONNECTED
                }
            )
            .build()

        val (repeatInterval, timeUnit) = when (settings.frequency) {
            "DAILY" -> 1L to TimeUnit.DAYS
            "WEEKLY" -> 7L to TimeUnit.DAYS
            "15_DAYS" -> 15L to TimeUnit.DAYS
            "MONTHLY" -> 30L to TimeUnit.DAYS
            else -> 1L to TimeUnit.DAYS
        }

        val workRequest = PeriodicWorkRequestBuilder<BackupWorker>(repeatInterval, timeUnit)
            .setConstraints(constraints)
            .addTag(WORK_NAME)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            workRequest
        )
        Log.d("BackupScheduler", "Work enqueued with CANCEL_AND_REENQUEUE")
    }

    fun cancelBackup() {
        Log.d("BackupScheduler", "Cancelling backup work")
        workManager.cancelUniqueWork(WORK_NAME)
    }

    companion object {
        private const val WORK_NAME = "auto_backup_work"
    }
}
