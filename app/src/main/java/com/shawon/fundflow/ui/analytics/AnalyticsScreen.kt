package com.shawon.fundflow.ui.analytics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.shawon.fundflow.core.designsystem.FundFlowCard

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.analyticsState.collectAsState()
    val modelProducer = remember { CartesianChartModelProducer() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Analytics", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        when (val s = state) {
            is AnalyticsUiState.Success -> {
                LaunchedEffect(s.weeklyData) {
                    modelProducer.runTransaction {
                        columnSeries {
                            series(s.weeklyData.values.map { it.toFloat() })
                        }
                    }
                }

                FundFlowCard(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Weekly Spending", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            rememberColumnCartesianLayer(),
                        ),
                        modelProducer = modelProducer,
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                FundFlowCard(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Category Breakdown", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    s.categoryBreakdown.forEach { (catId, amount) ->
                        Text(text = "Category $catId: $amount")
                    }
                }
            }
            is AnalyticsUiState.Loading -> {
                Text(text = "Loading analytics...")
            }
            is AnalyticsUiState.NoData -> {
                Text(text = "No analytics data available for this cycle.")
            }
        }
    }
}
