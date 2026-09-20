package com.vegam.budgetcalculator.core.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val locale = Locale("en", "IN")
    
    fun formatMinorUnits(amountMinor: Long): String {
        val amountDouble = amountMinor.toDouble() / 100.0
        val formatter = NumberFormat.getCurrencyInstance(locale)
        return formatter.format(amountDouble)
    }

    fun formatMinorUnitsNoCurrencySymbol(amountMinor: Long): String {
        val amountDouble = amountMinor.toDouble() / 100.0
        val formatter = NumberFormat.getInstance(locale)
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return formatter.format(amountDouble)
    }
}
