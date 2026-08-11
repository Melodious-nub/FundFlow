package com.shawon.fundflow.ui.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shawon.fundflow.core.auth.GoogleAuthManager
import com.shawon.fundflow.data.backup.CloudBackupRepository
import com.shawon.fundflow.data.local.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CloudBackupViewModel @Inject constructor(
    private val cloudBackupRepository: CloudBackupRepository,
    private val googleAuthManager: GoogleAuthManager,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(CloudBackupUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<CloudBackupEvent>()
    val event = _event.asSharedFlow()

    val lastBackupTime = userPreferences.lastCloudBackupTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    init {
        checkUserStatus()
    }

    fun checkUserStatus() {
        val account = googleAuthManager.getLastSignedInAccount()
        _uiState.value = _uiState.value.copy(
            isSignedIn = account != null,
            userEmail = account?.email
        )
    }

    fun getSignInIntent() = googleAuthManager.getSignInIntent()

    fun onSignInResult(success: Boolean) {
        if (success) {
            checkUserStatus()
        } else {
            viewModelScope.launch {
                _event.emit(CloudBackupEvent.ShowMessage("Google Sign-In failed"))
            }
        }
    }

    fun signOut() {
        googleAuthManager.signOut {
            checkUserStatus()
        }
    }

    fun backupToCloud() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            cloudBackupRepository.backupToCloud()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _event.emit(CloudBackupEvent.ShowMessage("Cloud backup successful"))
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _event.emit(CloudBackupEvent.ShowMessage("Backup failed: ${error.message}"))
                }
        }
    }

    fun restoreFromCloud() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            cloudBackupRepository.restoreFromCloud()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _event.emit(CloudBackupEvent.RestoreSuccess)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _event.emit(CloudBackupEvent.ShowMessage("Restore failed: ${error.message}"))
                }
        }
    }
}

data class CloudBackupUiState(
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val userEmail: String? = null
)

sealed interface CloudBackupEvent {
    data class ShowMessage(val message: String) : CloudBackupEvent
    data object RestoreSuccess : CloudBackupEvent
}
