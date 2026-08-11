package com.shawon.fundflow.data.backup

import android.content.Context
import android.net.Uri
import android.os.Build
import com.shawon.fundflow.data.backup.model.BackupData
import com.shawon.fundflow.data.backup.model.BackupMetadata
import com.shawon.fundflow.data.backup.model.PreferencesBackupData
import com.shawon.fundflow.data.backup.model.RoomBackupData
import com.shawon.fundflow.data.local.UserPreferences
import com.shawon.fundflow.domain.repository.BudgetRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val budgetRepository: BudgetRepository,
    private val userPreferences: UserPreferences,
    private val serializer: BackupSerializer
) {
    private val BACKUP_VERSION = 1

    suspend fun createBackup(): BackupData {
        val metadata = BackupMetadata(
            version = BACKUP_VERSION,
            timestamp = System.currentTimeMillis(),
            appVersion = getAppVersion(),
            deviceName = Build.MODEL
        )

        val cycles = budgetRepository.getAllCycles().first()
        val expenses = budgetRepository.getAllExpenses().first()
        val categories = budgetRepository.getAllCategories().first()

        val roomData = RoomBackupData(
            cycles = cycles.map { it.toEntity() }, // Wait, repo gives domain models?
            expenses = expenses,
            categories = categories
        )

        val preferencesData = PreferencesBackupData(
            currencyCode = userPreferences.currencyCode.first(),
            onboardingCompleted = userPreferences.isOnboardingCompleted.first()
        )

        return BackupData(metadata, roomData, preferencesData)
    }

    suspend fun exportBackup(uri: Uri): Result<Unit> {
        return runCatching {
            val data = createBackup()
            val jsonString = serializer.serialize(data)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonString)
                }
            } ?: throw Exception("Failed to open output stream")
            
            // Update backup info
            val size = jsonString.toByteArray().size.toLong()
            userPreferences.updateBackupInfo(System.currentTimeMillis(), size)
        }
    }

    suspend fun importBackup(uri: Uri): Result<Unit> {
        return runCatching {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
            } ?: throw Exception("Failed to open input stream")

            restoreFromJson(jsonString)
        }
    }

    suspend fun restoreFromJson(jsonString: String) {
        val backupData = serializer.deserialize(jsonString)
        validateBackup(backupData)
        restoreBackup(backupData)
    }

    fun validateBackup(data: BackupData) {
        if (data.metadata.version > BACKUP_VERSION) {
            throw Exception("Backup version ${data.metadata.version} is newer than app version $BACKUP_VERSION")
        }
        // Add more validation if needed (structure, mandatory fields)
    }

    suspend fun restoreBackup(data: BackupData) {
        // 1. Restore Room Data in correct order to respect Foreign Keys
        budgetRepository.deleteAllData()
        budgetRepository.insertCategories(data.roomData.categories)
        budgetRepository.insertCycles(data.roomData.cycles)
        budgetRepository.insertExpenses(data.roomData.expenses)

        // 2. Restore Preferences
        userPreferences.setCurrencyCode(data.preferences.currencyCode)
        userPreferences.setOnboardingCompleted(data.preferences.onboardingCompleted)
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    // Mapper extension for domain to entity conversion if needed
    private fun com.shawon.fundflow.domain.model.BudgetCycle.toEntity() = com.shawon.fundflow.data.local.entities.BudgetCycleEntity(
        id = id,
        name = name,
        startDate = startDate,
        endDate = endDate,
        baseAmount = baseAmount,
        carryForward = carryForward,
        isClosed = isClosed
    )
}
