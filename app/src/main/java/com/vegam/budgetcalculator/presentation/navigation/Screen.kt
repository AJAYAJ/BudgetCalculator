package com.vegam.budgetcalculator.presentation.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ForgotPassword : Screen("forgot_password")
    
    data object Dashboard : Screen("dashboard")
    data object Calendar : Screen("calendar")
    data object AddExpense : Screen("add_expense")
    data object Analytics : Screen("analytics")
    data object More : Screen("more")
    
    data object ManageCategories : Screen("manage_categories")
    data object CategoryDetails : Screen("category_details/{categoryId}") {
        fun createRoute(categoryId: String) = "category_details/$categoryId"
    }
}
