package com.example.gitphos.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.gitphos.ui.auth.AuthRoute

// --- Routes ---

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Dashboard : Screen("dashboard")
    data object Picker : Screen("picker")
    data object Repo : Screen("repo")
    data object Sync : Screen("sync")
}

// --- Auth Nav Entry ---

fun NavGraphBuilder.authScreen(navController: NavHostController) {
    composable(route = Screen.Auth.route) {
        AuthRoute(
            onNavigateToDashboard = {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            }
        )
    }
}

// --- Root NavHost (wire all screens here as they are built) ---
// Example usage in MainActivity or AppNavHost:
//
// @Composable
// fun AppNavHost(
//     navController: NavHostController = rememberNavController(),
//     startDestination: String = Screen.Auth.route,
// ) {
//     NavHost(navController = navController, startDestination = startDestination) {
//         authScreen(navController)
//         // dashboardScreen(navController)  ← Step 12.2
//         // pickerScreen(navController)     ← Step 12.3
//         // repoScreen(navController)       ← Step 12.4
//         // syncScreen(navController)       ← Step 12.5
//     }
// }