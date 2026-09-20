package com.vegam.budgetcalculator.presentation.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vegam.budgetcalculator.presentation.navigation.Screen

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        Screen.Dashboard,
        Screen.Calendar,
        Screen.AddExpense, // We'll handle this specially if needed
        Screen.Analytics,
        Screen.More
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            val icon = when (screen) {
                                Screen.Dashboard -> Icons.Default.Dashboard
                                Screen.Calendar -> Icons.Default.CalendarMonth
                                Screen.AddExpense -> Icons.Default.AddCircle
                                Screen.Analytics -> Icons.Default.PieChart
                                Screen.More -> Icons.Default.Menu
                                else -> Icons.Default.Home
                            }
                            Icon(icon, contentDescription = screen.route)
                        },
                        label = { Text(screen.route.replaceFirstChar { it.uppercase() }) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                com.vegam.budgetcalculator.presentation.dashboard.DashboardScreen(
                    onAddExpenseClick = { navController.navigate(Screen.AddExpense.route) },
                    onCategoryClick = { /* TODO */ },
                    onExpenseClick = { /* TODO */ }
                )
            }
            composable(Screen.Calendar.route) {
                com.vegam.budgetcalculator.presentation.calendar.CalendarScreen()
            }
            composable(Screen.AddExpense.route) {
                com.vegam.budgetcalculator.presentation.expense.add.AddExpenseScreen(
                    onBackPressed = { navController.popBackStack() },
                    onExpenseAdded = { navController.popBackStack() }
                )
            }
            composable(Screen.Analytics.route) {
                com.vegam.budgetcalculator.presentation.analytics.AnalyticsScreen()
            }
            composable(Screen.More.route) {
                Column {
                    Text("More Settings")
                    Button(onClick = onLogout) {
                        Text("Logout")
                    }
                }
            }
        }
    }
}
