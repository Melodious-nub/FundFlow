package com.shawon.fundflow.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shawon.fundflow.core.update.UpdateManager
import com.shawon.fundflow.data.local.UserPreferences
import com.shawon.fundflow.data.remote.model.GitHubRelease
import com.shawon.fundflow.data.repository.UpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val repository: UpdateRepository,
    private val updateManager: UpdateManager,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val uiState = _uiState.asStateFlow()

    val lastCheckTime = userPreferences.lastUpdateCheckTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val lastUpdateTime = userPreferences.lastAppUpdateTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun checkForUpdates(currentVersion: String) {
        viewModelScope.launch {
            userPreferences.checkAndTrackVersionChange(currentVersion)
            _uiState.value = UpdateUiState.Checking
            repository.getLatestRelease()
                .onSuccess { release ->
                    userPreferences.updateLastUpdateCheckTime(System.currentTimeMillis())
                    val latestVersion = release.tagName.removePrefix("v")
                    if (isNewerVersion(currentVersion, latestVersion)) {
                        _uiState.value = UpdateUiState.UpdateAvailable(release)
                    } else {
                        _uiState.value = UpdateUiState.UpToDate
                    }
                }
                .onFailure { error ->
                    _uiState.value = UpdateUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    private fun isNewerVersion(current: String, latest: String): Boolean {
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        
        for (i in 0 until maxOf(currentParts.size, latestParts.size)) {
            val c = currentParts.getOrElse(i) { 0 }
            val l = latestParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (c > l) return false
        }
        return false
    }

    fun startUpdate(release: GitHubRelease) {
        val asset = release.assets.firstOrNull { it.name.endsWith(".apk") }
        if (asset != null) {
            updateManager.downloadAndInstall(asset.downloadUrl, asset.name)
        }
    }

    fun resetState() {
        _uiState.value = UpdateUiState.Idle
    }
}

sealed interface UpdateUiState {
    data object Idle : UpdateUiState
    data object Checking : UpdateUiState
    data object UpToDate : UpdateUiState
    data class UpdateAvailable(val release: GitHubRelease) : UpdateUiState
    data class Error(val message: String) : UpdateUiState
}
