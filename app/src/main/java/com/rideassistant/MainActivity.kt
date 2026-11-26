package com.rideassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rideassistant.ui.RideAssistantViewModel
import com.rideassistant.ui.navigation.NavRoutes
import com.rideassistant.ui.screens.*
import com.rideassistant.ui.theme.RideAssistantTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RideAssistantTheme {
                RideAssistantRoot()
            }
        }
    }
}

@Composable
fun RideAssistantRoot(viewModel: RideAssistantViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val settings by viewModel.settings.collectAsState()
    val captureState by viewModel.captureState.collectAsState()
    val latestOffer by viewModel.latestOffer.collectAsState()
    val rideHistory by viewModel.rideHistory.collectAsState()
    val homeLocation by viewModel.homeLocation.collectAsState()
    val analytics by viewModel.analyticsSummary.collectAsState()
    val diagnostics by viewModel.diagnostics.collectAsState()

    val startDestination = if (settings.onboardingCompleted) NavRoutes.HOME else NavRoutes.ONBOARDING
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val bottomTabs = listOf(
        Triple(NavRoutes.HOME, "Home", Icons.Default.DirectionsCar),
        Triple(NavRoutes.RULES, "Rules", Icons.Default.Tune),
        Triple(NavRoutes.HOME_PRIORITY, "My Home", Icons.Default.Home),
        Triple(NavRoutes.HISTORY, "History", Icons.Default.History),
        Triple(NavRoutes.ANALYTICS, "Stats", Icons.Default.Analytics)
    )

    Scaffold(
        bottomBar = {
            if (currentRoute != NavRoutes.ONBOARDING) {
                NavigationBar {
                    bottomTabs.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            selected = currentRoute == route,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(NavRoutes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(NavRoutes.ONBOARDING) {
                OnboardingScreen(
                    onComplete = {
                        viewModel.completeOnboarding()
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                        }
                    },
                    onSaveHomeLocation = { name, lat, lon ->
                        viewModel.saveHomeLocation(name, lat, lon)
                    },
                    onSaveInitialMinFare = { minFare ->
                        viewModel.saveInitialMinFare(minFare)
                    }
                )
            }
            composable(NavRoutes.HOME) {
                HomeScreen(
                    settings = settings,
                    captureState = captureState,
                    latestOffer = latestOffer,
                    homeLocation = homeLocation,
                    analytics = analytics,
                    onToggleMonitoring = viewModel::toggleMonitoring,
                    onToggleHomePriority = viewModel::toggleHomePriority,
                    onToggleBubble = viewModel::toggleBubble,
                    onNavigate = { navController.navigate(it) }
                )
            }
            composable(NavRoutes.RULES) {
                RulesScreen(
                    settings = settings,
                    onSaveRules = viewModel::updateRules
                )
            }
            composable(NavRoutes.HOME_PRIORITY) {
                HomePriorityScreen(
                    homePriorityEnabled = settings.homePriorityEnabled,
                    homeLocation = homeLocation,
                    onToggleHomePriority = viewModel::toggleHomePriority,
                    onSaveHome = viewModel::saveHomeLocation
                )
            }
            composable(NavRoutes.HISTORY) {
                HistoryScreen(
                    rides = rideHistory,
                    onDeleteRide = viewModel::deleteRide,
                    onClearAll = viewModel::clearAllHistory
                )
            }
            composable(NavRoutes.ANALYTICS) {
                AnalyticsScreen(summary = analytics)
            }
            composable(NavRoutes.DIAGNOSTICS) {
                DiagnosticsScreen(
                    checks = diagnostics,
                    onRefresh = viewModel::refreshDiagnostics
                )
            }
            composable(NavRoutes.PRIVACY) {
                PrivacyScreen(
                    onClearHistory = viewModel::clearAllHistory,
                    onClearHome = viewModel::clearHomeLocation
                )
            }
            composable(NavRoutes.GUIDE) {
                GuideScreen()
            }
        }
    }
}
