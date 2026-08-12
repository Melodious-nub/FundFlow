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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.shawon.fundflow.core.common.Screen
import com.shawon.fundflow.core.update.UpdateManager
import com.shawon.fundflow.ui.analytics.AnalyticsScreen
import com.shawon.fundflow.ui.backup.BackupScreen
import com.shawon.fundflow.ui.backup.CloudBackupScreen
import com.shawon.fundflow.ui.backup.LocalBackupScreen
import com.shawon.fundflow.ui.budget.BudgetSetupScreen
import com.shawon.fundflow.ui.cycle.CycleManagementScreen
import com.shawon.fundflow.ui.dashboard.DashboardScreen
import com.shawon.fundflow.ui.expense.ExpenseScreen
import com.shawon.fundflow.ui.history.HistoryScreen
import com.shawon.fundflow.ui.onboarding.OnboardingScreen
import com.shawon.fundflow.ui.settings.AboutScreen
import com.shawon.fundflow.ui.settings.AppUpdateScreen
import com.shawon.fundflow.ui.settings.SettingsScreen
import com.shawon.fundflow.ui.splash.SplashScreen
import com.shawon.fundflow.ui.theme.FundFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var updateManager: UpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FundFlowTheme {
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_START -> updateManager.registerReceiver()
                            Lifecycle.Event.ON_STOP -> updateManager.unregisterReceiver()
                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        updateManager.unregisterReceiver()
                    }
                }
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
                        navController.navigate(Screen.BudgetSetup()) {
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
                        navController.navigate(Screen.BudgetSetup())
                    },
                    onNavigateToRecovery = {
                        navController.navigate(Screen.Backup(isInitialSetup = true))
                    }
                )
            }

            composable<Screen.BudgetSetup> {
                BudgetSetupScreen(
                    onNavigateToDashboard = {
                        navController.navigate(Screen.Dashboard) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable<Screen.Dashboard> {
                DashboardScreen(
                    onNavigateToAddExpense = {
                        navController.navigate(Screen.ExpenseForm)
                    },
                    onNavigateToBudgetSetup = { cycleId ->
                        navController.navigate(Screen.BudgetSetup(cycleId))
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

            composable<Screen.CycleManagement> {
                CycleManagementScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onEditCycle = { cycleId ->
                        navController.navigate(Screen.BudgetSetup(cycleId))
                    }
                )
            }

            composable<Screen.Settings> {
                SettingsScreen(
                    onNavigateToAbout = {
                        navController.navigate(Screen.About)
                    },
                    onNavigateToCycleManagement = {
                        navController.navigate(Screen.CycleManagement)
                    },
                    onNavigateToBackup = {
                        navController.navigate(Screen.Backup(isInitialSetup = false))
                    },
                    onNavigateToUpdate = {
                        navController.navigate(Screen.AppUpdate)
                    }
                )
            }

            composable<Screen.AppUpdate> {
                AppUpdateScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable<Screen.Backup> { backStackEntry ->
                val args = backStackEntry.toRoute<Screen.Backup>()
                BackupScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToLocalBackup = {
                        navController.navigate(Screen.LocalBackup(isInitialSetup = args.isInitialSetup))
                    },
                    onNavigateToCloudBackup = {
                        navController.navigate(Screen.CloudBackup(isInitialSetup = args.isInitialSetup))
                    },
                    isInitialSetup = args.isInitialSetup
                )
            }

            composable<Screen.LocalBackup> { backStackEntry ->
                val args = backStackEntry.toRoute<Screen.LocalBackup>()
                LocalBackupScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onRestoreSuccess = {
                        navController.navigate(Screen.Dashboard) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    isInitialSetup = args.isInitialSetup
                )
            }

            composable<Screen.CloudBackup> { backStackEntry ->
                val args = backStackEntry.toRoute<Screen.CloudBackup>()
                CloudBackupScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onRestoreSuccess = {
                        navController.navigate(Screen.Dashboard) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    isInitialSetup = args.isInitialSetup
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
