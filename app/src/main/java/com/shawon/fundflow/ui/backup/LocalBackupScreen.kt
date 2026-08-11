package com.shawon.fundflow.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shawon.fundflow.core.designsystem.FundFlowCard
import com.shawon.fundflow.core.designsystem.RestoreSuccessModal
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalBackupScreen(
    onNavigateBack: () -> Unit,
    onRestoreSuccess: () -> Unit = {},
    viewModel: BackupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val backupInfo by viewModel.backupInfo.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showRestoreConfirmation by remember { mutableStateOf(false) }
    var showSuccessModal by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportBackup(it) }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { 
            pendingRestoreUri = it
            showRestoreConfirmation = true 
        }
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is BackupEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                BackupEvent.RestoreSuccess -> showSuccessModal = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore", fontWeight = FontWeight.Bold) },
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
                FundFlowCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Last Backup Info",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val lastBackupDate = if (backupInfo.first > 0) {
                            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(backupInfo.first))
                        } else "Never"
                        
                        val lastBackupSize = if (backupInfo.second > 0) {
                            String.format("%.2f KB", backupInfo.second / 1024f)
                        } else "0 KB"

                        Text(text = "Time: $lastBackupDate", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Size: $lastBackupSize", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { 
                        val fileName = "fundflow_backup_${System.currentTimeMillis()}.json"
                        createDocumentLauncher.launch(fileName) 
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Backup")
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { openDocumentLauncher.launch(arrayOf("application/json")) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restore from Backup")
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Local backup only. Data will be saved as a JSON file. Please keep this file safe to restore your data later.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    if (showRestoreConfirmation) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmation = false },
            title = { Text("Restore Data?") },
            text = { Text("Restoring data will replace ALL current information. This action cannot be undone. Are you sure you want to proceed?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingRestoreUri?.let { viewModel.importBackup(it) }
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

    if (showSuccessModal) {
        RestoreSuccessModal(
            onDismiss = {
                showSuccessModal = false
                onRestoreSuccess()
            }
        )
    }
}
