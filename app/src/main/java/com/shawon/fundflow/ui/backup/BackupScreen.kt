package com.shawon.fundflow.ui.backup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shawon.fundflow.data.local.AutoBackupSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLocalBackup: () -> Unit,
    onNavigateToCloudBackup: () -> Unit,
    isInitialSetup: Boolean = false,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val autoBackupSettings by viewModel.autoBackupSettings.collectAsState()
    val lastAutoBackupTime by viewModel.lastAutoBackupTime.collectAsState()
    val isGoogleSignedIn by viewModel.isGoogleSignedIn.collectAsState()
    
    var showFrequencyDialog by remember { mutableStateOf(false) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var showNetworkDialog by remember { mutableStateOf(false) }
    var showSignInGuidance by remember { mutableStateOf(false) }

    // Re-check sign in status every time the screen is resumed
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshSignInStatus()
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
                title = { 
                    Text(
                        if (isInitialSetup) "Recover Your Data" else "Backup & Restore", 
                        fontWeight = FontWeight.Bold
                    ) 
                },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (isInitialSetup) {
                Text(
                    text = "Welcome back! Choose how you want to recover your budget history.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // --- Manual Recovery Section ---
            Text(
                text = "Manual Recovery",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            BackupOptionItem(
                title = "Restore from Device",
                subtitle = "Select a JSON backup file from your storage",
                icon = Icons.Default.SdStorage,
                onClick = onNavigateToLocalBackup
            )

            BackupOptionItem(
                title = "Restore from Cloud",
                subtitle = "Sign in to Google Drive to download your data",
                icon = Icons.Default.CloudQueue,
                onClick = onNavigateToCloudBackup
            )

            if (!isInitialSetup) {
                // --- Automatic Backup Section ---
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Automatic Backup",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        val lastRunText = if (lastAutoBackupTime > 0) {
                            val formatter = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault())
                            "Last run: ${formatter.format(java.util.Date(lastAutoBackupTime))}"
                        } else "Never run automatically"

                        ListItem(
                            headlineContent = { Text("Enable Auto Backup", fontWeight = FontWeight.Bold) },
                            supportingContent = { Text(lastRunText) },
                            leadingContent = { Icon(Icons.Default.EventRepeat, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            trailingContent = {
                                Switch(
                                    checked = autoBackupSettings.enabled,
                                    onCheckedChange = { 
                                        if (it && (autoBackupSettings.target == "CLOUD" || autoBackupSettings.target == "BOTH") && !isGoogleSignedIn) {
                                            showSignInGuidance = true
                                        } else {
                                            viewModel.updateAutoBackupSettings(autoBackupSettings.copy(enabled = it))
                                        }
                                    }
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                        )

                        if (autoBackupSettings.enabled) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                            
                            SettingsActionItem(
                                title = "Backup Destination",
                                value = autoBackupSettings.target.lowercase().replaceFirstChar { it.uppercase() },
                                icon = Icons.Default.Storage,
                                onClick = { showTargetDialog = true }
                            )

                            SettingsActionItem(
                                title = "Frequency",
                                value = autoBackupSettings.frequency.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                icon = Icons.Default.EventRepeat,
                                onClick = { showFrequencyDialog = true }
                            )

                            SettingsActionItem(
                                title = "Network Conditions",
                                value = if (autoBackupSettings.networkType == "BOTH") "Wi-Fi or Cellular" else autoBackupSettings.networkType.lowercase().replaceFirstChar { it.uppercase() },
                                icon = Icons.Default.NetworkCheck,
                                onClick = { showNetworkDialog = true }
                            )

                            if (autoBackupSettings.target == "LOCAL" || autoBackupSettings.target == "BOTH") {
                                Text(
                                    text = "Note: Automatic local backups are saved to your Downloads/FundFlow_Backups folder for easy access.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Selection Dialogs
    if (showTargetDialog) {
        SelectionDialog(
            title = "Backup Destination",
            options = listOf(
                "LOCAL" to "Internal Storage", 
                "CLOUD" to "Google Drive ${if (!isGoogleSignedIn) "(Sign-in required)" else ""}", 
                "BOTH" to "Both (Recommended) ${if (!isGoogleSignedIn) "(Sign-in required)" else ""}"
            ),
            selectedValue = autoBackupSettings.target,
            onDismiss = { showTargetDialog = false },
            onSelect = { 
                if ((it == "CLOUD" || it == "BOTH") && !isGoogleSignedIn) {
                    showSignInGuidance = true
                } else {
                    viewModel.updateAutoBackupSettings(autoBackupSettings.copy(target = it))
                }
                showTargetDialog = false
            }
        )
    }

    if (showSignInGuidance) {
        AlertDialog(
            onDismissRequest = { showSignInGuidance = false },
            title = { Text("Sign-in Required") },
            text = { Text("To use Google Drive for automatic backups, you need to sign in to your Google account first. Would you like to go to the Cloud Backup screen now?") },
            confirmButton = {
                Button(onClick = {
                    showSignInGuidance = false
                    onNavigateToCloudBackup()
                }) {
                    Text("Sign In")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignInGuidance = false }) {
                    Text("Maybe Later")
                }
            }
        )
    }

    if (showFrequencyDialog) {
        SelectionDialog(
            title = "Backup Frequency",
            options = listOf("DAILY" to "Daily", "WEEKLY" to "Weekly", "15_DAYS" to "Every 15 Days", "MONTHLY" to "Monthly"),
            selectedValue = autoBackupSettings.frequency,
            onDismiss = { showFrequencyDialog = false },
            onSelect = { 
                viewModel.updateAutoBackupSettings(autoBackupSettings.copy(frequency = it))
                showFrequencyDialog = false
            }
        )
    }

    if (showNetworkDialog) {
        SelectionDialog(
            title = "Network Conditions",
            options = listOf("WIFI" to "Wi-Fi Only", "CELLULAR" to "Cellular Data", "BOTH" to "Wi-Fi or Cellular"),
            selectedValue = autoBackupSettings.networkType,
            onDismiss = { showNetworkDialog = false },
            onSelect = { 
                viewModel.updateAutoBackupSettings(autoBackupSettings.copy(networkType = it))
                showNetworkDialog = false
            }
        )
    }
}

@Composable
private fun SettingsActionItem(
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.bodyLarge) },
        trailingContent = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        },
        leadingContent = { Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    )
}

@Composable
private fun SelectionDialog(
    title: String,
    options: List<Pair<String, String>>,
    selectedValue: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(value) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = value == selectedValue, onClick = { onSelect(value) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun BackupOptionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
