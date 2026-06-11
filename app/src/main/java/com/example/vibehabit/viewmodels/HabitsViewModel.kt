package com.example.vibehabit.viewmodels

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibehabit.Habit
import com.example.vibehabit.R
import com.example.vibehabit.auth.AuthState
import com.example.vibehabit.repository.AuthRepository
import com.example.vibehabit.repository.HabitRepository
import com.example.vibehabit.widget.HabitWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

val Context.dataStore by preferencesDataStore(name = "habits_prefs")

@HiltViewModel
class HabitsViewModel @Inject constructor(
    application: Application,
    private val authRepository: AuthRepository,     // ІНЖЕКТИМО АВТОРИЗАЦІЮ
    private val habitRepository: HabitRepository    // ІНЖЕКТИМО ЗВИЧКИ
) : AndroidViewModel(application) {

    private val dataStore = application.dataStore
    private val ONBOARDING_KEY = booleanPreferencesKey("is_onboarding_completed")
    private val USERNAME_KEY = stringPreferencesKey("username")

    private val _isOnboardingCompleted = MutableStateFlow<Boolean?>(null)
    val isOnboardingCompleted: StateFlow<Boolean?> = _isOnboardingCompleted.asStateFlow()

    private val defaultUser = application.getString(R.string.default_username)
    private val _username = MutableStateFlow<String>(defaultUser)
    val username: StateFlow<String> = _username.asStateFlow()

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _heatmapStats = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())
    val heatmapStats: StateFlow<Map<LocalDate, Int>> = _heatmapStats.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isEmailVerified = MutableStateFlow(true)
    val isEmailVerified: StateFlow<Boolean> = _isEmailVerified.asStateFlow()

    private var habitsJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                _isOnboardingCompleted.value = preferences[ONBOARDING_KEY] ?: false
                _username.value = preferences[USERNAME_KEY] ?: defaultUser
            }
        }
        observeAuthState() // Підписуємось на авторизацію при старті
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
                    _habits.value = emptyList()
                    _heatmapStats.value = emptyMap()
                }
            }
        }
    }

    private fun listenToHabits(userId: String) {
        habitsJob?.cancel()
        habitsJob = viewModelScope.launch {
            habitRepository.getHabitsFlow(userId).collect { habitsList ->
                _habits.value = habitsList
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
        viewModelScope.launch { dataStore.edit { it[ONBOARDING_KEY] = true } }
    }

    fun updateUsername(newName: String) {
        viewModelScope.launch { dataStore.edit { it[USERNAME_KEY] = newName } }
    }

    fun toggleHabitCompletion(habitId: Int, dateStr: String = LocalDate.now().toString()) {
        val userId = authRepository.currentUser?.uid ?: return
        val habit = _habits.value.find { it.id == habitId } ?: return

        val newDates = habit.completedDates.toMutableList()
        if (newDates.contains(dateStr)) newDates.remove(dateStr) else newDates.add(dateStr)

        viewModelScope.launch {
            habitRepository.updateHabitDates(userId, habitId, newDates)
        }
    }

    fun toggleHabitFavorite(habitId: Int) {
        val userId = authRepository.currentUser?.uid ?: return
        val habit = _habits.value.find { it.id == habitId } ?: return

        viewModelScope.launch {
            habitRepository.updateHabitFavorite(userId, habitId, !habit.isFavorite)
        }
    }

    fun addHabit(name: String, colorHex: String, iconName: String, targetDays: Int, frequency: String, reminderTime: String?) {
        val userId = authRepository.currentUser?.uid ?: return
        val newId = (_habits.value.maxOfOrNull { it.id } ?: 0) + 1
        val newHabit = Habit(
            id = newId, name = name, isFavorite = false, colorHex = colorHex,
            completedDates = emptyList(), iconName = iconName, targetDays = targetDays,
            frequency = frequency, reminderTime = reminderTime
        )

        viewModelScope.launch {
            habitRepository.saveHabit(userId, newHabit)
            handleReminder(newId, name, reminderTime)
        }
    }

    fun updateHabit(id: Int, name: String, colorHex: String, iconName: String, targetDays: Int, frequency: String, reminderTime: String?) {
        val userId = authRepository.currentUser?.uid ?: return
        val currentHabit = _habits.value.find { it.id == id }
        val updatedHabit = Habit(
            id = id, name = name, isFavorite = currentHabit?.isFavorite ?: false, colorHex = colorHex,
            completedDates = currentHabit?.completedDates ?: emptyList(),
            iconName = iconName, targetDays = targetDays, frequency = frequency, reminderTime = reminderTime
        )

        viewModelScope.launch {
            habitRepository.saveHabit(userId, updatedHabit)
            handleReminder(id, name, reminderTime)
        }
    }

    fun deleteHabit(habitId: Int) {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            habitRepository.deleteHabit(userId, habitId)
            com.example.vibehabit.notifications.NotificationHelper.cancelHabitReminder(getApplication<Application>(), habitId)
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
                if (e is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException ||
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
                .onFailure { e -> onError(e.localizedMessage ?: getApplication<Application>().getString(R.string.error_feedback_send)) }
        }
    }

    private fun handleReminder(habitId: Int, habitName: String, reminderTime: String?) {
        val context = getApplication<Application>()
        if (reminderTime != null) {
            val parts = reminderTime.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toIntOrNull() ?: 9
                val minute = parts[1].toIntOrNull() ?: 0
                com.example.vibehabit.notifications.NotificationHelper.scheduleHabitReminder(context, habitId, habitName, hour, minute)
            }
        } else {
            com.example.vibehabit.notifications.NotificationHelper.cancelHabitReminder(context, habitId)
        }
    }
}