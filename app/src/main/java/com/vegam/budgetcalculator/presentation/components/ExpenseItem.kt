package com.vegam.budgetcalculator.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vegam.budgetcalculator.core.util.CurrencyFormatter
import com.vegam.budgetcalculator.domain.model.Expense
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ExpenseItem(
    expense: Expense,
    categoryName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val date = Instant.ofEpochMilli(expense.dateTime).atZone(ZoneId.systemDefault()).toLocalDate()
    val formatter = DateTimeFormatter.ofPattern("dd MMM, hh:mm a")
    val timeStr = Instant.ofEpochMilli(expense.dateTime).atZone(ZoneId.systemDefault()).format(formatter)

    ListItem(
        modifier = modifier.fillMaxWidth(),
        headlineContent = {
            Text(
                text = expense.notes ?: categoryName,
                fontWeight = FontWeight.SemiBold
            )
        },
        supportingContent = {
            Text(text = "$categoryName • $timeStr", style = MaterialTheme.typography.bodySmall)
        },
        trailingContent = {
            Text(
                text = CurrencyFormatter.formatMinorUnits(expense.amountMinor),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
    )
}
