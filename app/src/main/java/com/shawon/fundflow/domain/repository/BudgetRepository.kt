package com.shawon.fundflow.domain.repository

import com.shawon.fundflow.domain.model.BudgetCycle
import com.shawon.fundflow.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getActiveCycle(): Flow<BudgetCycle?>
    fun getAllCycles(): Flow<List<BudgetCycle>>
    fun getExpensesForCycle(cycleId: Long): Flow<List<Expense>>
    suspend fun getExpenseById(expenseId: Long): Expense?
    suspend fun addExpense(expense: Expense)
    suspend fun deleteExpense(expenseId: Long)
    suspend fun createCycle(cycle: BudgetCycle)
    suspend fun closeCycle(cycleId: Long)
    
    fun getAllCategories(): Flow<List<com.shawon.fundflow.data.local.entities.CategoryEntity>>
    suspend fun addCategory(category: com.shawon.fundflow.data.local.entities.CategoryEntity)
    suspend fun deleteCategory(categoryId: Long)
    suspend fun seedDefaultCategories()
}
