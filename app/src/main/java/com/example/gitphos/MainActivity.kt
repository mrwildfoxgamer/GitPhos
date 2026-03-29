package com.example.gitphos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.gitphos.ui.navigation.Screen
import com.example.gitphos.ui.navigation.authScreen
import com.example.gitphos.ui.navigation.dashboardScreen
import com.example.gitphos.ui.navigation.pickerScreen
import com.example.gitphos.ui.navigation.repoScreen // Added import
import com.example.gitphos.ui.navigation.syncScreen
import com.example.gitphos.ui.theme.GitPhosTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GitPhosTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.Auth.route // Removed the errant repoScreen from here
                ) {
                    authScreen(navController)

                    dashboardScreen(
                        onNavigateToAuth = { navController.navigate(Screen.Auth.route) { popUpTo(0) } },
                        onNavigateToPicker = { navController.navigate(Screen.Picker.route) },
                        onNavigateToRepo = { navController.navigate(Screen.Repo.route) },
                        onNavigateToSync = { navController.navigate(Screen.Sync.route) }
                    )

                    pickerScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )

                    // Moved repoScreen down here into the graph builder!
                    repoScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )

                    syncScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )  // syncScreen(navController)       ← Step 12.5
                }
            }
        }
    }
}