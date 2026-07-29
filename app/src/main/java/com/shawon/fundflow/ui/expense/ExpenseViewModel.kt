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

    fun saveExpense() {
        val title = expenseTitle.value
        val amount = expenseAmount.value.toLongOrNull() ?: 0L
        val note = expenseNote.value.takeIf { it.isNotBlank() }
        val categoryId = selectedCategoryId.value
        val timestamp = expenseTimestamp.value

        if (title.isBlank() || amount <= 0) return

        viewModelScope.launch {
            val cycle = repository.getActiveCycle().first()
            if (cycle != null) {
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

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            repository.deleteExpense(id)
        }
    }
}

sealed interface ExpenseNavigation {
    data object Back : ExpenseNavigation
}
