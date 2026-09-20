package com.vegam.budgetcalculator.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vegam.budgetcalculator.domain.repository.AuthRepository
import com.vegam.budgetcalculator.domain.repository.BudgetRepository
import com.vegam.budgetcalculator.domain.repository.CategoryRepository
import com.vegam.budgetcalculator.domain.repository.ExpenseRepository
import com.vegam.budgetcalculator.domain.usecase.analytics.CategorySpending
import com.vegam.budgetcalculator.domain.usecase.analytics.DailySpending
import com.vegam.budgetcalculator.domain.usecase.analytics.GetCategorySpendingUseCase
import com.vegam.budgetcalculator.domain.usecase.analytics.GetDailySpendingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository,
    private val getCategorySpendingUseCase: GetCategorySpendingUseCase,
    private val getDailySpendingUseCase: GetDailySpendingUseCase
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth = _selectedMonth.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AnalyticsUiState> = combine(
        _selectedMonth,
        authRepository.currentUserFlow
    ) { month, user ->
        month to user
    }.flatMapLatest { (month, user) ->
        if (user == null) return@flatMapLatest flowOf(AnalyticsUiState.Error("User not logged in"))

        val startOfMonth = month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = month.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        combine(
            categoryRepository.observeCategories(user.id),
            expenseRepository.observeExpensesByDateRange(user.id, startOfMonth, endOfMonth),
            budgetRepository.observeMonthlyBudgets(user.id, month.year, month.monthValue)
        ) { categories, expenses, budgets ->
            val categorySpending = getCategorySpendingUseCase(categories, budgets, expenses)
            val dailySpending = getDailySpendingUseCase(expenses, month.atDay(1), month.atEndOfMonth())
            
            AnalyticsUiState.Success(
                AnalyticsData(
                    categorySpending = categorySpending,
                    dailySpending = dailySpending
                )
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsUiState.Loading)

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }
}

sealed interface AnalyticsUiState {
    data object Loading : AnalyticsUiState
    data class Success(val data: AnalyticsData) : AnalyticsUiState
    data class Error(val message: String) : AnalyticsUiState
}

data class AnalyticsData(
    val categorySpending: List<CategorySpending>,
    val dailySpending: List<DailySpending>
)
