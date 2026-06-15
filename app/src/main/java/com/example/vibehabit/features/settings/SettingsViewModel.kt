package com.example.vibehabit.features.settings

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

val Context.settingsDataStore by preferencesDataStore(name = "settings_prefs")

@HiltViewModel
class SettingsViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.settingsDataStore
    private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")

    private val _isThemeLoaded = MutableStateFlow(false)
    val isThemeLoaded = _isThemeLoaded.asStateFlow()

    val isDarkTheme: StateFlow<Boolean?> = dataStore.data
        .map { preferences -> preferences[DARK_THEME_KEY] }
        .onEach { _isThemeLoaded.value = true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private val _language = MutableStateFlow(getCurrentLanguage())
    val language: StateFlow<String> = _language.asStateFlow()

    private fun getCurrentLanguage(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (locales.isEmpty) "en" else locales.toLanguageTags()
    }

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dataStore.edit { preferences ->
                    preferences[DARK_THEME_KEY] = isDark
                }
            }
        }
    }

    fun setLanguage(languageCode: String) {
        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
        _language.value = languageCode
    }
}
