package com.vegam.budgetcalculator.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vegam.budgetcalculator.presentation.components.BudgetSummaryCard
import com.vegam.budgetcalculator.presentation.components.CategoryBudgetCard
import com.vegam.budgetcalculator.presentation.components.ExpenseItem
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddExpenseClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onExpenseClick: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Budget Calculator", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpenseClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DashboardUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is DashboardUiState.Success -> {
                    val data = state.data
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Month Selector
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { viewModel.previousMonth() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Prev")
                                }
                                Text(
                                    text = data.month.format(monthFormatter),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { viewModel.nextMonth() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next")
                                }
                            }
                        }

                        // Summary Card
                        item {
                            BudgetSummaryCard(
                                totalBudget = data.summary.totalBudget,
                                totalSpent = data.summary.totalSpent,
                                remaining = data.summary.remaining,
                                percentage = data.summary.percentage
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Category Budgets",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Category Cards
                        items(data.categorySpending) { spending ->
                            CategoryBudgetCard(
                                spending = spending,
                                onClick = { onCategoryClick(spending.category.id) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Recent Expenses",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = { /* See All */ }) {
                                    Text("See All")
                                }
                            }
                        }

                        if (data.recentExpenses.isEmpty()) {
                            item {
                                Text(
                                    "No expenses yet for this month.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        } else {
                            items(data.recentExpenses) { expense ->
                                val categoryName = data.categorySpending.find { it.category.id == expense.categoryId }?.category?.name ?: "Unknown"
                                ExpenseItem(
                                    expense = expense,
                                    categoryName = categoryName,
                                    onClick = { onExpenseClick(expense.id) }
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
}
