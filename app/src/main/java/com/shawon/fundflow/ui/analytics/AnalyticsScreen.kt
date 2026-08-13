package com.shawon.fundflow.ui.analytics

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalDensity
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.shawon.fundflow.core.designsystem.FundFlowCard
import com.shawon.fundflow.ui.settings.SettingsViewModel
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.analyticsState.collectAsState()
    val cycles by viewModel.allCycles.collectAsState()
    val currencyCode by settingsViewModel.currencyCode.collectAsState()
    val lineModelProducer = remember { CartesianChartModelProducer() }
    val columnModelProducer = remember { CartesianChartModelProducer() }
    
    var expanded by remember { mutableStateOf(false) }

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
                        val displayName = when (val s = state) {
                            is AnalyticsUiState.Success -> s.cycleName
                            is AnalyticsUiState.NoData -> s.cycleName.ifEmpty { "Select Cycle" }
                            else -> "Select Cycle"
                        }
                        Text(
                            text = displayName,
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
                    LaunchedEffect(s.dailyTrend) {
                        if (s.dailyTrend.isNotEmpty()) {
                            lineModelProducer.runTransaction {
                                lineSeries {
                                    series(s.dailyTrend.values.map { it.toFloat() })
                                }
                            }
                            columnModelProducer.runTransaction {
                                columnSeries {
                                    series(s.dailyTrend.values.map { it.toFloat() })
                                }
                            }
                        }
                    }

                    // Stats Cards Row
                    Row(modifier = Modifier.fillMaxWidth()) {
                        FundFlowCard(modifier = Modifier.weight(1f)) {
                            Text(text = "Total Spent", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "$currencySymbol ${s.totalSpent}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        FundFlowCard(modifier = Modifier.weight(1f)) {
                            Text(text = "Avg / Day", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "$currencySymbol ${s.averagePerDay}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (s.topSpendingDay != null) {
                        FundFlowCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.QueryStats, 
                                    contentDescription = null, 
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Peak Spending Day", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "${s.topSpendingDay.first}: $currencySymbol ${s.topSpendingDay.second}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Spending Trend
                    FundFlowCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daily Spending",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (s.dailyTrend.isNotEmpty()) {
                            val axisLabel = rememberAxisLabelComponent(color = MaterialTheme.colorScheme.onSurface)
                            val axisLine = rememberAxisLineComponent(color = MaterialTheme.colorScheme.outlineVariant)
                            val axisGuideline = rememberAxisGuidelineComponent(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            CartesianChartHost(
                                chart = rememberCartesianChart(
                                    rememberColumnCartesianLayer(
                                        columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                                            rememberLineComponent(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                thickness = 8.dp
                                            )
                                        )
                                    ),
                                    startAxis = rememberStartAxis(label = axisLabel, line = axisLine, guideline = axisGuideline),
                                    bottomAxis = rememberBottomAxis(label = axisLabel, line = axisLine, guideline = axisGuideline),
                                ),
                                modelProducer = columnModelProducer,
                                modifier = Modifier.fillMaxWidth().height(200.dp)
                            )
                        } else {
                            NoDataPlaceholder()
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Line Chart for Trend
                    FundFlowCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cumulative Trend",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (s.dailyTrend.isNotEmpty()) {
                            val axisLabel = rememberAxisLabelComponent(color = MaterialTheme.colorScheme.onSurface)
                            val axisLine = rememberAxisLineComponent(color = MaterialTheme.colorScheme.outlineVariant)
                            val axisGuideline = rememberAxisGuidelineComponent(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            CartesianChartHost(
                                chart = rememberCartesianChart(
                                    rememberLineCartesianLayer(
                                        lineProvider = LineCartesianLayer.LineProvider.series(
                                            rememberLine(
                                                fill = LineCartesianLayer.LineFill.single(Fill(MaterialTheme.colorScheme.primary.toArgb())),
                                                thickness = 3.dp
                                            )
                                        )
                                    ),
                                    startAxis = rememberStartAxis(label = axisLabel, line = axisLine, guideline = axisGuideline),
                                    bottomAxis = rememberBottomAxis(label = axisLabel, line = axisLine, guideline = axisGuideline),
                                ),
                                modelProducer = lineModelProducer,
                                modifier = Modifier.fillMaxWidth().height(150.dp)
                            )
                        } else {
                            NoDataPlaceholder()
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Category Breakdown
                    FundFlowCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PieChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Category Breakdown",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        s.categoryBreakdown.forEach { category ->
                            CategoryProgressItem(category, currencySymbol)
                            Spacer(modifier = Modifier.height(12.dp))
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
                                text = if (s.cycleName.isEmpty()) "No cycle selected." else "No expenses found for '${s.cycleName}'.",
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

@Composable
private fun CategoryProgressItem(category: CategoryAnalytics, currencySymbol: String) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(category.color.toColorInt()))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.categoryName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$currencySymbol ${category.amount}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { category.percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            strokeCap = StrokeCap.Round,
            color = Color(category.color.toColorInt()),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            text = "${(category.percentage * 100).toInt()}% of total",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
private fun NoDataPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No spending recorded for this cycle",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
