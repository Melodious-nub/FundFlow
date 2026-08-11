package com.shawon.fundflow.ui.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shawon.fundflow.data.backup.BackupRepository
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
class BackupViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<BackupEvent>()
    val event = _event.asSharedFlow()

    val backupInfo = userPreferences.lastBackupInfo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0L, 0L))

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            backupRepository.exportBackup(uri)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _event.emit(BackupEvent.ShowMessage("Backup exported successfully"))
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _event.emit(BackupEvent.ShowMessage("Export failed: ${error.message}"))
                }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            backupRepository.importBackup(uri)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _event.emit(BackupEvent.RestoreSuccess)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _event.emit(BackupEvent.ShowMessage("Restore failed: ${error.message}"))
                }
        }
    }
}

data class BackupUiState(
    val isLoading: Boolean = false
)

sealed interface BackupEvent {
    data class ShowMessage(val message: String) : BackupEvent
    data object RestoreSuccess : BackupEvent
}
