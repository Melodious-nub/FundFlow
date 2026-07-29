package com.shawon.fundflow.core.common

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Splash : Screen

    @Serializable
    data object Onboarding : Screen

    @Serializable
    data object BudgetSetup : Screen

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
    data object Settings : Screen
}
