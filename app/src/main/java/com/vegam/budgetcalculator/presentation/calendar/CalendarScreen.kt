package com.vegam.budgetcalculator.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vegam.budgetcalculator.core.util.CurrencyFormatter
import com.vegam.budgetcalculator.presentation.components.ExpenseItem
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val expensesForMonth by viewModel.expensesForMonth.collectAsState()
    val expensesForDate by viewModel.expensesForDate.collectAsState()
    
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

        // Calendar Grid
        CalendarGrid(
            month = selectedMonth,
            selectedDate = selectedDate,
            onDateSelected = { viewModel.selectDate(it) },
            expenses = expensesForMonth
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        val dailyTotal = expensesForDate.sumOf { it.amountMinor }
        Text(
            text = "Total: ${CurrencyFormatter.formatMinorUnits(dailyTotal)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (expensesForDate.isEmpty()) {
                item {
                    Text("No transactions for this day.", modifier = Modifier.padding(vertical = 16.dp))
                }
            } else {
                items(expensesForDate) { expense ->
                    ExpenseItem(
                        expense = expense,
                        categoryName = "Expense", // TODO: Get actual category name
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    expenses: List<com.vegam.budgetcalculator.domain.model.Expense>
) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfMonth = month.atDay(1).dayOfWeek.value % 7 // adjustment for Sun=0
    val days = (1..daysInMonth).toList()
    val dayNames = listOf("S", "M", "T", "W", "T", "F", "S")

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            dayNames.forEach { name ->
                Text(
                    text = name,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        val totalGridItems = firstDayOfMonth + daysInMonth
        val rows = (totalGridItems + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val index = row * 7 + col
                    val day = index - firstDayOfMonth + 1
                    
                    if (day in 1..daysInMonth) {
                        val date = month.atDay(day)
                        val isSelected = date == selectedDate
                        val hasExpenses = expenses.any {
                            java.time.Instant.ofEpochMilli(it.dateTime).atZone(java.time.ZoneId.systemDefault()).toLocalDate() == date
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = MaterialTheme.shapes.small
                                )
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = day.toString(),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                                if (hasExpenses) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                androidx.compose.foundation.shape.CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
