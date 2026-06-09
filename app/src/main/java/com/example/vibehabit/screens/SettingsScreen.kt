package com.example.vibehabit.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vibehabit.R
import com.example.vibehabit.components.SegmentedControl
import com.example.vibehabit.viewmodels.SettingsViewModel
import com.example.vibehabit.viewmodels.HabitsViewModel
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.text.TextStyle
import com.example.vibehabit.components.ProfileBlock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    habitsViewModel: HabitsViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val currentLanguage by settingsViewModel.language.collectAsState(initial = "en")

    // Визначаємо, яка вкладка активна (0 - English, 1 - Українська)
    val selectedLanguageIndex = if (currentLanguage == "uk") 1 else 0

    val languages = listOf(
        stringResource(R.string.language_english),
        stringResource(R.string.language_ukrainian)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            ProfileBlock(viewModel = habitsViewModel)
            Spacer(modifier = Modifier.height(24.dp))
            // Картка з налаштуванням теми
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dark_theme_label),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = onThemeChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.choose_language),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )

                SegmentedControl(
                    items = languages,
                    selectedIndex = selectedLanguageIndex,
                    onItemSelection = { index ->
                        val languageTag = if (index == 1) "uk" else "en"
                        settingsViewModel.setLanguage(languageTag)
                    }
                )
            }
        }
    }
}
