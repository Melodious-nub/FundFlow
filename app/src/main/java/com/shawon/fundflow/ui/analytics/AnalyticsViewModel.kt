package com.shawon.fundflow.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shawon.fundflow.domain.model.BudgetCycle
import com.shawon.fundflow.domain.model.Expense
import com.shawon.fundflow.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    private val selectedCycleId = MutableStateFlow<Long?>(null)

    init {
        // Automatically reset selection when a new cycle is created/becomes active
        viewModelScope.launch {
            repository.getActiveCycle().collect { 
                selectedCycleId.value = null
            }
        }
    }

    val allCycles: StateFlow<List<BudgetCycle>> = repository.getAllCycles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allCategories = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val analyticsState: StateFlow<AnalyticsUiState> = combine(
        repository.getActiveCycle(),
        selectedCycleId,
        allCategories,
        allCycles
    ) { active, selectedId, categories, cycles ->
        val targetId = selectedId ?: active?.id ?: cycles.firstOrNull()?.id
        val targetCycle = cycles.find { it.id == targetId }
        DataModel(targetId, categories, targetCycle)
    }.flatMapLatest { model ->
        if (model.targetId == null) flowOf(AnalyticsUiState.NoData(""))
        else repository.getExpensesForCycle(model.targetId).map { expenses ->
            processAnalytics(expenses, model.categories, model.targetCycle)
        }
    }.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState.Loading
    )

    private data class DataModel(
        val targetId: Long?,
        val categories: List<com.shawon.fundflow.data.local.entities.CategoryEntity>,
        val targetCycle: BudgetCycle?
    )

    fun onCycleSelected(id: Long) {
        selectedCycleId.value = id
    }

    private fun processAnalytics(
        expenses: List<Expense>,
        categories: List<com.shawon.fundflow.data.local.entities.CategoryEntity>,
        cycle: BudgetCycle?
    ): AnalyticsUiState {
        if (cycle == null) return AnalyticsUiState.NoData("No Cycle Selected")
        if (expenses.isEmpty()) return AnalyticsUiState.NoData(cycle.name)

        val categoryMap = categories.associateBy { it.id }
        
        val categoryBreakdownMap = expenses.groupBy { it.categoryId ?: -1L }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
        
        val totalSpent = expenses.sumOf { it.amount }
        
        val categoryBreakdown = categoryBreakdownMap.asSequence().map { (catId, amount) ->
            val category = categoryMap[catId]
            CategoryAnalytics(
                categoryId = catId,
                categoryName = category?.name ?: "General",
                amount = amount,
                percentage = if (totalSpent > 0) amount.toFloat() / totalSpent else 0f,
                color = category?.colorHex ?: "#9E9E9E"
            )
        }.sortedByDescending { it.amount }.toList()

        val dailyTrend = expenses.groupBy { 
            val cal = java.util.Calendar.getInstance().apply { 
                timeInMillis = it.timestamp
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        }.mapValues { it.value.sumOf { exp -> exp.amount } }
        .toSortedMap()

        // Calculate Average per day
        val now = System.currentTimeMillis()
        val cycleStart = cycle.startDate
        val daysElapsed = ((minOf(now, cycle.endDate) - cycleStart) / (24 * 60 * 60 * 1000)).coerceAtLeast(1L)
        val averagePerDay = totalSpent / daysElapsed

        // Top Spending Day
        val topSpendingDay = dailyTrend.maxByOrNull { it.value }?.let { entry ->
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            sdf.format(java.util.Date(entry.key)) to entry.value
        }

        return AnalyticsUiState.Success(
            cycleName = cycle.name,
            totalSpent = totalSpent,
            averagePerDay = averagePerDay,
            topSpendingDay = topSpendingDay,
            categoryBreakdown = categoryBreakdown,
            dailyTrend = dailyTrend
        )
    }
}

data class CategoryAnalytics(
    val categoryId: Long,
    val categoryName: String,
    val amount: Long,
    val percentage: Float,
    val color: String
)

sealed interface AnalyticsUiState {
    data object Loading : AnalyticsUiState
    data class NoData(val cycleName: String) : AnalyticsUiState
    data class Success(
        val cycleName: String,
        val totalSpent: Long,
        val averagePerDay: Long,
        val topSpendingDay: Pair<String, Long>?,
        val categoryBreakdown: List<CategoryAnalytics>,
        val dailyTrend: Map<Long, Long>
    ) : AnalyticsUiState
}
