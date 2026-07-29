package com.shawon.fundflow.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shawon.fundflow.domain.model.Expense
import com.shawon.fundflow.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val expenses: StateFlow<List<Expense>> = repository.getActiveCycle()
        .flatMapLatest { cycle ->
            if (cycle == null) flowOf(emptyList())
            else repository.getExpensesForCycle(cycle.id)
        }
        .combine(_searchQuery) { expenses, query ->
            if (query.isBlank()) expenses
            else expenses.filter { it.title.contains(query, ignoreCase = true) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    private var lastDeletedExpense: Expense? = null

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            lastDeletedExpense = expense
            repository.deleteExpense(expense.id)
            _snackbarEvent.emit("Expense deleted")
        }
    }

    fun undoDelete() {
        lastDeletedExpense?.let { expense ->
            viewModelScope.launch {
                repository.addExpense(expense)
                lastDeletedExpense = null
            }
        }
    }
}
