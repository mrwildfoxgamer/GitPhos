package com.example.gitphos

import android.net.Uri
import android.provider.Settings
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
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