package com.vegam.budgetcalculator.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vegam.budgetcalculator.presentation.auth.login.LoginScreen
import com.vegam.budgetcalculator.presentation.auth.register.RegisterScreen

@Composable
fun BudgetNavHost(
    navController: NavHostController,
    isLoggedIn: Boolean,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Dashboard.route) {
            com.vegam.budgetcalculator.presentation.main.MainScreen(
                onLogout = onLogout
            )
        }
    }
}
