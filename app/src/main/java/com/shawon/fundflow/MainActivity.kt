package com.shawon.fundflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shawon.fundflow.core.common.Screen
import com.shawon.fundflow.ui.analytics.AnalyticsScreen
import com.shawon.fundflow.ui.budget.BudgetSetupScreen
import com.shawon.fundflow.ui.dashboard.DashboardScreen
import com.shawon.fundflow.ui.expense.ExpenseScreen
import com.shawon.fundflow.ui.history.HistoryScreen
import com.shawon.fundflow.ui.onboarding.OnboardingScreen
import com.shawon.fundflow.ui.settings.AboutScreen
import com.shawon.fundflow.ui.settings.SettingsScreen
import com.shawon.fundflow.ui.splash.SplashScreen
import com.shawon.fundflow.ui.theme.FundFlowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarScreens = listOf(
        Screen.Dashboard,
        Screen.History,
        Screen.Analytics,
        Screen.Settings
    )

    val showBottomBar = bottomBarScreens.any { screen ->
        currentDestination?.hierarchy?.any { it.route?.contains(screen::class.simpleName ?: "") == true } == true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // Key fix for nested padding
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val items = listOf(
                        Triple("Home", Icons.Default.Dashboard, Screen.Dashboard),
                        Triple("History", Icons.AutoMirrored.Filled.List, Screen.History),
                        Triple("Analytics", Icons.Default.Analytics, Screen.Analytics),
                        Triple("Settings", Icons.Default.Settings, Screen.Settings)
                    )

                    items.forEach { (label, icon, screen) ->
                        val isSelected = currentDestination?.hierarchy?.any { 
                            it.route?.contains(screen::class.simpleName ?: "") == true 
                        } == true
                        
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen) {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash,
            modifier = Modifier.padding(innerPadding), // Bottom padding for Nav Bar
            enterTransition = { fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) }
        ) {
            composable<Screen.Splash> {
                SplashScreen(
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding) {
                            popUpTo(Screen.Splash) { inclusive = true }
                        }
                    },
                    onNavigateToBudgetSetup = {
                        navController.navigate(Screen.BudgetSetup) {
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
                    onNavigateToBudgetSetup = {
                        navController.navigate(Screen.BudgetSetup)
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
                SettingsScreen(
                    onNavigateToAbout = {
                        navController.navigate(Screen.About)
                    }
                )
            }

            composable<Screen.About> {
                AboutScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
