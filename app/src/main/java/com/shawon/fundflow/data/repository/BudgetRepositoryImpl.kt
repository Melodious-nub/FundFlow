package com.shawon.fundflow.data.repository

import com.shawon.fundflow.data.local.dao.BudgetDao
import com.shawon.fundflow.data.local.entities.BudgetCycleEntity
import com.shawon.fundflow.data.local.entities.CategoryEntity
import com.shawon.fundflow.data.local.entities.ExpenseEntity
import com.shawon.fundflow.domain.model.BudgetCycle
import com.shawon.fundflow.domain.model.Expense
import com.shawon.fundflow.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun getActiveCycle(): Flow<BudgetCycle?> {
        return budgetDao.getActiveCycle().map { it?.toDomain() }
    }

    override fun getAllCycles(): Flow<List<BudgetCycle>> {
        return budgetDao.getAllCycles().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getExpensesForCycle(cycleId: Long): Flow<List<Expense>> {
        return budgetDao.getExpensesForCycle(cycleId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getExpenseById(expenseId: Long): Expense? {
        return budgetDao.getExpenseById(expenseId)?.toDomain()
    }

    override suspend fun addExpense(expense: Expense) {
        budgetDao.insertExpense(expense.toEntity())
    }

    override suspend fun deleteExpense(expenseId: Long) {
        budgetDao.deleteExpense(expenseId)
    }

    override suspend fun createCycle(cycle: BudgetCycle) {
        budgetDao.insertCycle(cycle.toEntity())
    }

    override suspend fun closeCycle(cycleId: Long) {
        budgetDao.closeCycle(cycleId)
    }

    override fun getAllCategories(): Flow<List<CategoryEntity>> {
        return budgetDao.getAllCategories()
    }

    override suspend fun addCategory(category: CategoryEntity) {
        budgetDao.insertCategory(category)
    }

    override suspend fun deleteCategory(categoryId: Long) {
        budgetDao.deleteCategory(categoryId)
    }

    override suspend fun seedDefaultCategories() {
        val defaults = listOf(
            CategoryEntity(name = "Food", iconRes = 0, colorHex = "#FF5722", orderIndex = 0),
            CategoryEntity(name = "Transport", iconRes = 0, colorHex = "#2196F3", orderIndex = 1),
            CategoryEntity(name = "Shopping", iconRes = 0, colorHex = "#E91E63", orderIndex = 2),
            CategoryEntity(name = "Entertainment", iconRes = 0, colorHex = "#9C27B0", orderIndex = 3),
            CategoryEntity(name = "Bills", iconRes = 0, colorHex = "#F44336", orderIndex = 4)
        )
        budgetDao.insertCategories(defaults)
    }
}

// Mappers
private fun BudgetCycleEntity.toDomain() = BudgetCycle(
    id = id,
    name = name,
    startDate = startDate,
    endDate = endDate,
    baseAmount = baseAmount,
    carryForward = carryForward,
    isClosed = isClosed
)

private fun BudgetCycle.toEntity() = BudgetCycleEntity(
    id = id,
    name = name,
    startDate = startDate,
    endDate = endDate,
    baseAmount = baseAmount,
    carryForward = carryForward,
    isClosed = isClosed
)

private fun ExpenseEntity.toDomain() = Expense(
    id = id,
    cycleId = cycleId,
    categoryId = categoryId,
    title = title,
    amount = amount,
    timestamp = timestamp,
    note = note,
    paymentMethod = paymentMethod
)

private fun Expense.toEntity() = ExpenseEntity(
    id = id,
    cycleId = cycleId,
    categoryId = categoryId,
    title = title,
    amount = amount,
    timestamp = timestamp,
    note = note,
    paymentMethod = paymentMethod
)
