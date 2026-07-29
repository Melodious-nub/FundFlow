package com.shawon.fundflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shawon.fundflow.data.local.dao.BudgetDao
import com.shawon.fundflow.data.local.entities.BudgetCycleEntity
import com.shawon.fundflow.data.local.entities.CategoryEntity
import com.shawon.fundflow.data.local.entities.ExpenseEntity

@Database(
    entities = [
        BudgetCycleEntity::class,
        CategoryEntity::class,
        ExpenseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FundFlowDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao
}
