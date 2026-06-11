package com.example.vibehabit.viewmodels

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val Context.settingsDataStore by preferencesDataStore(name = "settings_prefs")

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.settingsDataStore

    private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    private val LANGUAGE_KEY = stringPreferencesKey("language")

    val isDarkTheme: Flow<Boolean?> = dataStore.data
        .map { preferences -> preferences[DARK_THEME_KEY] }

    val language: Flow<String> = dataStore.data
        .map { preferences -> preferences[LANGUAGE_KEY] ?: "en" }

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[DARK_THEME_KEY] = isDark
            }
        }
    }

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[LANGUAGE_KEY] = languageCode
            }
            // Також оновлюємо AppCompatDelegate для миттєвої зміни мови в системі
            val appLocale = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }
}
