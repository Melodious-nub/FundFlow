package com.shawon.fundflow.ui.analytics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.shawon.fundflow.core.designsystem.FundFlowCard
import com.shawon.fundflow.ui.settings.SettingsViewModel
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.analyticsState.collectAsState()
    val cycles by viewModel.allCycles.collectAsState()
    val currencyCode by settingsViewModel.currencyCode.collectAsState()
    val modelProducer = remember { CartesianChartModelProducer() }
    
    var expanded by remember { mutableStateOf(false) }
    var selectedCycleName by remember { mutableStateOf("Select Cycle") }

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

    // Update selected name when cycles load or change
    LaunchedEffect(cycles) {
        if (selectedCycleName == "Select Cycle") {
            cycles.find { !it.isClosed }?.let {
                selectedCycleName = it.name
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Cycle Dropdown
            Text(
                text = "Budget Cycle",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Box {
                OutlinedCard(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCycleName,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    cycles.forEach { cycle ->
                        DropdownMenuItem(
                            text = { Text(cycle.name) },
                            onClick = {
                                selectedCycleName = cycle.name
                                viewModel.onCycleSelected(cycle.id)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            when (val s = state) {
                is AnalyticsUiState.Success -> {
                    LaunchedEffect(s.weeklyData) {
                        if (s.weeklyData.isNotEmpty()) {
                            modelProducer.runTransaction {
                                columnSeries {
                                    series(s.weeklyData.values.map { it.toFloat() })
                                }
                            }
                        }
                    }

                    FundFlowCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Weekly Spending",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (s.weeklyData.values.any { it > 0 }) {
                            CartesianChartHost(
                                chart = rememberCartesianChart(
                                    rememberColumnCartesianLayer(),
                                ),
                                modelProducer = modelProducer,
                                modifier = Modifier.fillMaxWidth().height(200.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No spending recorded for this cycle",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    FundFlowCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Category Breakdown",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        s.categoryBreakdown.forEach { (catId, amount) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (catId == -1L) "General" else "Category $catId",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "$currencySymbol $amount",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                is AnalyticsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is AnalyticsUiState.NoData -> {
                    FundFlowCard(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No expenses found for this cycle.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
