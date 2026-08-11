package com.shawon.fundflow.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shawon.fundflow.core.designsystem.FundFlowCard
import com.shawon.fundflow.domain.model.Expense
import com.shawon.fundflow.ui.settings.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    onNavigateToAddExpense: () -> Unit,
    onNavigateToBudgetSetup: (Long?) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cycles by viewModel.allCycles.collectAsState()
    val currencyCode by settingsViewModel.currencyCode.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var expanded by remember { mutableStateOf(false) }
    var showEndCycleConfirmation by remember { mutableStateOf(false) }

    val currencySymbol = remember(currencyCode) {
        when(currencyCode) {
            "TK" -> "৳"
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "INR" -> "₹"
            else -> "৳"
        }
    }

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is DashboardUiState.NoActiveCycle -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No active budget cycle found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onNavigateToBudgetSetup(null) }) {
                        Text("Create Budget Cycle")
                    }
                }
            }
            is DashboardUiState.Success -> {
                Column {
                    // Cycle Selector Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedCard(
                                onClick = { expanded = true },
                                shape = MaterialTheme.shapes.small
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = state.cycle.name, style = MaterialTheme.typography.labelLarge)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                cycles.forEach { cycle ->
                                    DropdownMenuItem(
                                        text = { Text(cycle.name + if (!cycle.isClosed) " (Active)" else "") },
                                        onClick = {
                                            viewModel.selectCycle(cycle.id)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (!state.cycle.isClosed) {
                            TextButton(onClick = { showEndCycleConfirmation = true }) {
                                Icon(Icons.Default.LockClock, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("End Cycle")
                            }
                        }
                    }

                    if (showEndCycleConfirmation) {
                        AlertDialog(
                            onDismissRequest = { showEndCycleConfirmation = false },
                            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            title = { Text(text = "End Current Cycle?") },
                            text = {
                                Text(
                                    text = "Ending the cycle will lock all expenses for this period. You won't be able to add, edit, or delete expenses in this cycle anymore.\n\nAre you sure you want to proceed?"
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        viewModel.closeCycle(state.cycle.id)
                                        showEndCycleConfirmation = false
                                    }
                                ) {
                                    Text("End Cycle", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showEndCycleConfirmation = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                    
                    DashboardContent(
                        state = state,
                        totalCycles = cycles.size,
                        currencySymbol = currencySymbol,
                        onNavigateToBudgetSetup = { onNavigateToBudgetSetup(null) }
                    )
                }
            }
        }

        if (uiState is DashboardUiState.Success && !(uiState as DashboardUiState.Success).cycle.isClosed) {
            FloatingActionButton(
                onClick = onNavigateToAddExpense,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState.Success,
    totalCycles: Int,
    currencySymbol: String,
    onNavigateToBudgetSetup: () -> Unit
) {
    val today = remember { SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault()).format(Date()) }
    val cycleDateFormatter = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val expenseDateFormatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Text(
                text = today,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.isExpired && !state.cycle.isClosed) {
            item {
                FundFlowCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cycle Period Ended",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Your budget cycle expired on ${cycleDateFormatter.format(Date(state.cycle.endDate))}. End this cycle to start a new one.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToBudgetSetup,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Start New Cycle")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            FundFlowCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Current Balance", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "$currencySymbol ${state.currentBalance}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "Total $totalCycles Cycles",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Remaining Days", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "${state.remainingDays}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Safe Spending", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "$currencySymbol ${state.dailySafeSpending}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            FundFlowCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Range",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${cycleDateFormatter.format(Date(state.cycle.startDate))} - ${cycleDateFormatter.format(Date(state.cycle.endDate))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    strokeCap = StrokeCap.Round,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(state.progress * 100).toInt()}% of budget spent",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(
                text = "Cycle Expenses",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (state.recentExpenses.isEmpty()) {
            item {
                Text(
                    text = "No expenses recorded in this cycle.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(state.recentExpenses) { expense ->
                ExpenseItem(
                    expense = expense, 
                    currencySymbol = currencySymbol,
                    dateText = expenseDateFormatter.format(Date(expense.timestamp))
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun ExpenseItem(expense: Expense, currencySymbol: String, dateText: String) {
    FundFlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${expense.categoryName ?: "General"} • $dateText",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "-$currencySymbol ${expense.amount}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
