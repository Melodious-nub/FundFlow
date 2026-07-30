package com.shawon.fundflow.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shawon.fundflow.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetSetupViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<BudgetSetupNavigation>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _previousCycleRemaining = MutableStateFlow<Long?>(null)
    val previousCycleRemaining = _previousCycleRemaining.asStateFlow()

    init {
        viewModelScope.launch {
            val allCycles = repository.getAllCycles().first()
            if (allCycles.isNotEmpty()) {
                val lastCycle = allCycles.first() // Sorted by date desc
                val expenses = repository.getExpensesForCycle(lastCycle.id).first()
                val spent = expenses.sumOf { it.amount }
                val remaining = (lastCycle.baseAmount + lastCycle.carryForward) - spent
                _previousCycleRemaining.value = remaining
            }
        }
    }

    fun saveBudgetCycle(
        name: String,
        startDate: Long,
        endDate: Long,
        amount: Long,
        carryForward: Long = 0L
    ) {
        viewModelScope.launch {
            val cycle = com.shawon.fundflow.domain.model.BudgetCycle(
                id = 0,
                name = name,
                startDate = startDate,
                endDate = endDate,
                baseAmount = amount,
                carryForward = carryForward,
                isClosed = false
            )
            repository.createCycle(cycle)
            _navigationEvent.emit(BudgetSetupNavigation.ToDashboard)
        }
    }
}

sealed interface BudgetSetupNavigation {
    data object ToDashboard : BudgetSetupNavigation
}
