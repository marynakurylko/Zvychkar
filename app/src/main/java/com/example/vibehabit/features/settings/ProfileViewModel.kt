package com.example.vibehabit.features.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibehabit.R
import com.example.vibehabit.features.auth.AuthRepository
import com.example.vibehabit.shared_viewmodels.HabitRepository
import com.example.vibehabit.shared_viewmodels.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    application: Application,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
    private val habitRepository: HabitRepository
) : AndroidViewModel(application) {

    private val _isOnboardingCompleted = MutableStateFlow<Boolean?>(null)
    val isOnboardingCompleted: StateFlow<Boolean?> = _isOnboardingCompleted.asStateFlow()

    private val defaultUser = application.getString(R.string.default_username)
    private val _username = MutableStateFlow<String>(defaultUser)
    val username: StateFlow<String> = _username.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.isOnboardingCompletedFlow.collect { isCompleted ->
                _isOnboardingCompleted.value = isCompleted ?: false
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.usernameFlow.collect { name ->
                _username.value = name ?: defaultUser
            }
        }
        // Автоматично ставимо ім'я з пошти при першому логіні
        viewModelScope.launch {
            authRepository.getAuthStateFlow().collect { user ->
                if (user != null && _username.value == defaultUser && user.email != null) {
                    updateUsername(user.email!!.substringBefore("@"))
                }
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch { userPreferencesRepository.completeOnboarding() }
    }

    fun updateUsername(newName: String) {
        viewModelScope.launch { userPreferencesRepository.updateUsername(newName) }
    }

    fun sendFeedback(message: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = authRepository.currentUser
        if (user == null) {
            onError(getApplication<Application>().getString(R.string.error_not_authorized))
            return
        }
        viewModelScope.launch {
            val email = user.email ?: getApplication<Application>().getString(R.string.no_email_provided)
            habitRepository.sendFeedback(user.uid, email, message)
                .onSuccess { onSuccess() }
                .onFailure { e -> onError(e.localizedMessage ?: getApplication<Application>().getString(R.string.error_feedback_send)) }
        }
    }
}

