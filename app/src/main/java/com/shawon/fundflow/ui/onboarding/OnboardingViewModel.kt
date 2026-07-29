package com.shawon.fundflow.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shawon.fundflow.data.local.UserPreferences
import com.shawon.fundflow.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val repository: BudgetRepository
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<OnboardingNavigation>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.seedDefaultCategories()
            userPreferences.setOnboardingCompleted(true)
            _navigationEvent.emit(OnboardingNavigation.ToBudgetSetup)
        }
    }
}

sealed interface OnboardingNavigation {
    data object ToBudgetSetup : OnboardingNavigation
}
