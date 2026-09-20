package com.vegam.budgetcalculator.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.vegam.budgetcalculator.domain.model.Category
import com.vegam.budgetcalculator.domain.model.Expense
import com.vegam.budgetcalculator.domain.usecase.analytics.CategorySpending
import com.vegam.budgetcalculator.domain.usecase.budget.MonthlySummary
import com.vegam.budgetcalculator.presentation.components.BudgetSummaryCard
import com.vegam.budgetcalculator.presentation.components.ExpenseItem
import com.vegam.budgetcalculator.ui.theme.BudgetCalculatorTheme
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddExpenseClick: () -> Unit,
    onSummaryClick: () -> Unit,
    onExpenseClick: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    DashboardContent(
        uiState = uiState,
        onAddExpenseClick = onAddExpenseClick,
        onSummaryClick = onSummaryClick,
        onExpenseClick = onExpenseClick,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onDeleteExpense = viewModel::deleteExpense
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onAddExpenseClick: () -> Unit,
    onSummaryClick: () -> Unit,
    onExpenseClick: (String) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDeleteExpense: (String) -> Unit
) {
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    var expensePendingDelete by remember { mutableStateOf<Expense?>(null) }

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
                                IconButton(onClick = onPreviousMonth) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Prev")
                                }
                                Text(
                                    text = data.month.format(monthFormatter),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = onNextMonth) {
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
                                percentage = data.summary.percentage,
                                onClick = onSummaryClick
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
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { value ->
                                        if (value != SwipeToDismissBoxValue.Settled) {
                                            expensePendingDelete = expense
                                        }
                                        false
                                    }
                                )
                                SwipeToDismissBox(
                                    state = dismissState,
                                    backgroundContent = {
                                        Row(
                                            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.errorContainer).padding(horizontal = 20.dp),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onErrorContainer)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Delete", color = MaterialTheme.colorScheme.onErrorContainer)
                                        }
                                    }
                                ) {
                                    ExpenseItem(
                                        expense = expense,
                                        categoryName = categoryName,
                                        onClick = { onExpenseClick(expense.id) }
                                    )
                                }
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

    expensePendingDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { expensePendingDelete = null },
            title = { Text("Delete expense?") },
            text = { Text("This expense will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteExpense(expense.id)
                    expensePendingDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { expensePendingDelete = null }) { Text("Cancel") } }
        )
    }
}

@Preview(name = "Dashboard - Light", showBackground = true, showSystemUi = true)
@Preview(name = "Dashboard - Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DashboardScreenPreview() {
    val now = System.currentTimeMillis()
    val groceries = Category(
        id = "groceries",
        userId = "preview-user",
        name = "Groceries",
        icon = "🛒",
        color = 0xFF4CAF50.toInt(),
        isDefault = true,
        createdAt = now,
        updatedAt = now
    )
    val travel = Category(
        id = "travel",
        userId = "preview-user",
        name = "Travel",
        icon = "✈️",
        color = 0xFF2196F3.toInt(),
        isDefault = true,
        createdAt = now,
        updatedAt = now
    )
    val previewState = DashboardUiState.Success(
        DashboardData(
            month = YearMonth.now(),
            summary = MonthlySummary(
                totalBudget = 50_000_00,
                totalSpent = 18_750_00,
                remaining = 31_250_00,
                percentage = 37.5f
            ),
            categorySpending = listOf(
                CategorySpending(groceries, 15_000_00, 8_200_00, 6_800_00, 54.7f),
                CategorySpending(travel, 10_000_00, 3_500_00, 6_500_00, 35f)
            ),
            recentExpenses = listOf(
                Expense(
                    id = "expense-1",
                    userId = "preview-user",
                    categoryId = groceries.id,
                    subCategoryId = null,
                    amountMinor = 1_250_00,
                    notes = "Weekly groceries",
                    dateTime = now,
                    createdAt = now,
                    updatedAt = now
                ),
                Expense(
                    id = "expense-2",
                    userId = "preview-user",
                    categoryId = travel.id,
                    subCategoryId = null,
                    amountMinor = 650_00,
                    notes = "Cab fare",
                    dateTime = now - 3_600_000,
                    createdAt = now,
                    updatedAt = now
                )
            )
        )
    )

    BudgetCalculatorTheme(dynamicColor = false) {
        DashboardContent(
            uiState = previewState,
            onAddExpenseClick = {},
            onSummaryClick = {},
            onExpenseClick = {},
            onPreviousMonth = {},
            onNextMonth = {},
            onDeleteExpense = {}
        )
    }
}
