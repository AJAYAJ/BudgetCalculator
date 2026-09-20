package com.vegam.budgetcalculator.presentation.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vegam.budgetcalculator.core.util.CurrencyFormatter
import java.time.format.DateTimeFormatter

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Month Selector
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.previousMonth() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Prev")
            }
            Text(
                text = selectedMonth.format(monthFormatter),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { viewModel.nextMonth() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next")
            }
        }

        when (val state = uiState) {
            is AnalyticsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is AnalyticsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is AnalyticsUiState.Success -> {
                val data = state.data
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            "Spending by Category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        DonutChart(data.categorySpending)
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                    
                    item {
                        Text(
                            "Daily Spending",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        BarChart(data.dailySpending)
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    items(data.categorySpending) { spending ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(12.dp),
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = Color(spending.category.color)
                                ) {}
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(spending.category.name)
                            }
                            Text(
                                CurrencyFormatter.formatMinorUnits(spending.spentAmount),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DonutChart(categorySpending: List<com.vegam.budgetcalculator.domain.usecase.analytics.CategorySpending>) {
    val totalSpent = categorySpending.sumOf { it.spentAmount }
    if (totalSpent == 0L) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("No data available")
        }
        return
    }

    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        var startAngle = -90f
        categorySpending.forEach { spending ->
            val sweepAngle = (spending.spentAmount.toFloat() / totalSpent.toFloat()) * 360f
            drawArc(
                color = Color(spending.category.color),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 40.dp.toPx())
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun BarChart(dailySpending: List<com.vegam.budgetcalculator.domain.usecase.analytics.DailySpending>) {
    val maxSpent = dailySpending.maxOfOrNull { it.amountMinor } ?: 1L
    val effectiveMax = if (maxSpent == 0L) 1L else maxSpent

    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val barWidth = size.width / dailySpending.size
        dailySpending.forEachIndexed { index, spending ->
            val barHeight = (spending.amountMinor.toFloat() / effectiveMax.toFloat()) * size.height
            drawRect(
                color = Color.Blue.copy(alpha = 0.5f),
                topLeft = Offset(index * barWidth + 2.dp.toPx(), size.height - barHeight),
                size = Size(barWidth - 4.dp.toPx(), barHeight)
            )
        }
    }
}
