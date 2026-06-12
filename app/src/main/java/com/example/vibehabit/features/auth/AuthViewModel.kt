package com.example.vibehabit.features.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibehabit.R
import com.example.vibehabit.shared_viewmodels.HabitRepository
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    application: Application,
    private val authRepository: AuthRepository,
    private val habitRepository: HabitRepository
) : AndroidViewModel(application) {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isEmailVerified = MutableStateFlow(true)
    val isEmailVerified: StateFlow<Boolean> = _isEmailVerified.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.getAuthStateFlow().collect { user ->
                if (user != null) {
                    _authState.value = AuthState.Authenticated(user)
                    _isEmailVerified.value = user.isEmailVerified
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            authRepository.signInWithGoogle(idToken).onFailure { e ->
                onError(e.localizedMessage ?: getApplication<Application>().getString(R.string.error_google_sign_in))
            }
        }
    }

    fun signUpWithEmail(email: String, pass: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            authRepository.signUpWithEmail(email, pass).onFailure { e ->
                onError(e.localizedMessage ?: getApplication<Application>().getString(R.string.error_registration))
            }
        }
    }

    fun signInWithEmail(email: String, pass: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            authRepository.signInWithEmail(email, pass).onFailure { e ->
                onError(e.localizedMessage ?: getApplication<Application>().getString(R.string.error_sign_in))
            }
        }
    }

    fun reloadUser(onResult: (Boolean) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            authRepository.reloadUser()
                .onSuccess { isVerified ->
                    _isEmailVerified.value = isVerified
                    onResult(isVerified)
                }
                .onFailure { e -> onError(e.localizedMessage ?: getApplication<Application>().getString(R.string.error_reload_user)) }
        }
    }

    fun resendVerificationEmail(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            authRepository.resendVerificationEmail()
                .onSuccess { onSuccess() }
                .onFailure { e -> onError(e.localizedMessage ?: getApplication<Application>().getString(R.string.error_resend_email)) }
        }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank()) {
            onError(getApplication<Application>().getString(R.string.error_enter_email))
            return
        }
        viewModelScope.launch {
            authRepository.resetPassword(email)
                .onSuccess { onSuccess() }
                .onFailure { e -> onError(e.localizedMessage ?: getApplication<Application>().getString(R.string.error_reset_password_send)) }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = authRepository.currentUser?.uid
        if (userId == null) {
            onError(getApplication<Application>().getString(R.string.error_user_not_found))
            return
        }

        viewModelScope.launch {
            try {
                withContext(NonCancellable) {
                    habitRepository.deleteAllUserData(userId).getOrThrow()
                    authRepository.deleteAccount().getOrThrow()
                }
                onSuccess()
            } catch (e: Exception) {
                val errorMessage = e.message ?: ""
                if (e is FirebaseAuthRecentLoginRequiredException ||
                    errorMessage.contains("CREDENTIAL_TOO_OLD_LOGIN_AGAIN") ||
                    errorMessage.contains("recent login")) {
                    onError(getApplication<Application>().getString(R.string.error_reauth_required))
                } else {
                    onError("${getApplication<Application>().getString(R.string.error_title).substringBefore(" ")}: ${e.localizedMessage}")
                }
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
}

