package com.shawon.fundflow.ui.backup

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
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

    fun onSignInResult(data: Intent?) {
        viewModelScope.launch {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                task.getResult(ApiException::class.java)
                checkUserStatus()
                _event.emit(CloudBackupEvent.ShowMessage("Signed in successfully"))
            } catch (e: ApiException) {
                val message = when (e.statusCode) {
                    7 -> "Network Error (Status: 7)"
                    10 -> "Developer Error: SHA-1 or Package name mismatch (Status: 10)"
                    12500 -> "Sign-in Failed: Check API configuration (Status: 12500)"
                    12501 -> "Sign-in Cancelled"
                    else -> "Sign-in Failed (Status: ${e.statusCode})"
                }
                _event.emit(CloudBackupEvent.ShowMessage(message))
            } catch (e: Exception) {
                _event.emit(CloudBackupEvent.ShowMessage("An unexpected error occurred: ${e.message}"))
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
