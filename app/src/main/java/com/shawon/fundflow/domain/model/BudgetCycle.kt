package com.shawon.fundflow.domain.model

data class BudgetCycle(
    val id: Long,
    val name: String,
    val startDate: Long,
    val endDate: Long,
    val baseAmount: Long,
    val carryForward: Long,
    val isClosed: Boolean
) {
    val totalBudget: Long get() = baseAmount + carryForward
}
