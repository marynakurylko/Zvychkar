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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.glance.appwidget.updateAll
import com.example.vibehabit.widget.HabitWidget

val Context.dataStore by preferencesDataStore(name = "habits_prefs")

class HabitsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.dataStore
    private val HABITS_KEY = stringPreferencesKey("habits_json")
    private val gson = Gson()

    private val THEME_KEY = stringPreferencesKey("app_theme_mode")
    private val ONBOARDING_KEY = booleanPreferencesKey("is_onboarding_completed")

    private val _isOnboardingCompleted = MutableStateFlow<Boolean?>(null)
    val isOnboardingCompleted: StateFlow<Boolean?> = _isOnboardingCompleted.asStateFlow()

    private val today = LocalDate.now().toString()
    private val yesterday = LocalDate.now().minusDays(1).toString()

    private val initialHabits = listOf(
        Habit(id = 1, name = "Випити склянку води зранку", isFavorite = true, colorHex = "#00BCD4", completedDates = setOf(today, yesterday)),
        Habit(id = 2, name = "Прочитати 10 сторінок книги", isFavorite = false, colorHex = "#8A2BE2", completedDates = emptySet()),
        Habit(id = 3, name = "Тренування (30 хв)", isFavorite = true, colorHex = "#FF9800", completedDates = emptySet())
    )

    // Використовуємо надійний MutableStateFlow для гарантованої реактивності
    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    // Окремий стейт для Heatmap
    private val _heatmapStats = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())
    val heatmapStats: StateFlow<Map<LocalDate, Int>> = _heatmapStats.asStateFlow()

    init {
        // Слухаємо тему та онбординг
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                _isOnboardingCompleted.value = preferences[ONBOARDING_KEY] ?: false
            }
        }

        // ГОЛОВНИЙ СЛУХАЧ: Завжди читаємо базу. Якщо віджет змінить її - миттєво дізнаємося!
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                val json = preferences[HABITS_KEY]
                if (json != null) {
                    val type = object : TypeToken<List<Habit>>() {}.type
                    val parsed: List<Habit> = gson.fromJson(json, type) ?: emptyList()
                    _habits.value = parsed
                    updateHeatmap(parsed) // Оновлюємо статистику
                } else {
                    // Якщо база порожня (перший запуск)
                    _habits.value = initialHabits
                    saveHabits(initialHabits)
                }
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
                } catch (e: Exception) {
                    // Ігноруємо некоректні дати
                }
            }
        }
        _heatmapStats.value = stats
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[ONBOARDING_KEY] = true
            }
        }
    }

    // Централізована функція: пише в базу і відправляє команду віджетам
    private fun saveHabits(newList: List<Habit>) {
        // Оптимістичне оновлення (миттєва реакція UI)
        _habits.value = newList
        updateHeatmap(newList)

        viewModelScope.launch {
            val json = gson.toJson(newList)
            dataStore.edit { preferences ->
                preferences[HABITS_KEY] = json
            }
            // Штовхаємо віджет, щоб він перемалювався
            HabitWidget().updateAll(getApplication<Application>())
        }
    }

    fun toggleHabitCompletion(habitId: Int, dateStr: String = LocalDate.now().toString()) {
        val currentList = _habits.value
        val newList = currentList.map { habit ->
            if (habit.id == habitId) {
                val newDates = habit.completedDates.toMutableSet()
                if (newDates.contains(dateStr)) {
                    newDates.remove(dateStr)
                } else {
                    newDates.add(dateStr)
                }
                habit.copy(completedDates = newDates)
            } else {
                habit
            }
        }
        saveHabits(newList)
    }

    fun toggleHabitFavorite(habitId: Int) {
        val currentList = _habits.value
        val newList = currentList.map { habit ->
            if (habit.id == habitId) habit.copy(isFavorite = !habit.isFavorite) else habit
        }
        saveHabits(newList)
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

    fun addHabit(name: String, colorHex: String, iconName: String, targetDays: Int, frequency: String, reminderTime: String?) {
        val currentList = _habits.value
        val newId = (currentList.maxOfOrNull { it.id } ?: 0) + 1
        val newHabit = Habit(
            id = newId, name = name, isFavorite = false, colorHex = colorHex,
            completedDates = emptySet(), iconName = iconName, targetDays = targetDays,
            frequency = frequency, reminderTime = reminderTime
        )
        saveHabits(currentList + newHabit)
        handleReminder(newId, name, reminderTime)
    }

    fun updateHabit(id: Int, name: String, colorHex: String, iconName: String, targetDays: Int, frequency: String, reminderTime: String?) {
        val currentList = _habits.value
        val newList = currentList.map { habit ->
            if (habit.id == id) {
                habit.copy(
                    name = name, colorHex = colorHex, iconName = iconName,
                    targetDays = targetDays, frequency = frequency, reminderTime = reminderTime
                )
            } else habit
        }
        saveHabits(newList)
        handleReminder(id, name, reminderTime)
    }

    fun deleteHabit(habitId: Int) {
        val currentList = _habits.value
        val newList = currentList.filter { it.id != habitId }
        saveHabits(newList)
        com.example.vibehabit.notifications.NotificationHelper.cancelHabitReminder(getApplication<Application>(), habitId)
    }
}