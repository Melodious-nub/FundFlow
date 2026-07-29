package com.shawon.fundflow.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shawon.fundflow.domain.model.BudgetCycle
import com.shawon.fundflow.domain.model.Expense
import com.shawon.fundflow.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    private val selectedCycleId = MutableStateFlow<Long?>(null)

    val allCycles: StateFlow<List<BudgetCycle>> = repository.getAllCycles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val analyticsState: StateFlow<AnalyticsUiState> = combine(
        repository.getActiveCycle(),
        selectedCycleId
    ) { active, selectedId ->
        selectedId ?: active?.id
    }.flatMapLatest { cycleId ->
        if (cycleId == null) flowOf(AnalyticsUiState.NoData)
        else repository.getExpensesForCycle(cycleId).map { expenses ->
            if (expenses.isEmpty()) AnalyticsUiState.NoData
            else processAnalytics(expenses)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState.Loading
    )

    fun onCycleSelected(id: Long) {
        selectedCycleId.value = id
    }

    private fun processAnalytics(expenses: List<Expense>): AnalyticsUiState.Success {
        val categoryBreakdown = expenses.groupBy { it.categoryId ?: -1L }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
        
        val weeklyData = expenses.groupBy { 
            val date = java.util.Calendar.getInstance().apply { timeInMillis = it.timestamp }
            date.get(java.util.Calendar.DAY_OF_WEEK)
        }.mapValues { it.value.sumOf { exp -> exp.amount } }
        
        val sortedWeeklyData = mutableMapOf<Int, Long>()
        for (i in 1..7) {
            sortedWeeklyData[i] = weeklyData[i] ?: 0L
        }

        return AnalyticsUiState.Success(
            categoryBreakdown = categoryBreakdown,
            weeklyData = sortedWeeklyData
        )
    }
}

sealed interface AnalyticsUiState {
    data object Loading : AnalyticsUiState
    data object NoData : AnalyticsUiState
    data class Success(
        val categoryBreakdown: Map<Long, Long>,
        val weeklyData: Map<Int, Long>
    ) : AnalyticsUiState
}
