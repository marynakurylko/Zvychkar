package com.example.vibehabit.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.example.vibehabit.R
import com.example.vibehabit.components.SegmentedControl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val currentLocale = AppCompatDelegate.getApplicationLocales().toLanguageTags()

    // Визначаємо, яка вкладка активна (0 - English, 1 - Українська)
    var selectedLanguageIndex by remember {
        mutableIntStateOf(if (currentLocale.contains("uk")) 1 else 0)
    }

    val languages = listOf(
        stringResource(R.string.language_english),
        stringResource(R.string.language_ukrainian)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                // Використовуємо локалізований рядок для заголовка
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
                    // Сучасний перемикач
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
                        selectedLanguageIndex = index
                        // Магія перемикання мови
                        val languageTag = if (index == 1) "uk" else "en"
                        val appLocale = LocaleListCompat.forLanguageTags(languageTag)
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    }
                )
            }
        }
    }
}