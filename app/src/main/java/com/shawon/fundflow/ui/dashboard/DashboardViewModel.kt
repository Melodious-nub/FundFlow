package com.shawon.fundflow.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shawon.fundflow.domain.model.BudgetCycle
import com.shawon.fundflow.domain.model.Expense
import com.shawon.fundflow.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = repository.getActiveCycle()
        .flatMapLatest { cycle ->
            if (cycle == null) {
                flowOf(DashboardUiState.NoActiveCycle)
            } else {
                repository.getExpensesForCycle(cycle.id).map { expenses ->
                    calculateDashboardData(cycle, expenses)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState.Loading
        )

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    private var lastDeletedExpense: Expense? = null

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

    private fun calculateDashboardData(cycle: BudgetCycle, expenses: List<Expense>): DashboardUiState.Success {
        val totalSpent = expenses.sumOf { it.amount }
        val currentBalance = cycle.totalBudget - totalSpent
        
        val now = System.currentTimeMillis()
        val remainingMillis = cycle.endDate - now
        val remainingDays = (remainingMillis / (24 * 60 * 60 * 1000)).coerceAtLeast(1)
        
        val dailySafeSpending = if (remainingDays > 0) currentBalance / remainingDays else 0L
        
        val progress = if (cycle.totalBudget > 0) totalSpent.toFloat() / cycle.totalBudget else 0f
        
        val todayStart = now - (now % (24 * 60 * 60 * 1000))
        val todaySpent = expenses.filter { it.timestamp >= todayStart }.sumOf { it.amount }

        return DashboardUiState.Success(
            cycle = cycle,
            currentBalance = currentBalance,
            todaySpent = todaySpent,
            remainingDays = remainingDays,
            dailySafeSpending = dailySafeSpending,
            progress = progress,
            recentExpenses = expenses.take(10)
        )
    }
}

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data object NoActiveCycle : DashboardUiState
    data class Success(
        val cycle: BudgetCycle,
        val currentBalance: Long,
        val todaySpent: Long,
        val remainingDays: Long,
        val dailySafeSpending: Long,
        val progress: Float,
        val recentExpenses: List<Expense>
    ) : DashboardUiState
}
