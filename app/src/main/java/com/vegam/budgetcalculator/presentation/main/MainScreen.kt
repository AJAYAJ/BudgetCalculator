package com.vegam.budgetcalculator.presentation.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vegam.budgetcalculator.presentation.MainViewModel

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    mainViewModel: MainViewModel = hiltViewModel()
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
                MoreScreen(
                    onManageCategories = { navController.navigate(Screen.ManageCategories.route) },
                    onLogout = onLogout
                )
            }
            composable(Screen.ManageCategories.route) {
                com.vegam.budgetcalculator.presentation.category.ManageCategoriesScreen(
                    onBackPressed = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun MoreScreen(
    onManageCategories: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("More", style = MaterialTheme.typography.headlineMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Card(onClick = onManageCategories, modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text("Manage Categories") },
                supportingContent = { Text("Create and edit categories, icons, colors and subcategories") },
                leadingContent = { Icon(Icons.Default.Category, contentDescription = null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
            )
        }
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Text("  Logout")
        }
    }
}
