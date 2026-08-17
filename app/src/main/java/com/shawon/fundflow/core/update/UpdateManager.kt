package com.shawon.fundflow.core.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.shawon.fundflow.data.local.UserPreferences
import com.shawon.fundflow.data.remote.model.GitHubAsset
import com.shawon.fundflow.data.remote.model.GitHubRelease
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferences
) {
    private var downloadId: Long = -1
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var progressJob: Job? = null

    private val _updateState = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateState = _updateState.asStateFlow()

    private val _currentRelease = MutableStateFlow<GitHubRelease?>(null)
    val currentRelease = _currentRelease.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress = _downloadProgress.asStateFlow()

    init {
        // Load persisted update state
        scope.launch {
            val info = userPreferences.latestReleaseInfo.first()
            val tag = info["tag"]
            if (!tag.isNullOrEmpty()) {
                val apkName = info["name"] ?: ""
                val release = GitHubRelease(
                    tagName = tag,
                    name = tag,
                    body = info["body"] ?: "",
                    assets = listOf(GitHubAsset(name = apkName, downloadUrl = info["url"] ?: "", contentType = "application/vnd.android.package-archive", size = 0L))
                )
                _currentRelease.value = release
                
                // Check if already downloaded
                val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), apkName)
                if (file.exists()) {
                    _updateState.value = UpdateStatus.DownloadComplete
                } else {
                    _updateState.value = UpdateStatus.UpdateAvailable(release)
                }
            }
        }
    }

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                Log.d("UpdateManager", "Download complete, triggering installation")
                stopProgressPolling()
                _updateState.value = UpdateStatus.DownloadComplete
                installLatestDownloadedApk()
            }
        }
    }

    fun setAvailableUpdate(release: GitHubRelease) {
        _currentRelease.value = release
        if (_updateState.value !is UpdateStatus.Downloading && _updateState.value !is UpdateStatus.DownloadComplete) {
            _updateState.value = UpdateStatus.UpdateAvailable(release)
        }
        
        // Persist release info
        scope.launch {
            val asset = release.assets.firstOrNull { it.name.endsWith(".apk") }
            if (asset != null) {
                userPreferences.saveLatestRelease(
                    tag = release.tagName,
                    body = release.body,
                    url = asset.downloadUrl,
                    name = asset.name
                )
            }
        }
    }

    fun setChecking() {
        _updateState.value = UpdateStatus.Checking
    }

    fun setUpToDate() {
        _currentRelease.value = null
        _updateState.value = UpdateStatus.UpToDate
    }

    fun setError(message: String) {
        _updateState.value = UpdateStatus.Error(message)
    }

    fun reset() {
        _updateState.value = UpdateStatus.Idle
        _currentRelease.value = null
        _downloadProgress.value = 0f
        scope.launch {
            userPreferences.saveLatestRelease("", "", "", "")
        }
    }

    /**
     * Called when the update screen is resumed to re-verify permissions if an installation was pending.
     */
    fun refreshStatus() {
        val currentState = _updateState.value
        if (currentState is UpdateStatus.Error && currentState.message.contains("Permission")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (context.packageManager.canRequestPackageInstalls()) {
                    _updateState.value = UpdateStatus.DownloadComplete
                }
            }
        }
    }

    fun registerReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                downloadReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(
                downloadReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }
    }

    fun unregisterReceiver() {
        try {
            context.unregisterReceiver(downloadReceiver)
        } catch (e: Exception) {
            // Already unregistered
        }
    }

    fun downloadAndInstall(downloadUrl: String, fileName: String) {
        if (_updateState.value is UpdateStatus.Downloading) return

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        
        // Clean up old file if exists
        val oldFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        if (oldFile.exists()) oldFile.delete()

        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle("FundFlow Update")
            .setDescription("Downloading latest version...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        downloadId = downloadManager.enqueue(request)
        _updateState.value = UpdateStatus.Downloading
        
        // Save filename for later
        context.getSharedPreferences("update_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("pending_apk_name", fileName)
            .apply()

        startProgressPolling(downloadId)
    }

    fun startProgressPolling(id: Long) {
        progressJob?.cancel()
        progressJob = scope.launch {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            var isDownloading = true
            while (isDownloading) {
                val query = DownloadManager.Query().setFilterById(id)
                val cursor: Cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val bytesDownloaded = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val bytesTotal = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    
                    if (bytesTotal > 0) {
                        _downloadProgress.value = bytesDownloaded.toFloat() / bytesTotal.toFloat()
                    }

                    val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        isDownloading = false
                        _updateState.value = UpdateStatus.DownloadComplete
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        isDownloading = false
                        _updateState.value = UpdateStatus.Error("Download failed. Please try again.")
                    }
                }
                cursor.close()
                delay(500)
            }
        }
    }

    private fun stopProgressPolling() {
        progressJob?.cancel()
        progressJob = null
    }

    fun installLatestDownloadedApk() {
        val fileName = context.getSharedPreferences("update_prefs", Context.MODE_PRIVATE)
            .getString("pending_apk_name", null)
        if (fileName != null) {
            installApk(fileName)
        }
    }

    fun installApk(fileName: String) {
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        if (!file.exists()) {
            Log.e("UpdateManager", "APK file not found at ${file.absolutePath}")
            _updateState.value = UpdateStatus.Error("Installation file missing. Please download again.")
            return
        }

        // Check for Unknown Sources permission on Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                _updateState.value = UpdateStatus.Error("Permission required to install updates. Please enable 'Install unknown apps'.")
                openInstallPermissionSettings()
                return
            }
        }

        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("UpdateManager", "Failed to start installation: ${e.message}")
            _updateState.value = UpdateStatus.Error("Failed to start installation. Please try again.")
        }
    }

    private fun openInstallPermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
}

sealed interface UpdateStatus {
    data object Idle : UpdateStatus
    data object Checking : UpdateStatus
    data object UpToDate : UpdateStatus
    data class UpdateAvailable(val release: GitHubRelease) : UpdateStatus
    data object Downloading : UpdateStatus
    data object DownloadComplete : UpdateStatus
    data class Error(val message: String) : UpdateStatus
}
