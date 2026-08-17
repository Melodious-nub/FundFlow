package com.shawon.fundflow.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.shawon.fundflow.core.designsystem.FundFlowCard
import com.shawon.fundflow.core.update.UpdateStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdateScreen(
    onNavigateBack: () -> Unit,
    viewModel: UpdateViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val packageInfo = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: Exception) {
            null
        }
    }
    val currentVersion = packageInfo?.versionName ?: "1.0.0"
    
    val uiState by viewModel.uiState.collectAsState()
    val currentRelease by viewModel.currentRelease.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val lastCheckTime by viewModel.lastCheckTime.collectAsState()
    val lastUpdateTime by viewModel.lastUpdateTime.collectAsState()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Updates", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (uiState) {
                        is UpdateStatus.UpdateAvailable -> Icons.Default.Download
                        is UpdateStatus.UpToDate -> Icons.Default.Celebration
                        is UpdateStatus.Downloading -> Icons.Default.Download
                        is UpdateStatus.DownloadComplete -> Icons.Default.Celebration
                        else -> Icons.Default.SystemUpdate
                    },
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = when (uiState) {
                    is UpdateStatus.UpdateAvailable -> "New Update Available!"
                    is UpdateStatus.UpToDate -> "You're All Caught Up!"
                    is UpdateStatus.Downloading -> "Downloading Update..."
                    is UpdateStatus.DownloadComplete -> "Ready to Install!"
                    else -> "Version Control"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = when (uiState) {
                    is UpdateStatus.UpdateAvailable -> "A newer version of FundFlow is ready for you."
                    is UpdateStatus.UpToDate -> "You are using the latest and greatest version of FundFlow."
                    is UpdateStatus.Downloading -> "Fetching the latest features for you. Almost there!"
                    is UpdateStatus.DownloadComplete -> "Download finished. Tap below to install."
                    else -> "Keep your app up to date for the latest features and security improvements."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Info Card
            FundFlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    UpdateInfoRow(label = "Installed Version", value = "v$currentVersion")
                    
                    val checkTimeText = if (lastCheckTime > 0) {
                        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(lastCheckTime))
                    } else "Never"
                    UpdateInfoRow(label = "Last Checked", value = checkTimeText)

                    if (lastUpdateTime > 0) {
                        val updateTimeText = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(lastUpdateTime))
                        UpdateInfoRow(label = "Last Updated", value = updateTimeText)
                    }
                }
            }

            // Update Details Section
            currentRelease?.let { release ->
                if (uiState !is UpdateStatus.UpToDate) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "What's New in ${release.tagName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = release.body,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(16.dp),
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (uiState is UpdateStatus.Error) {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Action Required",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (uiState as UpdateStatus.Error).message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            Spacer(modifier = Modifier.weight(1f))

            if (uiState is UpdateStatus.Downloading) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${(downloadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Button(
                onClick = { 
                    val state = uiState
                    when (state) {
                        is UpdateStatus.UpdateAvailable -> viewModel.startUpdate(state.release)
                        is UpdateStatus.DownloadComplete -> viewModel.startUpdate(null) 
                        else -> viewModel.checkForUpdates(currentVersion)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
                enabled = uiState !is UpdateStatus.Downloading && uiState !is UpdateStatus.Checking,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            ) {
                if (uiState is UpdateStatus.Checking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    val icon = if (uiState is UpdateStatus.UpdateAvailable || uiState is UpdateStatus.Downloading) Icons.Default.Download else Icons.Default.Update
                    val text = when (uiState) {
                        is UpdateStatus.UpdateAvailable -> "Download & Install"
                        is UpdateStatus.Downloading -> "Downloading..."
                        is UpdateStatus.DownloadComplete -> "Install Now"
                        else -> "Check for Updates"
                    }
                    
                    Icon(icon, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun UpdateInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
