package com.shawon.fundflow.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shawon.fundflow.data.local.dao.BudgetDao
import com.shawon.fundflow.data.local.entities.BudgetCycleEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetSetupViewModel @Inject constructor(
    private val budgetDao: BudgetDao
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<BudgetSetupNavigation>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun saveBudgetCycle(
        name: String,
        startDate: Long,
        endDate: Long,
        amount: Long
    ) {
        viewModelScope.launch {
            val cycle = BudgetCycleEntity(
                name = name,
                startDate = startDate,
                endDate = endDate,
                baseAmount = amount,
                carryForward = 0 // Initial setup
            )
            budgetDao.insertCycle(cycle)
            _navigationEvent.emit(BudgetSetupNavigation.ToDashboard)
        }
    }
}

sealed interface BudgetSetupNavigation {
    data object ToDashboard : BudgetSetupNavigation
}
