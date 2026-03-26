package com.example.gitphos.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.gitphos.ui.auth.AuthRoute
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
// Compose Runtime & Lifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Hilt Navigation
import androidx.hilt.navigation.compose.hiltViewModel

// Local Dashboard Components (Adjust package path if necessary)
import com.example.gitphos.ui.dashboard.DashboardViewModel
import com.example.gitphos.ui.dashboard.DashboardEffect
import com.example.gitphos.ui.dashboard.DashboardScreen
import com.example.gitphos.ui.picker.PickerEffect
import com.example.gitphos.ui.picker.PickerScreen
import com.example.gitphos.ui.picker.PickerViewModel

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

fun NavGraphBuilder.dashboardScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToPicker: () -> Unit,
    onNavigateToRepo: () -> Unit,
    onNavigateToSync: () -> Unit
) {
    composable(Screen.Dashboard.route) {
        val viewModel: DashboardViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    DashboardEffect.NavigateToAuth -> onNavigateToAuth()
                    DashboardEffect.NavigateToPicker -> onNavigateToPicker()
                    DashboardEffect.NavigateToRepo -> onNavigateToRepo()
                    DashboardEffect.NavigateToSync -> onNavigateToSync()
                    is DashboardEffect.ShowError -> { /* wire to snackbar in step 12.5 */ }
                }
            }
        }

        DashboardScreen(state = state, onEvent = viewModel::onEvent)
    }
}

fun NavGraphBuilder.pickerScreen(
    onNavigateBack: () -> Unit
) {
    composable(Screen.Picker.route) {
        val viewModel: PickerViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    PickerEffect.NavigateBack -> onNavigateBack()
                    is PickerEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                }
            }
        }

        PickerScreen(state = state, onEvent = viewModel::onEvent)
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