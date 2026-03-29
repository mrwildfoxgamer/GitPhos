package com.example.gitphos.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.gitphos.ui.auth.AuthRoute

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
import com.example.gitphos.ui.repo.RepoEffect
import com.example.gitphos.ui.repo.RepoScreen
import com.example.gitphos.ui.repo.RepoViewModel
import com.example.gitphos.ui.sync.SyncEffect
import com.example.gitphos.ui.sync.SyncScreen
import com.example.gitphos.ui.sync.SyncViewModel
import kotlinx.coroutines.launch

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
            // FIX: Refresh the dashboard data every time we return to this screen
            viewModel.loadDashboard()

            viewModel.effect.collect { effect ->
                when (effect) {
                    DashboardEffect.NavigateToAuth -> onNavigateToAuth()
                    DashboardEffect.NavigateToPicker -> onNavigateToPicker()
                    DashboardEffect.NavigateToRepo -> onNavigateToRepo()
                    DashboardEffect.NavigateToSync -> onNavigateToSync()
                    is DashboardEffect.ShowError -> { /* wire to snackbar if needed */ }
                }
            }
        }
        DashboardScreen(state = state, onEvent = viewModel::onEvent)
    }
}

fun NavGraphBuilder.pickerScreen(onNavigateBack: () -> Unit) {
    composable(Screen.Picker.route) {
        val viewModel: PickerViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    PickerEffect.NavigateBack -> onNavigateBack()
                    is PickerEffect.ShowMessage -> launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
            }
        }
        PickerScreen(state = state, onEvent = viewModel::onEvent)
    }
}

fun NavGraphBuilder.repoScreen(onNavigateBack: () -> Unit) {
    composable(Screen.Repo.route) {
        // Restored standard setup here!
        val viewModel: RepoViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    RepoEffect.NavigateBack -> onNavigateBack()
                    is RepoEffect.ShowMessage -> launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
            }
        }
        RepoScreen(state = state, onEvent = viewModel::onEvent)
    }
}

fun NavGraphBuilder.syncScreen(onNavigateBack: () -> Unit) {
    composable(Screen.Sync.route) {
        // Restored standard setup here!
        val viewModel: SyncViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    SyncEffect.NavigateBack -> onNavigateBack()
                    is SyncEffect.ShowMessage -> launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
            }
        }
        SyncScreen(state = state, onEvent = viewModel::onEvent)
    }
}