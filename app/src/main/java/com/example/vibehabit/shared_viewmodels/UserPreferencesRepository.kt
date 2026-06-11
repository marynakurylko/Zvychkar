package com.example.vibehabit.shared_viewmodels

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences> // Hilt автоматично передасть сюди DataStore з AppModule
) {
    private val ONBOARDING_KEY = booleanPreferencesKey("is_onboarding_completed")
    private val USERNAME_KEY = stringPreferencesKey("username")

    // Перетворюємо дані з DataStore на зручний Flow
    val isOnboardingCompletedFlow: Flow<Boolean?> = dataStore.data.map { preferences ->
        preferences[ONBOARDING_KEY]
    }

    val usernameFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USERNAME_KEY]
    }

    suspend fun completeOnboarding() {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_KEY] = true
        }
    }

    suspend fun updateUsername(newName: String) {
        dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = newName
        }
    }
}