package com.vegam.budgetcalculator.presentation.loans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vegam.budgetcalculator.domain.model.LoanPerson
import com.vegam.budgetcalculator.ui.theme.BudgetCalculatorTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(
    onBackPressed: () -> Unit,
    onPersonClick: (String) -> Unit,
    viewModel: LoansViewModel = hiltViewModel()
) {
    val people by viewModel.people.collectAsState()

    LoansContent(
        people = people,
        onBackPressed = onBackPressed,
        onPersonClick = onPersonClick,
        onAddPerson = viewModel::addPerson
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoansContent(
    people: List<LoanPerson>,
    onBackPressed: () -> Unit,
    onPersonClick: (String) -> Unit,
    onAddPerson: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var fabOffset by remember { mutableStateOf(Offset.Zero) }
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val maxLeftPx = with(density) { (configuration.screenWidthDp.dp - 80.dp).toPx() }
    val maxUpPx = with(density) { (configuration.screenHeightDp.dp - 180.dp).toPx() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loans") },
                navigationIcon = { IconButton(onClick = onBackPressed) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 40.dp)
                    .offset { IntOffset(fabOffset.x.roundToInt(), fabOffset.y.roundToInt()) }
                    .pointerInput(maxLeftPx, maxUpPx) {
                        detectDragGesturesAfterLongPress { change, dragAmount ->
                            change.consume()
                            fabOffset = Offset(
                                x = (fabOffset.x + dragAmount.x).coerceIn(-maxLeftPx, 0f),
                                y = (fabOffset.y + dragAmount.y).coerceIn(-maxUpPx, 0f)
                            )
                        }
                    }
            ) {
                Icon(Icons.Default.Add, "Add person")
            }
        }
    ) { padding ->
        if (people.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("No loan accounts yet", style = MaterialTheme.typography.headlineSmall)
                Text("Tap + to add a person such as Judith or Rick.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(people, key = { it.id }) { person ->
                    ListItem(
                        headlineContent = { Text(person.name) },
                        leadingContent = { Icon(Icons.Default.Person, null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                        modifier = Modifier.fillMaxWidth().clickable { onPersonClick(person.id) }.padding(horizontal = 8.dp)
                    )
                }
                item { Spacer(Modifier.size(80.dp)) }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add person") },
            text = { OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true) },
            confirmButton = {
                Button(onClick = {
                    onAddPerson(name)
                    name = ""
                    showAddDialog = false
                }, enabled = name.isNotBlank()) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }
}

@Preview(name = "Loans - Light", showBackground = true, showSystemUi = true)
@Preview(
    name = "Loans - Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun LoansScreenPreview() {
    val now = System.currentTimeMillis()
    BudgetCalculatorTheme(dynamicColor = false) {
        LoansContent(
            people = listOf(
                LoanPerson("judith", "preview-user", "Judith", now),
                LoanPerson("rick", "preview-user", "Rick", now),
                LoanPerson("maya", "preview-user", "Maya", now)
            ),
            onBackPressed = {},
            onPersonClick = {},
            onAddPerson = {}
        )
    }
}
