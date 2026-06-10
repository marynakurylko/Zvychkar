package com.example.vibehabit.viewmodels

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibehabit.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.glance.appwidget.updateAll
import com.example.vibehabit.widget.HabitWidget
import com.example.vibehabit.auth.AuthState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutionException
import kotlinx.coroutines.tasks.await
import android.util.Log
import kotlinx.coroutines.NonCancellable
import com.google.firebase.auth.GoogleAuthProvider

val Context.dataStore by preferencesDataStore(name = "habits_prefs")

class HabitsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.dataStore
    private val THEME_KEY = stringPreferencesKey("app_theme_mode")
    private val ONBOARDING_KEY = booleanPreferencesKey("is_onboarding_completed")
    private val USERNAME_KEY = stringPreferencesKey("username")

    private val _isOnboardingCompleted = MutableStateFlow<Boolean?>(null)
    val isOnboardingCompleted: StateFlow<Boolean?> = _isOnboardingCompleted.asStateFlow()

    private val _username = MutableStateFlow<String>("Користувач")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _heatmapStats = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())
    val heatmapStats: StateFlow<Map<LocalDate, Int>> = _heatmapStats.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var habitsListener: ListenerRegistration? = null

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                _isOnboardingCompleted.value = preferences[ONBOARDING_KEY] ?: false
                _username.value = preferences[USERNAME_KEY] ?: "Користувач"
            }
        }
        checkAuthState()
    }

    private fun checkAuthState() {
        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                _authState.value = AuthState.Authenticated(currentUser)
                listenToHabits(currentUser.uid)

                // Якщо ім'я дефолтне, створюємо нікнейм з пошти
                if (_username.value == "Користувач" && currentUser.email != null) {
                    val emailPrefix = currentUser.email!!.substringBefore("@")
                    updateUsername(emailPrefix)
                }
            } else {
                _authState.value = AuthState.Unauthenticated
                habitsListener?.remove()
                _habits.value = emptyList()
                _heatmapStats.value = emptyMap()
            }
        }
    }

    private fun listenToHabits(userId: String) {
        habitsListener?.remove()
        habitsListener = firestore.collection("users")
            .document(userId)
            .collection("habits")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val habitsList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Habit::class.java)
                }.sortedBy { it.id }

                _habits.value = habitsList
                updateHeatmap(habitsList)

                viewModelScope.launch {
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
        val userId = auth.currentUser?.uid ?: return
        val habit = _habits.value.find { it.id == habitId } ?: return

        val newDates = habit.completedDates.toMutableList()
        if (newDates.contains(dateStr)) newDates.remove(dateStr) else newDates.add(dateStr)

        firestore.collection("users").document(userId)
            .collection("habits").document(habitId.toString())
            .update("completedDates", newDates)
    }

    fun toggleHabitFavorite(habitId: Int) {
        val userId = auth.currentUser?.uid ?: return
        val habit = _habits.value.find { it.id == habitId } ?: return

        firestore.collection("users").document(userId)
            .collection("habits").document(habitId.toString())
            .update("isFavorite", !habit.isFavorite)
    }

    fun addHabit(name: String, colorHex: String, iconName: String, targetDays: Int, frequency: String, reminderTime: String?) {
        val userId = auth.currentUser?.uid ?: return
        val newId = (_habits.value.maxOfOrNull { it.id } ?: 0) + 1
        val newHabit = Habit(
            id = newId, name = name, isFavorite = false, colorHex = colorHex,
            completedDates = emptyList(), iconName = iconName, targetDays = targetDays,
            frequency = frequency, reminderTime = reminderTime
        )

        firestore.collection("users").document(userId)
            .collection("habits").document(newId.toString())
            .set(newHabit)

        handleReminder(newId, name, reminderTime)
    }

    fun updateHabit(id: Int, name: String, colorHex: String, iconName: String, targetDays: Int, frequency: String, reminderTime: String?) {
        val userId = auth.currentUser?.uid ?: return
        val currentHabit = _habits.value.find { it.id == id }
        val updatedHabit = Habit(
            id = id, name = name, isFavorite = currentHabit?.isFavorite ?: false, colorHex = colorHex,
            completedDates = currentHabit?.completedDates ?: emptyList(),
            iconName = iconName, targetDays = targetDays, frequency = frequency, reminderTime = reminderTime
        )

        firestore.collection("users").document(userId)
            .collection("habits").document(id.toString())
            .set(updatedHabit)

        handleReminder(id, name, reminderTime)
    }

    fun deleteHabit(habitId: Int) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("habits").document(habitId.toString())
            .delete()

        com.example.vibehabit.notifications.NotificationHelper.cancelHabitReminder(getApplication<Application>(), habitId)
    }

    fun signInWithGoogle(idToken: String, onError: (String) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnFailureListener { exception ->
                onError(exception.localizedMessage ?: "Помилка входу через Google")
            }
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onError("Користувач не знайдений у системі.")
            return
        }
        val userId = currentUser.uid

        viewModelScope.launch {
            try {
                Log.d("AuthDebug", "Початок видалення. UID: $userId")

                // ЗАХИЩЕНИЙ БЛОК: Не буде скасований, навіть якщо екран закриється
                withContext(NonCancellable) {
                    val habitsRef = firestore.collection("users").document(userId).collection("habits")
                    val snapshot = habitsRef.get().await()
                    Log.d("AuthDebug", "Знайдено звичок для видалення: ${snapshot.documents.size}")

                    val batch = firestore.batch()
                    for (doc in snapshot.documents) {
                        batch.delete(doc.reference)
                    }
                    batch.delete(firestore.collection("users").document(userId))

                    batch.commit().await()
                    Log.d("AuthDebug", "Дані Firestore успішно видалено")

                    currentUser.delete().await()
                    Log.d("AuthDebug", "Акаунт успішно видалено з Firebase Auth")
                }

                onSuccess()

            } catch (e: Exception) {
                Log.e("AuthDebug", "Помилка під час видалення", e)
                val errorMessage = e.message ?: ""

                if (e is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException ||
                    errorMessage.contains("CREDENTIAL_TOO_OLD_LOGIN_AGAIN") ||
                    errorMessage.contains("recent login")) {

                    onError("🔒 Для безпеки ця дія вимагає свіжого входу. Будь ласка, вийдіть з акаунта, увійдіть знову та повторіть видалення.")
                } else {
                    onError("Помилка: ${e.localizedMessage}")
                }
            }
        }
    }
    fun signUpWithEmail(email: String, pass: String, onError: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnFailureListener { exception -> onError(exception.localizedMessage ?: "Помилка реєстрації") }
    }

    fun signInWithEmail(email: String, pass: String, onError: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnFailureListener { exception -> onError(exception.localizedMessage ?: "Помилка входу") }
    }

    fun signOut() {
        auth.signOut()
    }

    private fun handleReminder(habitId: Int, habitName: String, reminderTime: String?) {
        val context = getApplication<Application>()
        if (reminderTime != null) {
            val parts = reminderTime.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toIntOrNull() ?: 9
                val minute = parts[1].toIntOrNull() ?: 0
                com.example.vibehabit.notifications.NotificationHelper.scheduleHabitReminder(
                    context, habitId, habitName, hour, minute
                )
            }
        } else {
            com.example.vibehabit.notifications.NotificationHelper.cancelHabitReminder(context, habitId)
        }
    }

    fun sendFeedback(message: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onError("Користувач не авторизований")
            return
        }

        // Формуємо об'єкт із даними відгуку
        val feedbackData = hashMapOf(
            "userId" to currentUser.uid,
            "email" to (currentUser.email ?: "Без email"),
            "message" to message,
            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp() // Автоматичний точний час сервера
        )

        // Зберігаємо в нову колекцію "feedback"
        firestore.collection("feedback")
            .add(feedbackData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Не вдалося відправити відгук. Спробуйте пізніше.")
            }
    }
}