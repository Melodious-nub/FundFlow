package com.shawon.fundflow.ui.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Calendar

@Composable
fun BudgetSetupScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: BudgetSetupViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("Monthly Budget") }
    var amount by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("30") }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                BudgetSetupNavigation.ToDashboard -> onNavigateToDashboard()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Set Up Your Budget",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Cycle Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Budget Amount") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = days,
            onValueChange = { days = it },
            label = { Text("Duration (Days)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val amountLong = amount.toLongOrNull() ?: 0L
                val durationDays = days.toIntOrNull() ?: 30
                val start = System.currentTimeMillis()
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = start
                calendar.add(Calendar.DAY_OF_YEAR, durationDays)
                val end = calendar.timeInMillis

                viewModel.saveBudgetCycle(
                    name = name,
                    startDate = start,
                    endDate = end,
                    amount = amountLong
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Complete Setup")
        }
    }
}
