package com.shawon.fundflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shawon.fundflow.data.local.entities.BudgetCycleEntity
import com.shawon.fundflow.data.local.entities.CategoryEntity
import com.shawon.fundflow.data.local.entities.ExpenseEntity
import com.shawon.fundflow.data.local.entities.ExpenseWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget_cycles WHERE isClosed = 0 LIMIT 1")
    fun getActiveCycle(): Flow<BudgetCycleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycle(cycle: BudgetCycleEntity): Long

    @Query("SELECT * FROM categories ORDER BY orderIndex ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategory(categoryId: Long)

    @androidx.room.Transaction
    @Query("SELECT * FROM expenses WHERE cycleId = :cycleId ORDER BY timestamp DESC")
    fun getExpensesForCycle(cycleId: Long): Flow<List<ExpenseWithCategory>>

    @androidx.room.Transaction
    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    suspend fun getExpenseById(expenseId: Long): ExpenseWithCategory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpense(expenseId: Long)

    @Query("SELECT * FROM budget_cycles ORDER BY startDate DESC")
    fun getAllCycles(): Flow<List<BudgetCycleEntity>>

    @Query("SELECT * FROM budget_cycles WHERE id = :cycleId")
    suspend fun getCycleById(cycleId: Long): BudgetCycleEntity?

    @Query("UPDATE budget_cycles SET isClosed = 1 WHERE id = :cycleId")
    suspend fun closeCycle(cycleId: Long)

    @Query("UPDATE budget_cycles SET isClosed = 1 WHERE isClosed = 0")
    suspend fun closeAllActiveCycles()

    @Query("DELETE FROM budget_cycles WHERE id = :cycleId")
    suspend fun deleteCycle(cycleId: Long)

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycles(cycles: List<BudgetCycleEntity>)

    @Query("DELETE FROM budget_cycles")
    suspend fun deleteAllCycles()

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
}
