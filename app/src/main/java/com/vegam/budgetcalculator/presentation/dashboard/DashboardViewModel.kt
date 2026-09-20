package com.vegam.budgetcalculator.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vegam.budgetcalculator.domain.model.Expense
import com.vegam.budgetcalculator.domain.repository.AuthRepository
import com.vegam.budgetcalculator.domain.repository.BudgetRepository
import com.vegam.budgetcalculator.domain.repository.CategoryRepository
import com.vegam.budgetcalculator.domain.repository.ExpenseRepository
import com.vegam.budgetcalculator.domain.usecase.analytics.CategorySpending
import com.vegam.budgetcalculator.domain.usecase.analytics.GetCategorySpendingUseCase
import com.vegam.budgetcalculator.domain.usecase.budget.GetMonthlySummaryUseCase
import com.vegam.budgetcalculator.domain.usecase.budget.MonthlySummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository,
    private val getMonthlySummaryUseCase: GetMonthlySummaryUseCase,
    private val getCategorySpendingUseCase: GetCategorySpendingUseCase
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth = _selectedMonth.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> = combine(
        _selectedMonth,
        authRepository.currentUserFlow
    ) { month, user ->
        month to user
    }.flatMapLatest { (month, user) ->
        if (user == null) return@flatMapLatest flowOf(DashboardUiState.Error("User not logged in"))

        val startOfMonth = month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = month.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        combine(
            categoryRepository.observeCategories(user.id),
            expenseRepository.observeExpensesByDateRange(user.id, startOfMonth, endOfMonth),
            budgetRepository.observeMonthlyBudgets(user.id, month.year, month.monthValue)
        ) { categories, expenses, budgets ->
            val summary = getMonthlySummaryUseCase(budgets, expenses)
            val categorySpending = getCategorySpendingUseCase(categories, budgets, expenses)
            
            DashboardUiState.Success(
                DashboardData(
                    month = month,
                    summary = summary,
                    categorySpending = categorySpending,
                    recentExpenses = expenses.take(10)
                )
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState.Loading)

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }
}

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(val data: DashboardData) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

data class DashboardData(
    val month: YearMonth,
    val summary: MonthlySummary,
    val categorySpending: List<CategorySpending>,
    val recentExpenses: List<Expense>
)
