package com.shawon.fundflow.ui.budget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shawon.fundflow.core.designsystem.FundFlowCard
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSetupScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: BudgetSetupViewModel = hiltViewModel()
) {
    val existingCycle by viewModel.existingCycle.collectAsState()
    val suggestedStartDate by viewModel.suggestedStartDate.collectAsState()
    
    var name by remember(existingCycle) { mutableStateOf(existingCycle?.name ?: "Monthly Budget") }
    var amount by remember(existingCycle) { mutableStateOf(existingCycle?.baseAmount?.toString() ?: "") }
    
    var startDate by remember(existingCycle, suggestedStartDate) { 
        mutableLongStateOf(
            existingCycle?.startDate ?: suggestedStartDate ?: System.currentTimeMillis()
        ) 
    }
    
    val initialDuration = remember(existingCycle) {
        if (existingCycle != null) {
            val diff = existingCycle!!.endDate - existingCycle!!.startDate
            (diff / (24 * 60 * 60 * 1000)).toString()
        } else "30"
    }
    var days by remember(existingCycle) { mutableStateOf(initialDuration) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    val previousRemaining by viewModel.previousCycleRemaining.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var applyCarryForward by remember(existingCycle) { 
        mutableStateOf(existingCycle?.carryForward != 0L || existingCycle == null) 
    }

    val dateState = rememberDatePickerState(initialSelectedDateMillis = startDate)
    val formatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                BudgetSetupNavigation.ToDashboard -> onNavigateToDashboard()
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { startDate = it }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Cycle?") },
            text = { Text("This will permanently delete this budget cycle and all its associated expenses. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCycle()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            icon = { Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Validation Error") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            if (onNavigateBack != null) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (existingCycle != null) "Edit Budget Cycle" else "Budget Setup",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (existingCycle != null) "Update your spending period" else "Define your spending period",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            FundFlowCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Cycle Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Budget Amount (৳)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = formatter.format(Date(startDate)),
                    onValueChange = {},
                    label = { Text("Start Date") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    trailingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                    },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it },
                    label = { Text("Duration (Days)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            if (previousRemaining != null) {
                Spacer(modifier = Modifier.height(24.dp))
                FundFlowCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = applyCarryForward,
                            onCheckedChange = { applyCarryForward = it }
                        )
                        Column {
                            Text(
                                text = "Carry Forward Balance",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (previousRemaining!! >= 0) 
                                    "Add ৳${previousRemaining} to this cycle" 
                                else "Deduct debt ৳${kotlin.math.abs(previousRemaining!!)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val amountLong = amount.toLongOrNull() ?: 0L
                    val durationDays = days.toIntOrNull() ?: 30
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = startDate
                    calendar.add(Calendar.DAY_OF_YEAR, durationDays)
                    val end = calendar.timeInMillis

                    viewModel.saveBudgetCycle(
                        name = name,
                        startDate = startDate,
                        endDate = end,
                        amount = amountLong,
                        carryForward = if (applyCarryForward) (previousRemaining ?: 0L) else 0L
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.extraLarge,
                enabled = amount.isNotBlank() && name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (existingCycle != null) "Update Cycle" else "Complete Setup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (existingCycle != null) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Cycle")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
