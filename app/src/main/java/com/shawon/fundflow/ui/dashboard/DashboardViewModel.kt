package com.shawon.fundflow.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shawon.fundflow.domain.model.BudgetCycle
import com.shawon.fundflow.domain.model.Expense
import com.shawon.fundflow.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    private val _selectedCycleId = MutableStateFlow<Long?>(null)
    val selectedCycleId = _selectedCycleId.asStateFlow()

    val allCycles: StateFlow<List<BudgetCycle>> = repository.getAllCycles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getActiveCycle(),
        _selectedCycleId
    ) { active, selectedId ->
        selectedId ?: active?.id
    }.flatMapLatest { cycleId ->
        if (cycleId == null) {
            flowOf(DashboardUiState.NoActiveCycle)
        } else {
            combine(
                repository.getAllCycles().map { cycles -> cycles.find { it.id == cycleId } },
                repository.getExpensesForCycle(cycleId)
            ) { cycle, expenses ->
                if (cycle == null) DashboardUiState.NoActiveCycle
                else calculateDashboardData(cycle, expenses)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState.Loading
    )

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    fun selectCycle(id: Long) {
        _selectedCycleId.value = id
    }

    fun closeCycle(id: Long) {
        viewModelScope.launch {
            repository.closeCycle(id)
        }
    }

    private fun calculateDashboardData(cycle: BudgetCycle, expenses: List<Expense>): DashboardUiState.Success {
        val totalSpent = expenses.sumOf { it.amount }
        val currentBalance = cycle.totalBudget - totalSpent
        
        val now = System.currentTimeMillis()
        val isExpired = now > cycle.endDate
        
        val remainingMillis = cycle.endDate - now
        val remainingDays = (remainingMillis / (24 * 60 * 60 * 1000)).coerceAtLeast(0)
        
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
            recentExpenses = expenses.take(20),
            isExpired = isExpired
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
        val recentExpenses: List<Expense>,
        val isExpired: Boolean
    ) : DashboardUiState
}
