package com.example.vibehabit.shared_viewmodels

import android.app.Application
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibehabit.R
import com.example.vibehabit.features.auth.AuthState
import com.example.vibehabit.core.models.Habit
import com.example.vibehabit.core.ui.UiState
import com.example.vibehabit.features.create_habit.SaveHabitUseCase
import com.example.vibehabit.core.notifications.NotificationHelper
import com.example.vibehabit.features.auth.AuthRepository
import com.example.vibehabit.core.widget.HabitWidget
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HabitsViewModel @Inject constructor(
    application: Application,
    private val authRepository: AuthRepository,
    private val habitRepository: HabitRepository,
    // ВИДАЛЕНО: private val saveHabitUseCase: SaveHabitUseCase
    private val userPreferencesRepository: UserPreferencesRepository
) : AndroidViewModel(application) {
    private val _isOnboardingCompleted = MutableStateFlow<Boolean?>(null)
    val isOnboardingCompleted: StateFlow<Boolean?> = _isOnboardingCompleted.asStateFlow()

    private val defaultUser = application.getString(R.string.default_username)
    private val _username = MutableStateFlow<String>(defaultUser)
    val username: StateFlow<String> = _username.asStateFlow()

    private val _habitsState = MutableStateFlow<UiState<List<Habit>>>(UiState.Loading)
    val habitsState: StateFlow<UiState<List<Habit>>> = _habitsState.asStateFlow()

    private val currentHabits: List<Habit>
        get() = (_habitsState.value as? UiState.Success)?.data ?: emptyList()

    private val _heatmapStats = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())
    val heatmapStats: StateFlow<Map<LocalDate, Int>> = _heatmapStats.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isEmailVerified = MutableStateFlow(true)
    val isEmailVerified: StateFlow<Boolean> = _isEmailVerified.asStateFlow()

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent: SharedFlow<String> = _uiEvent.asSharedFlow()

    private var habitsJob: Job? = null

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
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.getAuthStateFlow().collect { user ->
                if (user != null) {
                    _authState.value = AuthState.Authenticated(user)
                    _isEmailVerified.value = user.isEmailVerified
                    listenToHabits(user.uid)

                    if (_username.value == defaultUser && user.email != null) {
                        updateUsername(user.email!!.substringBefore("@"))
                    }
                } else {
                    _authState.value = AuthState.Unauthenticated
                    habitsJob?.cancel()
                    _habitsState.value = UiState.Success(emptyList())
                    _heatmapStats.value = emptyMap()
                }
            }
        }
    }

    private fun listenToHabits(userId: String) {
        habitsJob?.cancel()
        _habitsState.value = UiState.Loading

        habitsJob = viewModelScope.launch {
            habitRepository.getHabitsFlow(userId)
                .catch { exception ->
                    _habitsState.value = UiState.Error(
                        exception.localizedMessage ?: "Не вдалося завантажити звички"
                    )
                }
                .collect { habitsList ->
                    _habitsState.value = UiState.Success(habitsList)
                    updateHeatmap(habitsList)
                    HabitWidget().updateAll(getApplication<Application>())
                }
        }
    }

    private fun updateHeatmap(habitsList: List<Habit>) {
        val stats = mutableMapOf<LocalDate, Int>()
        habitsList.forEach { habit ->
            habit.completedDates.forEach { dateStr ->
                try {
                    val date = LocalDate.parse(dateStr)
                    stats[date] = stats.getOrDefault(date, 0) + 1
                } catch (e: Exception) { }
            }
        }
        _heatmapStats.value = stats
    }

    fun completeOnboarding() {
        viewModelScope.launch { userPreferencesRepository.completeOnboarding() }
    }

    fun updateUsername(newName: String) {
        viewModelScope.launch { userPreferencesRepository.updateUsername(newName) }
    }

    fun toggleHabitCompletion(habitId: Int, dateStr: String = LocalDate.now().toString()) {
        val userId = authRepository.currentUser?.uid ?: return
        val habit = currentHabits.find { it.id == habitId } ?: return

        val newDates = habit.completedDates.toMutableList()
        if (newDates.contains(dateStr)) newDates.remove(dateStr) else newDates.add(dateStr)

        viewModelScope.launch {
            habitRepository.updateHabitDates(userId, habitId, newDates)
        }
    }

    fun toggleHabitFavorite(habitId: Int) {
        val userId = authRepository.currentUser?.uid ?: return
        val habit = currentHabits.find { it.id == habitId } ?: return

        viewModelScope.launch {
            habitRepository.updateHabitFavorite(userId, habitId, !habit.isFavorite)
        }
    }

    fun deleteHabit(habitId: Int) {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            habitRepository.deleteHabit(userId, habitId)
            NotificationHelper.cancelHabitReminder(getApplication<Application>(), habitId)
        }
    }

    // --- AUTH МЕТОДИ ---

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
                .onFailure { e -> onError(e.localizedMessage ?: getApplication<Application>().getString(
                    R.string.error_reload_user)) }
        }
    }

    fun resendVerificationEmail(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            authRepository.resendVerificationEmail()
                .onSuccess { onSuccess() }
                .onFailure { e -> onError(e.localizedMessage ?: getApplication<Application>().getString(
                    R.string.error_resend_email)) }
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
                .onFailure { e -> onError(e.localizedMessage ?: getApplication<Application>().getString(
                    R.string.error_reset_password_send)) }
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
                .onFailure { e -> onError(e.localizedMessage ?: getApplication<Application>().getString(
                    R.string.error_feedback_send)) }
        }
    }
}