package com.example.lume.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lume.ui.components.FinanceBottomBar
import com.example.lume.ui.screens.dashboard.DashboardScreen
import com.example.lume.ui.screens.receiver.ShareReceiverScreen
import android.net.Uri

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Dashboard.route,
    uris: List<Uri> = emptyList(), // Passed for receiver flow
    onFinishReceiver: () -> Unit = {}
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    Scaffold(
        bottomBar = {
            FinanceBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Dashboard.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        // Make background explicitly dark to match theme
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(navController = navController)
            }
            composable(Screen.Receiver.route) {
                ShareReceiverScreen(uris = uris, onDone = onFinishReceiver)
            }
            // Placeholders
            composable(Screen.Insights.route) { DashboardScreen(navController) }
            composable(Screen.Gastos.route) { DashboardScreen(navController) }
            composable(Screen.Perfil.route) { DashboardScreen(navController) }
            composable(Screen.Scan.route) {
                // Determine what Scan does. For now placeholder.
                DashboardScreen(navController) 
            }
        }
    }
}
