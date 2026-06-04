package com.example.vibehabit

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vibehabit.navigation.AppNavigation
import com.example.vibehabit.ui.theme.HabitTrackerTheme
import com.example.vibehabit.viewmodels.SettingsViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val storedIsDarkTheme by settingsViewModel.isDarkTheme.collectAsState(initial = null)
            val systemTheme = isSystemInDarkTheme()
            
            val isDarkTheme = storedIsDarkTheme ?: systemTheme

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
