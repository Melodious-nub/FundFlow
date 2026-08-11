package com.shawon.fundflow.domain.repository

import com.shawon.fundflow.domain.model.BudgetCycle
import com.shawon.fundflow.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getActiveCycle(): Flow<BudgetCycle?>
    fun getAllCycles(): Flow<List<BudgetCycle>>
    suspend fun getCycleById(cycleId: Long): BudgetCycle?
    fun getExpensesForCycle(cycleId: Long): Flow<List<Expense>>
    suspend fun getExpenseById(expenseId: Long): Expense?
    suspend fun addExpense(expense: Expense)
    suspend fun deleteExpense(expenseId: Long)
    suspend fun createCycle(cycle: BudgetCycle)
    suspend fun closeCycle(cycleId: Long)
    suspend fun closeAllActiveCycles()
    suspend fun deleteCycle(cycleId: Long)
    
    fun getAllExpenses(): Flow<List<com.shawon.fundflow.data.local.entities.ExpenseEntity>>
    suspend fun insertExpenses(expenses: List<com.shawon.fundflow.data.local.entities.ExpenseEntity>)
    suspend fun insertCycles(cycles: List<com.shawon.fundflow.data.local.entities.BudgetCycleEntity>)
    suspend fun deleteAllData()
    suspend fun insertCategories(categories: List<com.shawon.fundflow.data.local.entities.CategoryEntity>)
    
    fun getAllCategories(): Flow<List<com.shawon.fundflow.data.local.entities.CategoryEntity>>
    suspend fun addCategory(category: com.shawon.fundflow.data.local.entities.CategoryEntity)
    suspend fun deleteCategory(categoryId: Long)
    suspend fun seedDefaultCategories()
}
