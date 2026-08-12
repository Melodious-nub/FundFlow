package com.shawon.fundflow.core.common

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Splash : Screen

    @Serializable
    data object Onboarding : Screen

    @Serializable
    data class BudgetSetup(val cycleId: Long? = null) : Screen

    @Serializable
    data object Dashboard : Screen

    @Serializable
    data object ExpenseForm : Screen {
        // Can add arguments here later if needed (e.g., expenseId for edit)
    }

    @Serializable
    data object Analytics : Screen

    @Serializable
    data object History : Screen

    @Serializable
    data object CycleManagement : Screen

    @Serializable
    data class Backup(val isInitialSetup: Boolean = false) : Screen

    @Serializable
    data class LocalBackup(val isInitialSetup: Boolean = false) : Screen

    @Serializable
    data class CloudBackup(val isInitialSetup: Boolean = false) : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data object AppUpdate : Screen

    @Serializable
    data object About : Screen
}
