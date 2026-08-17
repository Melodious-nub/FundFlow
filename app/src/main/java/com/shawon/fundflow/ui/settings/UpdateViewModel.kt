package com.shawon.fundflow.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shawon.fundflow.core.update.UpdateManager
import com.shawon.fundflow.core.update.UpdateStatus
import com.shawon.fundflow.data.local.UserPreferences
import com.shawon.fundflow.data.remote.model.GitHubRelease
import com.shawon.fundflow.data.repository.UpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val repository: UpdateRepository,
    private val updateManager: UpdateManager,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val uiState: StateFlow<UpdateStatus> = updateManager.updateState

    val currentRelease: StateFlow<GitHubRelease?> = updateManager.currentRelease

    val downloadProgress: StateFlow<Float> = updateManager.downloadProgress

    val lastCheckTime = userPreferences.lastUpdateCheckTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val lastUpdateTime = userPreferences.lastAppUpdateTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun checkForUpdates(currentVersion: String) {
        viewModelScope.launch {
            userPreferences.checkAndTrackVersionChange(currentVersion)
            updateManager.setChecking()
            repository.getLatestRelease()
                .onSuccess { release ->
                    userPreferences.updateLastUpdateCheckTime(System.currentTimeMillis())
                    val latestVersion = release.tagName.removePrefix("v")
                    if (isNewerVersion(currentVersion, latestVersion)) {
                        updateManager.setAvailableUpdate(release)
                    } else {
                        updateManager.setUpToDate()
                    }
                }
                .onFailure { error ->
                    updateManager.setError(error.message ?: "Unknown error")
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

    fun startUpdate(release: GitHubRelease?) {
        if (release != null) {
            val asset = release.assets.firstOrNull { it.name.endsWith(".apk") }
            if (asset != null) {
                updateManager.downloadAndInstall(asset.downloadUrl, asset.name)
            }
        } else {
            // Manual trigger for already downloaded APK
            updateManager.installLatestDownloadedApk()
        }
    }

    fun resetState() {
        updateManager.reset()
    }

    fun refreshStatus() {
        updateManager.refreshStatus()
    }
}
