package com.shawon.fundflow.core.designsystem

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun RestoreSuccessModal(
    onDismiss: () -> Unit
) {
    var isDone by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Restoration Successful!") }
    
    LaunchedEffect(Unit) {
        delay(800.milliseconds)
        isDone = true
        statusText = "All set! Data updated."
        delay(1200.milliseconds)
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            if (isDone) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        title = { 
            Text(
                "Restore Finished", 
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall
            ) 
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, 
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(
                    statusText, 
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Your app is ready with the recovered data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        },
        shape = MaterialTheme.shapes.extraLarge
    )
}
