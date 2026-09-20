package com.vegam.budgetcalculator.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vegam.budgetcalculator.domain.repository.AuthRepository
import com.vegam.budgetcalculator.domain.usecase.category.CreateDefaultCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val createDefaultCategoriesUseCase: CreateDefaultCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun register(name: String, email: String, password: String, confirm: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = RegisterUiState.Error("Fields cannot be empty")
            return
        }
        if (password != confirm) {
            _uiState.value = RegisterUiState.Error("Passwords do not match")
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            val result = authRepository.signUpWithFirebase(name, email, password)
            if (result.isSuccess) {
                val user = result.getOrNull()
                if (user != null) {
                    try {
                        createDefaultCategoriesUseCase(user.id)
                    } catch (e: Exception) {
                        // Suppress or handle default category insertion errors gracefully
                    }
                }
                _uiState.value = RegisterUiState.Success
            } else {
                _uiState.value = RegisterUiState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }
}

sealed interface RegisterUiState {
    data object Idle : RegisterUiState
    data object Loading : RegisterUiState
    data object Success : RegisterUiState
    data class Error(val message: String) : RegisterUiState
}
