package com.example.vibehabit

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.vibehabit.core.navigation.AppNavigation
import com.example.vibehabit.core.notifications.NotificationHelper
import com.example.vibehabit.core.theme.ui.theme.HabitTrackerTheme
import com.example.vibehabit.features.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission result handled here
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Hold the splash screen until DataStore has emitted its first value (even if it's null)
        splashScreen.setKeepOnScreenCondition {
            !settingsViewModel.isThemeLoaded.value
        }

        enableEdgeToEdge()
        
        NotificationHelper.createNotificationChannel(this)
        checkNotificationPermission()

        setContent {
            val isLoaded by settingsViewModel.isThemeLoaded.collectAsState()
            val storedIsDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
            
            if (isLoaded) {
                // If storedIsDarkTheme is null, we fall back to the system theme
                val isDarkTheme = storedIsDarkTheme ?: isSystemInDarkTheme()

                HabitTrackerTheme(darkTheme = isDarkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(
                            isDarkTheme = isDarkTheme,
                            onThemeChange = { settingsViewModel.toggleTheme(it) }
                        )
                    }
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
