package com.vegam.budgetcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.vegam.budgetcalculator.presentation.MainViewModel
import com.vegam.budgetcalculator.presentation.navigation.BudgetNavHost
import com.vegam.budgetcalculator.ui.theme.BudgetCalculatorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val user by viewModel.currentUser.collectAsState()
            val navController = rememberNavController()

            BudgetCalculatorTheme {
                BudgetNavHost(
                    navController = navController,
                    isLoggedIn = user != null,
                    onLogout = { viewModel.logout() }
                )
            }
        }
    }
}
