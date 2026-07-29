package com.shawon.fundflow.domain.model

data class Expense(
    val id: Long,
    val cycleId: Long,
    val categoryId: Long?,
    val title: String,
    val amount: Long,
    val timestamp: Long,
    val note: String?,
    val paymentMethod: String,
    val categoryName: String? = null,
    val categoryColor: String? = null
)
