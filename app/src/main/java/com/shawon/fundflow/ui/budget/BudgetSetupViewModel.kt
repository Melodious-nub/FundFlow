package com.shawon.fundflow.ui.budget

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shawon.fundflow.domain.model.BudgetCycle
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
    private val repository: BudgetRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val cycleId: Long? = savedStateHandle.get<Long>("cycleId")

    private val _navigationEvent = MutableSharedFlow<BudgetSetupNavigation>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _previousCycleRemaining = MutableStateFlow<Long?>(null)
    val previousCycleRemaining = _previousCycleRemaining.asStateFlow()

    private val _suggestedStartDate = MutableStateFlow<Long?>(null)
    val suggestedStartDate = _suggestedStartDate.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _existingCycle = MutableStateFlow<BudgetCycle?>(null)
    val existingCycle = _existingCycle.asStateFlow()

    init {
        viewModelScope.launch {
            if (cycleId != null) {
                _existingCycle.value = repository.getCycleById(cycleId)
            }
            
            val allCycles = repository.getAllCycles().first()
            if (allCycles.isNotEmpty()) {
                // If editing, don't carry forward from self. 
                // We want to find the cycle before this one if it's an edit.
                val filteredCycles = if (cycleId != null) {
                    allCycles.filter { it.id != cycleId }
                } else {
                    allCycles
                }
                
                if (filteredCycles.isNotEmpty()) {
                    val lastCycle = filteredCycles.first() // Sorted by date desc
                    
                    // Suggest start date as the day after last cycle ended
                    _suggestedStartDate.value = lastCycle.endDate + (24 * 60 * 60 * 1000)
                    
                    val expenses = repository.getExpensesForCycle(lastCycle.id).first()
                    val spent = expenses.sumOf { it.amount }
                    val remaining = (lastCycle.baseAmount + lastCycle.carryForward) - spent
                    _previousCycleRemaining.value = remaining
                }
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
            val allCycles = repository.getAllCycles().first()
            val overlappingCycle = allCycles.find { existing ->
                if (cycleId != null && existing.id == cycleId) return@find false
                // Check if new range overlaps with any existing range
                // (StartA <= EndB) and (EndA >= StartB)
                startDate <= existing.endDate && endDate >= existing.startDate
            }

            if (overlappingCycle != null) {
                _errorMessage.value = "This period overlaps with cycle '${overlappingCycle.name}'. Please choose different dates."
                return@launch
            }

            // Automatically close ALL previous active cycles if creating a new one
            if (cycleId == null) {
                repository.closeAllActiveCycles()
            }

            val cycle = BudgetCycle(
                id = cycleId ?: 0,
                name = name,
                startDate = startDate,
                endDate = endDate,
                baseAmount = amount,
                carryForward = carryForward,
                isClosed = _existingCycle.value?.isClosed ?: false
            )
            repository.createCycle(cycle)
            _navigationEvent.emit(BudgetSetupNavigation.ToDashboard)
        }
    }

    fun deleteCycle() {
        cycleId?.let { id ->
            viewModelScope.launch {
                repository.deleteCycle(id)
                _navigationEvent.emit(BudgetSetupNavigation.ToDashboard)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

sealed interface BudgetSetupNavigation {
    data object ToDashboard : BudgetSetupNavigation
}
