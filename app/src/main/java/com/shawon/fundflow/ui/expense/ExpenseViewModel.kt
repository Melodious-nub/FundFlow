package com.shawon.fundflow.ui.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shawon.fundflow.data.local.entities.CategoryEntity
import com.shawon.fundflow.domain.model.Expense
import com.shawon.fundflow.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<ExpenseNavigation>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var editingExpenseId: Long? = null
    
    val expenseTitle = MutableStateFlow("")
    val expenseAmount = MutableStateFlow("")
    val expenseNote = MutableStateFlow("")
    val selectedCategoryId = MutableStateFlow<Long?>(null)
    val expenseTimestamp = MutableStateFlow(System.currentTimeMillis())

    private val _showDeadlineWarning = MutableStateFlow(false)
    val showDeadlineWarning = _showDeadlineWarning.asStateFlow()

    init {
        viewModelScope.launch {
            expenseNote.collect { note ->
                calculateAmountFromNote(note)
            }
        }
    }

    private fun calculateAmountFromNote(note: String) {
        // Regex to match digits within parentheses, e.g., (500)
        val regex = Regex("\\((\\d+)\\)")
        val matches = regex.findAll(note)
        if (matches.any()) {
            val sum = matches.sumOf { it.groupValues[1].toLong() }
            if (sum > 0) {
                expenseAmount.value = sum.toString()
            }
        }
    }

    fun loadExpense(id: Long) {
        viewModelScope.launch {
            editingExpenseId = id
            val expense = repository.getExpenseById(id)
            expense?.let {
                expenseTitle.value = it.title
                expenseAmount.value = it.amount.toString()
                expenseNote.value = it.note ?: ""
                selectedCategoryId.value = it.categoryId
                expenseTimestamp.value = it.timestamp
            }
        }
    }

    fun saveExpense(force: Boolean = false) {
        val title = expenseTitle.value
        val amount = expenseAmount.value.toLongOrNull() ?: 0L
        val note = expenseNote.value.takeIf { it.isNotBlank() }
        val categoryId = selectedCategoryId.value
        val timestamp = expenseTimestamp.value

        if (title.isBlank() || amount <= 0) return

        viewModelScope.launch {
            val cycle = repository.getActiveCycle().first()
            if (cycle != null) {
                // Check if the expense date is past the cycle's deadline
                if (!force && timestamp > cycle.endDate) {
                    _showDeadlineWarning.value = true
                    return@launch
                }

                val expense = Expense(
                    id = editingExpenseId ?: 0L,
                    cycleId = cycle.id,
                    categoryId = categoryId,
                    title = title,
                    amount = amount,
                    timestamp = timestamp,
                    note = note,
                    paymentMethod = "Cash"
                )
                repository.addExpense(expense)
                _navigationEvent.emit(ExpenseNavigation.Back)
            }
        }
    }

    fun dismissWarning() {
        _showDeadlineWarning.value = false
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            repository.deleteExpense(id)
        }
    }
}

sealed interface ExpenseNavigation {
    data object Back : ExpenseNavigation
}
