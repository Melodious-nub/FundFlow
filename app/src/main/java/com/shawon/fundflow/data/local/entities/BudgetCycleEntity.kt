package com.shawon.fundflow.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "budget_cycles")
data class BudgetCycleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val startDate: Long,
    val endDate: Long,
    val baseAmount: Long, // Smallest unit (e.g., cents)
    val carryForward: Long,
    val isClosed: Boolean = false
)
