package com.shawon.fundflow.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shawon.fundflow.data.local.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<SplashNavigation>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            delay(2000)
            val isOnboardingCompleted = userPreferences.isOnboardingCompleted.first()
            if (isOnboardingCompleted) {
                _navigationEvent.emit(SplashNavigation.ToDashboard)
            } else {
                _navigationEvent.emit(SplashNavigation.ToOnboarding)
            }
        }
    }
}

sealed interface SplashNavigation {
    data object ToOnboarding : SplashNavigation
    data object ToDashboard : SplashNavigation
}
