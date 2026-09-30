package com.vegam.budgetcalculator.presentation.loans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vegam.budgetcalculator.core.util.CurrencyFormatter
import com.vegam.budgetcalculator.domain.model.LoanSource
import com.vegam.budgetcalculator.domain.model.LoanTransaction
import com.vegam.budgetcalculator.domain.model.LoanTransactionType
import com.vegam.budgetcalculator.ui.theme.BudgetCalculatorTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailsScreen(
    personId: String,
    onBackPressed: () -> Unit,
    viewModel: LoanDetailsViewModel = hiltViewModel()
) {
    val person by viewModel.person.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    LaunchedEffect(personId) { viewModel.load(personId) }

    LoanDetailsContent(
        personName = person?.name ?: "Loan details",
        transactions = transactions,
        onBackPressed = onBackPressed,
        onAddTransaction = viewModel::addTransaction,
        onDeleteTransaction = viewModel::deleteTransaction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoanDetailsContent(
    personName: String,
    transactions: List<LoanTransaction>,
    onBackPressed: () -> Unit,
    onAddTransaction: (String, LoanSource, LoanTransactionType, Long) -> Unit,
    onDeleteTransaction: (String) -> Unit
) {
    var showForm by remember { mutableStateOf(false) }
    val total = transactions.sumOf { it.signedAmountMinor }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(personName) },
                navigationIcon = { IconButton(onClick = onBackPressed) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showForm = true }) { Icon(Icons.Default.Add, "Add transaction") }
        },
        bottomBar = {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total balance", style = MaterialTheme.typography.labelLarge)
                        Text(if (total >= 0) "You received more" else "Amount still to receive", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        CurrencyFormatter.formatMinorUnits(total),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (total < 0) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                    )
                }
            }
        }
    ) { padding ->
        if (transactions.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No transactions yet", style = MaterialTheme.typography.headlineSmall)
                Text("Tap + to add GIVEN, TAKEN, or INTEREST.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(transactions, key = { it.id }) { transaction ->
                    val date = Instant.ofEpochMilli(transaction.transactionDate)
                        .atZone(ZoneId.systemDefault()).toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(date, style = MaterialTheme.typography.bodySmall)
                            Text(transaction.type.displayName(), fontWeight = FontWeight.SemiBold)
                            Text(transaction.source.displayName(), style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            "${if (transaction.signedAmountMinor >= 0) "+" else "−"}${CurrencyFormatter.formatMinorUnits(transaction.amountMinor)}",
                            fontWeight = FontWeight.Bold,
                            color = if (transaction.signedAmountMinor >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = { onDeleteTransaction(transaction.id) }) {
                            Icon(Icons.Default.Delete, "Delete transaction", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    HorizontalDivider()
                }
                item { Spacer(Modifier.size(80.dp)) }
            }
        }
    }

    if (showForm) {
        TransactionFormSheet(
            onDismiss = { showForm = false },
            onSave = { amount, source, type, date ->
                onAddTransaction(amount, source, type, date)
                showForm = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionFormSheet(
    onDismiss: () -> Unit,
    onSave: (String, LoanSource, LoanTransactionType, Long) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var source by remember { mutableStateOf(LoanSource.GOOGLE_PAY) }
    var type by remember { mutableStateOf(LoanTransactionType.GIVEN) }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Add transaction", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = amount,
                onValueChange = { value -> if (value.all { it.isDigit() || it == '.' }) amount = value },
                label = { Text("Amount") },
                prefix = { Text("₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            EnumPicker("Source type", source, LoanSource.entries, { it.displayName() }) { source = it }
            EnumPicker("Transaction type", type, LoanTransactionType.entries, { it.displayName() }) { type = it }
            OutlinedTextField(
                value = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { TextButton(onClick = { showDatePicker = true }) { Text("Select") } }
            )
            Button(
                onClick = { onSave(amount, source, type, date) },
                enabled = (amount.toDoubleOrNull() ?: 0.0) > 0.0,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Add transaction") }
            Spacer(Modifier.size(16.dp))
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { date = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(pickerState) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumPicker(label: String, selected: T, values: List<T>, display: (T) -> String, onSelected: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = display(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            values.forEach { value ->
                DropdownMenuItem(text = { Text(display(value)) }, onClick = {
                    onSelected(value)
                    expanded = false
                })
            }
        }
    }
}

private fun LoanSource.displayName() = when (this) {
    LoanSource.GOOGLE_PAY -> "Google Pay"
    LoanSource.PHONE_PAY -> "PhonePe"
    LoanSource.CASH -> "Cash"
    LoanSource.OTHER -> "Other"
}

private fun LoanTransactionType.displayName() = name.lowercase().replaceFirstChar { it.uppercase() }

@Preview(name = "Loan Details - Light", showBackground = true, showSystemUi = true)
@Preview(
    name = "Loan Details - Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun LoanDetailsScreenPreview() {
    val now = System.currentTimeMillis()
    BudgetCalculatorTheme(dynamicColor = false) {
        LoanDetailsContent(
            personName = "Rick",
            transactions = listOf(
                LoanTransaction(
                    id = "given",
                    personId = "rick",
                    amountMinor = 3_000_00,
                    type = LoanTransactionType.GIVEN,
                    source = LoanSource.GOOGLE_PAY,
                    transactionDate = now - 60L * 24 * 60 * 60 * 1_000,
                    createdAt = now
                ),
                LoanTransaction(
                    id = "interest",
                    personId = "rick",
                    amountMinor = 200_00,
                    type = LoanTransactionType.INTEREST,
                    source = LoanSource.PHONE_PAY,
                    transactionDate = now - 30L * 24 * 60 * 60 * 1_000,
                    createdAt = now
                ),
                LoanTransaction(
                    id = "taken",
                    personId = "rick",
                    amountMinor = 2_000_00,
                    type = LoanTransactionType.TAKEN,
                    source = LoanSource.CASH,
                    transactionDate = now,
                    createdAt = now
                )
            ),
            onBackPressed = {},
            onAddTransaction = { _, _, _, _ -> },
            onDeleteTransaction = {}
        )
    }
}
