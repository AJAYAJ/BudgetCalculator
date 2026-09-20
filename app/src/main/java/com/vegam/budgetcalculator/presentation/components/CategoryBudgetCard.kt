package com.vegam.budgetcalculator.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vegam.budgetcalculator.core.util.CurrencyFormatter
import com.vegam.budgetcalculator.domain.usecase.analytics.CategorySpending

@Composable
fun CategoryBudgetCard(
    spending: CategorySpending,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category Circle with color
                    Surface(
                        modifier = Modifier.size(12.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color(spending.category.color)
                    ) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = spending.category.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (spending.percentage >= 100f) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Over Budget",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${CurrencyFormatter.formatMinorUnits(spending.spentAmount)} / ${CurrencyFormatter.formatMinorUnits(spending.budgetAmount)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${spending.percentage.toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { (spending.percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = if (spending.percentage >= 100f) MaterialTheme.colorScheme.error else Color(spending.category.color),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            if (spending.percentage >= 100f) {
                Text(
                    text = "Over budget by ${CurrencyFormatter.formatMinorUnits(spending.spentAmount - spending.budgetAmount)}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
