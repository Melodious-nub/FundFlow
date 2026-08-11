package com.shawon.fundflow.data.backup.model

import com.shawon.fundflow.data.local.entities.BudgetCycleEntity
import com.shawon.fundflow.data.local.entities.CategoryEntity
import com.shawon.fundflow.data.local.entities.ExpenseEntity
import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val metadata: BackupMetadata,
    val roomData: RoomBackupData,
    val preferences: PreferencesBackupData
)

@Serializable
data class BackupMetadata(
    val version: Int,
    val timestamp: Long,
    val appVersion: String,
    val deviceName: String
)

@Serializable
data class RoomBackupData(
    val cycles: List<BudgetCycleEntity>,
    val expenses: List<ExpenseEntity>,
    val categories: List<CategoryEntity>
)

@Serializable
data class PreferencesBackupData(
    val currencyCode: String,
    val onboardingCompleted: Boolean
)
