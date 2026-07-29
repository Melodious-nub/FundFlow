package com.shawon.fundflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shawon.fundflow.core.common.Screen
import com.shawon.fundflow.ui.analytics.AnalyticsScreen
import com.shawon.fundflow.ui.budget.BudgetSetupScreen
import com.shawon.fundflow.ui.dashboard.DashboardScreen
import com.shawon.fundflow.ui.expense.ExpenseScreen
import com.shawon.fundflow.ui.history.HistoryScreen
import com.shawon.fundflow.ui.onboarding.OnboardingScreen
import com.shawon.fundflow.ui.settings.SettingsScreen
import com.shawon.fundflow.ui.splash.SplashScreen
import com.shawon.fundflow.ui.theme.FundFlowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FundFlowTheme {
                FundFlowAppContent()
            }
        }
    }
}

@Composable
fun FundFlowAppContent() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash,
        modifier = Modifier.fillMaxSize()
    ) {
        composable<Screen.Splash> {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding) {
                        popUpTo(Screen.Splash) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard) {
                        popUpTo(Screen.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Onboarding> {
            OnboardingScreen(
                onNavigateToBudgetSetup = {
                    navController.navigate(Screen.BudgetSetup) {
                        popUpTo(Screen.Onboarding) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.BudgetSetup> {
            BudgetSetupScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard) {
                        popUpTo(Screen.BudgetSetup) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Dashboard> {
            DashboardScreen(
                onNavigateToAddExpense = {
                    navController.navigate(Screen.ExpenseForm)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History)
                },
                onNavigateToAnalytics = {
                    navController.navigate(Screen.Analytics)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings)
                }
            )
        }

        composable<Screen.ExpenseForm> {
            ExpenseScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.Analytics> {
            AnalyticsScreen()
        }

        composable<Screen.History> {
            HistoryScreen()
        }

        composable<Screen.Settings> {
            SettingsScreen()
        }
    }
}
