package com.vegam.budgetcalculator.domain.usecase.analytics

import com.vegam.budgetcalculator.domain.model.Expense
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class DailySpending(
    val date: LocalDate,
    val amountMinor: Long
)

class GetDailySpendingUseCase @Inject constructor() {
    operator fun invoke(expenses: List<Expense>, startDate: LocalDate, endDate: LocalDate): List<DailySpending> {
        val expenseMap = expenses.groupBy {
            Instant.ofEpochMilli(it.dateTime).atZone(ZoneId.systemDefault()).toLocalDate()
        }

        val result = mutableListOf<DailySpending>()
        var current = startDate
        while (!current.isAfter(endDate)) {
            val amount = expenseMap[current]?.sumOf { it.amountMinor } ?: 0L
            result.add(DailySpending(current, amount))
            current = current.plusDays(1)
        }
        return result
    }
}
