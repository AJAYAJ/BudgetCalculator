package com.vegam.budgetcalculator.presentation.backup

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(onBackPressed: () -> Unit, viewModel: BackupViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmBackup by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        viewModel.authorizationResult(result.resultCode, result.data)
    }
    LaunchedEffect(state.authorizationIntent) {
        state.authorizationIntent?.let { pendingIntent ->
            viewModel.authorizationLaunched()
            try {
                launcher.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
            } catch (_: Exception) {
                viewModel.cancelAuthorization()
            }
        }
    }
    BackHandler(enabled = state.busy) {}
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & restore") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed, enabled = !state.busy) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Google Drive", style = MaterialTheme.typography.headlineSmall)
            Text("Keep a private backup of your expenses, categories, subcategories, monthly budgets and loans in your Google Drive.")
            Text("After reinstalling, sign in to the same app account, open this screen and restore using the same Google account. Google may ask you to choose an account or grant permission.")
            Text("Backups are manual. Always complete a backup before uninstalling. Restore uses the latest backup in the selected Google Drive and replaces this app account’s local records.")
            Button(
                onClick = { confirmBackup = true },
                enabled = !state.busy && state.restoreSummary == null,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Back up now") }
            OutlinedButton(
                onClick = { viewModel.start(BackupOperation.RESTORE) },
                enabled = !state.busy && state.restoreSummary == null,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Restore from Google Drive") }
            TextButton(
                onClick = { viewModel.start(BackupOperation.CHECK) },
                enabled = !state.busy && state.restoreSummary == null,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Check latest backup") }
            if (state.busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            state.message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            Text(
                "Backups are stored in Drive’s hidden app-data folder, not as visible files in My Drive. Each upload keeps earlier backups; only the latest is offered for restore. Backups use your Google account’s storage. Passwords and sign-in tokens are never included. App preferences are not included.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
    if (confirmBackup) {
        AlertDialog(
            onDismissRequest = { confirmBackup = false },
            title = { Text("Save current data to Drive?") },
            text = { Text("This becomes the latest backup. If you just reinstalled or are missing data, restore your previous backup first instead of uploading an empty one.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmBackup = false
                    viewModel.start(BackupOperation.BACKUP)
                }) { Text("Back up") }
            },
            dismissButton = { TextButton(onClick = { confirmBackup = false }) { Text("Cancel") } }
        )
    }
    state.restoreSummary?.let { summary ->
        AlertDialog(
            onDismissRequest = viewModel::cancelRestore,
            title = { Text("Replace local data?") },
            text = {
                Text("Backup from ${DateFormat.getDateTimeInstance().format(Date(summary.createdAt))}\n\n" +
                    "${summary.expenses} expenses, ${summary.categories} categories, ${summary.budgets} budgets, " +
                    "${summary.loanPeople} loan people and ${summary.loanTransactions} loan transactions.\n\n" +
                    "All current records for this app account will be replaced. Changes made after this backup will be lost.")
            },
            confirmButton = { TextButton(onClick = viewModel::confirmRestore) { Text("Replace and restore") } },
            dismissButton = { TextButton(onClick = viewModel::cancelRestore) { Text("Cancel") } }
        )
    }
}
