package com.shawon.fundflow.data.repository

import com.shawon.fundflow.data.local.dao.BudgetDao
import com.shawon.fundflow.data.local.entities.BudgetCycleEntity
import com.shawon.fundflow.data.local.entities.CategoryEntity
import com.shawon.fundflow.data.local.entities.ExpenseEntity
import com.shawon.fundflow.domain.model.BudgetCycle
import com.shawon.fundflow.domain.model.Expense
import com.shawon.fundflow.domain.repository.BudgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
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

    override suspend fun getCycleById(cycleId: Long): BudgetCycle? = withContext(Dispatchers.IO) {
        budgetDao.getCycleById(cycleId)?.toDomain()
    }

    override fun getExpensesForCycle(cycleId: Long): Flow<List<Expense>> {
        return budgetDao.getExpensesForCycle(cycleId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getExpenseById(expenseId: Long): Expense? = withContext(Dispatchers.IO) {
        budgetDao.getExpenseById(expenseId)?.toDomain()
    }

    override suspend fun addExpense(expense: Expense) = withContext(Dispatchers.IO) {
        budgetDao.insertExpense(expense.toEntity())
    }

    override suspend fun deleteExpense(expenseId: Long) = withContext(Dispatchers.IO) {
        budgetDao.deleteExpense(expenseId)
    }

    override suspend fun createCycle(cycle: BudgetCycle) = withContext(Dispatchers.IO) {
        budgetDao.insertCycle(cycle.toEntity())
        Unit
    }

    override suspend fun closeCycle(cycleId: Long) = withContext(Dispatchers.IO) {
        budgetDao.closeCycle(cycleId)
    }

    override suspend fun closeAllActiveCycles() = withContext(Dispatchers.IO) {
        budgetDao.closeAllActiveCycles()
    }

    override suspend fun deleteCycle(cycleId: Long) = withContext(Dispatchers.IO) {
        budgetDao.deleteCycle(cycleId)
    }

    override fun getAllExpenses(): Flow<List<ExpenseEntity>> {
        return budgetDao.getAllExpenses()
    }

    override suspend fun insertExpenses(expenses: List<ExpenseEntity>) = withContext(Dispatchers.IO) {
        budgetDao.insertExpenses(expenses)
    }

    override suspend fun insertCycles(cycles: List<BudgetCycleEntity>) = withContext(Dispatchers.IO) {
        budgetDao.insertCycles(cycles)
    }

    override suspend fun deleteAllData() = withContext(Dispatchers.IO) {
        budgetDao.deleteAllExpenses()
        budgetDao.deleteAllCycles()
        budgetDao.deleteAllCategories()
    }

    override suspend fun insertCategories(categories: List<CategoryEntity>) = withContext(Dispatchers.IO) {
        budgetDao.insertCategories(categories)
    }

    override fun getAllCategories(): Flow<List<CategoryEntity>> {
        return budgetDao.getAllCategories()
    }

    override suspend fun addCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        budgetDao.insertCategory(category)
    }

    override suspend fun deleteCategory(categoryId: Long) = withContext(Dispatchers.IO) {
        budgetDao.deleteCategory(categoryId)
    }

    override suspend fun seedDefaultCategories() = withContext(Dispatchers.IO) {
        val existing = budgetDao.getAllCategories().first()

        val defaults = listOf(
            CategoryEntity(name = "Food", iconRes = 0, colorHex = "#FF5722", orderIndex = 0),
            CategoryEntity(name = "Transport", iconRes = 0, colorHex = "#2196F3", orderIndex = 1),
            CategoryEntity(name = "Shopping", iconRes = 0, colorHex = "#E91E63", orderIndex = 2),
            CategoryEntity(name = "Entertainment", iconRes = 0, colorHex = "#9C27B0", orderIndex = 3),
            CategoryEntity(name = "Rent", iconRes = 0, colorHex = "#795548", orderIndex = 4),
            CategoryEntity(name = "Bills", iconRes = 0, colorHex = "#F44336", orderIndex = 5),
            CategoryEntity(name = "Utilities", iconRes = 0, colorHex = "#4CAF50", orderIndex = 6),
            CategoryEntity(name = "Healthcare", iconRes = 0, colorHex = "#00BCD4", orderIndex = 7),
            CategoryEntity(name = "Subscriptions", iconRes = 0, colorHex = "#607D8B", orderIndex = 8)
        )

        if (existing.isEmpty()) {
            budgetDao.insertCategories(defaults)
        } else {
            val existingNames = existing.map { it.name.lowercase() }
            val missing = defaults.filter { it.name.lowercase() !in existingNames }
            if (missing.isNotEmpty()) {
                budgetDao.insertCategories(missing)
            }
        }
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

private fun com.shawon.fundflow.data.local.entities.ExpenseWithCategory.toDomain() = Expense(
    id = expense.id,
    cycleId = expense.cycleId,
    categoryId = expense.categoryId,
    title = expense.title,
    amount = expense.amount,
    timestamp = expense.timestamp,
    note = expense.note,
    paymentMethod = expense.paymentMethod,
    categoryName = category?.name,
    categoryColor = category?.colorHex
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
