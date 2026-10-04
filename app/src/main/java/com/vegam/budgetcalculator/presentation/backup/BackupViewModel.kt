package com.vegam.budgetcalculator.presentation.backup

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.ClearTokenRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.vegam.budgetcalculator.data.backup.BackupRepository
import com.vegam.budgetcalculator.data.backup.BackupSnapshot
import com.vegam.budgetcalculator.data.backup.DriveAuthorizationExpired
import com.vegam.budgetcalculator.data.backup.DriveBackupService
import com.vegam.budgetcalculator.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repository: BackupRepository,
    private val auth: AuthRepository
) : ViewModel() {
    private val authorization = Identity.getAuthorizationClient(context)
    private val promptDirectory = context.noBackupFilesDir
    private val _state = MutableStateFlow(BackupUiState())
    val state = _state.asStateFlow()
    private val _showRestorePrompt = MutableStateFlow(false)
    val showRestorePrompt = _showRestorePrompt.asStateFlow()
    private var operation: BackupOperation? = null
    private var operationUserId: String? = null
    private var pendingSnapshot: BackupSnapshot? = null

    init {
        viewModelScope.launch {
            auth.currentUserFlow.collect { user ->
                _showRestorePrompt.value = user != null && withContext(Dispatchers.IO) {
                    !promptFile(user.id).exists()
                }
            }
        }
    }

    fun dismissRestorePrompt() {
        _showRestorePrompt.value = false
        val userId = auth.getCurrentUserId() ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                promptFile(userId).writeText("shown")
            } catch (_: java.io.IOException) {
                // A failed marker write only causes the prompt to appear again next launch.
            }
        }
    }

    private fun promptFile(userId: String): File {
        val hash = MessageDigest.getInstance("SHA-256").digest(userId.toByteArray())
            .joinToString("") { "%02x".format(it) }
        return File(promptDirectory, "drive-restore-prompt-$hash")
    }

    fun start(requestedOperation: BackupOperation) {
        if (_state.value.busy) return
        val userId = auth.getCurrentUserId() ?: run {
            _state.value = BackupUiState(message = "Sign in to your app account first.")
            return
        }
        operation = requestedOperation
        operationUserId = userId
        pendingSnapshot = null
        _state.value = BackupUiState(busy = true, message = "Connecting to Google Drive…")
        viewModelScope.launch {
            try {
                val request = AuthorizationRequest.builder()
                    .setRequestedScopes(listOf(Scope(DriveBackupService.SCOPE)))
                    .build()
                val result = authorization.authorize(request).await()
                if (result.hasResolution()) {
                    _state.value = _state.value.copy(authorizationIntent = checkNotNull(result.pendingIntent))
                } else {
                    execute(checkNotNull(result.accessToken))
                }
            } catch (e: Exception) {
                fail(e)
            }
        }
    }

    fun authorizationLaunched() {
        _state.value = _state.value.copy(authorizationIntent = null)
    }

    fun authorizationResult(resultCode: Int, intent: Intent?) {
        if (operation == null) {
            _state.value = BackupUiState(message = "Please tap the backup or restore button again.")
            return
        }
        viewModelScope.launch {
            try {
                if (intent != null) {
                    val result = authorization.getAuthorizationResultFromIntent(intent)
                    execute(checkNotNull(result.accessToken))
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    cancelAuthorization()
                } else {
                    _state.value = BackupUiState(message = "Google Drive authorization failed (code $resultCode).")
                }
            } catch (e: Exception) {
                fail(e)
            }
        }
    }

    fun cancelAuthorization() {
        operation = null
        _state.value = BackupUiState(message = "Google Drive connection cancelled. Your data is unchanged.")
    }

    private suspend fun execute(token: String) {
        try {
            val userId = checkNotNull(operationUserId)
            check(auth.getCurrentUserId() == userId) { "Your app account changed. Please try again." }
            when (operation) {
                BackupOperation.CHECK -> {
                    val file = repository.latest(token, userId)
                    _state.value = BackupUiState(message = if (file == null)
                        "No backup found. Check that you selected the same Google account and signed in to the same app account."
                    else "Latest backup in this Google Drive: ${file.modifiedTime}")
                }
                BackupOperation.BACKUP -> {
                    _state.value = BackupUiState(busy = true, message = "Uploading backup…")
                    repository.backup(token, userId)
                    _state.value = BackupUiState(message = "Backup saved to Google Drive successfully. You can restore it after reinstalling.")
                }
                BackupOperation.RESTORE -> {
                    _state.value = BackupUiState(busy = true, message = "Downloading and validating backup…")
                    val snapshot = repository.prepareRestore(token, userId)
                    pendingSnapshot = snapshot
                    _state.value = BackupUiState(restoreSummary = RestoreSummary(
                        snapshot.createdAt, snapshot.expenses.size, snapshot.categories.size,
                        snapshot.budgets.size, snapshot.loanPeople.size, snapshot.loanTransactions.size
                    ))
                }
                null -> error("Please try again.")
            }
        } catch (e: DriveAuthorizationExpired) {
            authorization.clearToken(ClearTokenRequest.builder().setToken(token).build()).await()
            throw e
        } finally {
            operation = null
        }
    }

    fun cancelRestore() {
        pendingSnapshot = null
        _state.value = BackupUiState(message = "Restore cancelled. Your data is unchanged.")
    }

    fun confirmRestore() {
        val snapshot = pendingSnapshot ?: return
        pendingSnapshot = null
        _state.value = BackupUiState(busy = true, message = "Restoring data…")
        viewModelScope.launch {
            try {
                repository.restore(snapshot)
                _state.value = BackupUiState(message = "Restore complete. Your expenses, categories, budgets and loans are available.")
            } catch (e: Exception) {
                fail(e)
            }
        }
    }

    private fun fail(e: Exception) {
        if (e is CancellationException) throw e
        operation = null
        pendingSnapshot = null
        _state.value = BackupUiState(message = when (e) {
            is kotlinx.serialization.SerializationException -> "The backup could not be read. No local data was changed."
            is com.google.android.gms.common.api.ApiException -> {
                when (e.statusCode) {
                    16, 12501 -> "Google Drive connection cancelled. Your data is unchanged."
                    10 -> "Google Drive setup incomplete (Error 10: DEVELOPER_ERROR).\n\nTo fix this:\n1. Enable 'Google Drive API' in Google Cloud Console.\n2. Create an Android OAuth Client ID for package 'com.vegam.budgetcalculator' with your app's SHA-1 certificate fingerprint.\n3. Add your Google account under 'Test users' in Google Cloud Console if OAuth status is Testing."
                    12500 -> "Google Sign-In failed (Error 12500). Please ensure Google Play Services is updated and active on this device."
                    7 -> "Network error. Please check your internet connection and try again."
                    else -> "Google authorization failed (Error ${e.statusCode}). Please check your Google Cloud Console OAuth setup."
                }
            }
            else -> e.message ?: "Backup operation failed. Please try again."
        })
    }
}

enum class BackupOperation { CHECK, BACKUP, RESTORE }

data class BackupUiState(
    val busy: Boolean = false,
    val message: String? = null,
    val authorizationIntent: PendingIntent? = null,
    val restoreSummary: RestoreSummary? = null
)

data class RestoreSummary(
    val createdAt: Long,
    val expenses: Int,
    val categories: Int,
    val budgets: Int,
    val loanPeople: Int,
    val loanTransactions: Int
)
