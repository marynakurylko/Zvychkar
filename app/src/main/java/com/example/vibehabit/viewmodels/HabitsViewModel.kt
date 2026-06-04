package com.example.vibehabit.viewmodels

import android.app.Application
import android.content.Context
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

// Створюємо інстанс DataStore на рівні файлу
val Context.dataStore by preferencesDataStore(name = "habits_prefs")

// Успадковуємося від AndroidViewModel для доступу до Context
class HabitsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.dataStore
    private val HABITS_KEY = stringPreferencesKey("habits_json")
    private val gson = Gson()

    // Для тестування додамо кілька дат виконання для першої звички (сьогодні і вчора)
    private val today = LocalDate.now().toString()
    private val yesterday = LocalDate.now().minusDays(1).toString()

    private val initialHabits = listOf(
        Habit(id = 1, name = "Випити склянку води зранку", isFavorite = true, colorHex = "#00BCD4", completedDates = setOf(today, yesterday)),
        Habit(id = 2, name = "Прочитати 10 сторінок книги", isFavorite = false, colorHex = "#8A2BE2", completedDates = emptySet()),
        Habit(id = 3, name = "Тренування (30 хв)", isFavorite = true, colorHex = "#FF9800", completedDates = emptySet())
    )

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    init {
        // При старті відразу завантажуємо дані з пам'яті телефону
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            val preferences = dataStore.data.first()
            val json = preferences[HABITS_KEY]

            if (json != null) {
                // Якщо дані є в пам'яті — парсимо їх з JSON
                val type = object : TypeToken<List<Habit>>() {}.type
                val savedHabits: List<Habit> = gson.fromJson(json, type)
                _habits.value = savedHabits
            } else {
                // Якщо це перший запуск застосунку — використовуємо твої тестові дані
                _habits.value = initialHabits
                // Одразу зберігаємо їх у базу
                saveHabits(initialHabits)
            }
        }
    }

    // Централізована функція для оновлення стану та запису в DataStore
    private fun saveHabits(newList: List<Habit>) {
        _habits.value = newList // Миттєво оновлюємо UI

        viewModelScope.launch {
            val json = gson.toJson(newList)
            dataStore.edit { preferences ->
                preferences[HABITS_KEY] = json // Фоново записуємо на диск
            }
        }
    }

    // НОВА логіка виконання: тепер ми відмічаємо звичку для конкретної дати
    fun toggleHabitCompletion(habitId: Int, dateStr: String = LocalDate.now().toString()) {
        val currentList = _habits.value
        val newList = currentList.map { habit ->
            if (habit.id == habitId) {
                val newDates = habit.completedDates.toMutableSet()
                if (newDates.contains(dateStr)) {
                    newDates.remove(dateStr) // Якщо вже виконано в цей день — знімаємо відмітку
                } else {
                    newDates.add(dateStr) // Якщо не виконано — додаємо дату
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

    fun addHabit(name: String, colorHex: String, iconName: String, targetDays: Int, frequency: String) {
        val currentList = _habits.value
        val newId = (currentList.maxOfOrNull { it.id } ?: 0) + 1
        val newHabit = Habit(
            id = newId,
            name = name,
            isFavorite = false,
            colorHex = colorHex,
            completedDates = emptySet(),
            iconName = iconName,
            targetDays = targetDays,
            frequency = frequency
        )
        saveHabits(currentList + newHabit)
    }

    fun deleteHabit(habitId: Int) {
        val currentList = _habits.value
        val newList = currentList.filter { it.id != habitId }
        saveHabits(newList)
    }

    fun updateHabit(id: Int, name: String, colorHex: String, iconName: String, targetDays: Int, frequency: String) {
        val currentList = _habits.value
        val newList = currentList.map { habit ->
            if (habit.id == id) {
                habit.copy(
                    name = name,
                    colorHex = colorHex,
                    iconName = iconName,
                    targetDays = targetDays,
                    frequency = frequency
                )
            } else {
                habit
            }
        }
        saveHabits(newList)
    }
}