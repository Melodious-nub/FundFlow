package com.shawon.fundflow.ui.cycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shawon.fundflow.domain.model.BudgetCycle
import com.shawon.fundflow.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CycleManagementViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    val allCycles: StateFlow<List<BudgetCycle>> = repository.getAllCycles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteCycle(cycleId: Long) {
        viewModelScope.launch {
            repository.deleteCycle(cycleId)
        }
    }
}
