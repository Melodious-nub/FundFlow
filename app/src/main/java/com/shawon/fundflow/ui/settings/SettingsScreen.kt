package com.shawon.fundflow.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shawon.fundflow.core.designsystem.FundFlowCard

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currency by viewModel.currencyCode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Settings", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        FundFlowCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Currency", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Currently using $currency", style = MaterialTheme.typography.labelSmall)
                }
                TextButton(onClick = { viewModel.updateCurrency("EUR") }) {
                    Text("Change")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        FundFlowCard(modifier = Modifier.fillMaxWidth()) {
            Text(text = "About", style = MaterialTheme.typography.titleMedium)
            Text(text = "FundFlow v1.0", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Developed by Shawon", style = MaterialTheme.typography.labelSmall)
        }
    }
}
