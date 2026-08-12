package com.shawon.fundflow.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shawon.fundflow.core.designsystem.FundFlowCard
import com.shawon.fundflow.core.designsystem.RestoreSuccessModal
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudBackupScreen(
    onNavigateBack: () -> Unit,
    onRestoreSuccess: () -> Unit = {},
    isInitialSetup: Boolean = false,
    viewModel: CloudBackupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lastBackupTime by viewModel.lastBackupTime.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showRestoreConfirmation by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showSuccessModal by remember { mutableStateOf(false) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.onSignInResult(result.data)
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is CloudBackupEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                CloudBackupEvent.RestoreSuccess -> showSuccessModal = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isInitialSetup) "Restore from Cloud" else "Cloud Backup (Google Drive)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!uiState.isSignedIn) {
                    CloudSignInContent(
                        isInitialSetup = isInitialSetup,
                        onSignIn = { signInLauncher.launch(viewModel.getSignInIntent()) }
                    )
                } else {
                    CloudManageContent(
                        email = uiState.userEmail ?: "Unknown",
                        lastBackupTime = lastBackupTime,
                        isInitialSetup = isInitialSetup,
                        onBackup = { viewModel.backupToCloud() },
                        onRestore = { showRestoreConfirmation = true },
                        onSignOut = { showLogoutConfirmation = true }
                    )
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    if (showRestoreConfirmation) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmation = false },
            title = { Text("Restore from Cloud?") },
            text = { Text("Restoring from cloud will replace ALL current information. This action cannot be undone. Are you sure you want to proceed?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.restoreFromCloud()
                        showRestoreConfirmation = false
                    }
                ) {
                    Text("Restore", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = { Text("Sign Out?") },
            text = { Text("Are you sure you want to sign out? This will disconnect your Google account from FundFlow. You'll need to sign back in to access your cloud backups.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.signOut()
                        showLogoutConfirmation = false
                    }
                ) {
                    Text("Sign Out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSuccessModal) {
        RestoreSuccessModal(
            onDismiss = {
                showSuccessModal = false
                onRestoreSuccess()
            }
        )
    }
}

@Composable
private fun CloudSignInContent(
    isInitialSetup: Boolean,
    onSignIn: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CloudUpload,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (isInitialSetup) "Restore your data from Cloud" else "Secure your data in the cloud",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isInitialSetup) 
                "Sign in with Google to recover your previous budget and expenses from your private AppData folder on Google Drive."
                else "Sign in with Google to backup your budget and expenses to your own Google Drive AppData folder.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onSignIn,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Sign in with Google")
        }
    }
}

@Composable
private fun CloudManageContent(
    email: String,
    lastBackupTime: Long,
    isInitialSetup: Boolean,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        FundFlowCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Connected Account",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = email,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onSignOut) {
                    Icon(Icons.Default.Logout, contentDescription = "Sign Out", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        if (!isInitialSetup) {
            Spacer(modifier = Modifier.height(16.dp))

            FundFlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Last Cloud Backup",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val dateText = if (lastBackupTime > 0) {
                        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(lastBackupTime))
                    } else "Never"
                    Text(text = dateText, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            CloudActionButton(
                title = "Backup Now",
                icon = Icons.Default.CloudUpload,
                onClick = onBackup
            )
        } else {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Welcome back! Click the button below to restore your data from Google Drive.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        CloudActionButton(
            title = "Restore from Cloud",
            icon = Icons.Default.CloudDownload,
            onClick = onRestore,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Data is stored in your private AppData folder on Google Drive. FundFlow cannot access your other files.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun CloudActionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(64.dp),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}
