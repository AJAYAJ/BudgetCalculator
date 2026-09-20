package com.vegam.budgetcalculator.presentation.expense.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vegam.budgetcalculator.domain.model.Category
import com.vegam.budgetcalculator.domain.model.Expense
import com.vegam.budgetcalculator.domain.model.SubCategory
import com.vegam.budgetcalculator.domain.repository.AuthRepository
import com.vegam.budgetcalculator.domain.repository.CategoryRepository
import com.vegam.budgetcalculator.domain.usecase.expense.AddExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val categoryRepository: CategoryRepository,
    private val addExpenseUseCase: AddExpenseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddExpenseUiState>(AddExpenseUiState.Idle)
    val uiState = _uiState.asStateFlow()

    val categories: StateFlow<List<Category>> = authRepository.currentUserFlow
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else categoryRepository.observeCategories(user.id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val selectedCategoryId = MutableStateFlow<String?>(null)

    val subCategories: StateFlow<List<SubCategory>> = selectedCategoryId
        .flatMapLatest { categoryId ->
            if (categoryId == null) flowOf(emptyList())
            else categoryRepository.observeSubCategories(categoryId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(categoryId: String) {
        selectedCategoryId.value = categoryId
    }

    fun addExpense(
        amount: String,
        categoryId: String,
        subCategoryId: String?,
        notes: String,
        timestamp: Long
    ) {
        val amountMinor = try {
            (amount.toDouble() * 100).toLong()
        } catch (e: Exception) {
            _uiState.value = AddExpenseUiState.Error("Invalid amount")
            return
        }

        val userId = authRepository.getCurrentUserId() ?: return

        val expense = Expense(
            id = UUID.randomUUID().toString(),
            userId = userId,
            categoryId = categoryId,
            subCategoryId = subCategoryId,
            amountMinor = amountMinor,
            notes = notes,
            dateTime = timestamp,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            _uiState.value = AddExpenseUiState.Loading
            val result = addExpenseUseCase(expense)
            if (result.isSuccess) {
                _uiState.value = AddExpenseUiState.Success
            } else {
                _uiState.value = AddExpenseUiState.Error(result.exceptionOrNull()?.message ?: "Failed to save expense")
            }
        }
    }
}

sealed interface AddExpenseUiState {
    data object Idle : AddExpenseUiState
    data object Loading : AddExpenseUiState
    data object Success : AddExpenseUiState
    data class Error(val message: String) : AddExpenseUiState
}
