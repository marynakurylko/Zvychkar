package com.example.vibehabit.viewmodels

import androidx.lifecycle.ViewModel
import com.example.vibehabit.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HabitsViewModel : ViewModel() {

    // Створюємо початковий список (наші mock-дані переїхали сюди)
    private val initialHabits = listOf(
        Habit(id = 1, name = "Випити склянку води зранку", isCompleted = true, isFavorite = true, colorHex = "#00BCD4"),
        Habit(id = 2, name = "Прочитати 10 сторінок книги", isCompleted = false, isFavorite = false, colorHex = "#8A2BE2"),
        Habit(id = 3, name = "Тренування (30 хв)", isCompleted = false, isFavorite = true, colorHex = "#FF9800")
    )

    // MutableStateFlow — це реактивний контейнер для нашого стану
    private val _habits = MutableStateFlow(initialHabits)

    // Публічний незмінний стан, який буде читати наш UI
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    // Функція для зміни статусу виконання
    fun toggleHabitCompletion(habitId: Int) {
        _habits.update { currentList ->
            currentList.map { habit ->
                if (habit.id == habitId) {
                    habit.copy(isCompleted = !habit.isCompleted) // Створюємо копію зі зміненим статусом
                } else {
                    habit
                }
            }
        }
    }

    // Функція для зміни статусу "Улюблене"
    fun toggleHabitFavorite(habitId: Int) {
        _habits.update { currentList ->
            currentList.map { habit ->
                if (habit.id == habitId) {
                    habit.copy(isFavorite = !habit.isFavorite)
                } else {
                    habit
                }
            }
        }
    }

    fun addHabit(name: String, colorHex: String) {
        _habits.update { currentList ->
            // Знаходимо максимальний існуючий ID, щоб зробити новий (якщо список пустий, беремо 0)
            val newId = (currentList.maxOfOrNull { it.id } ?: 0) + 1

            val newHabit = Habit(
                id = newId,
                name = name,
                isCompleted = false,
                isFavorite = false, // За замовчуванням не в улюблених
                colorHex = colorHex
            )

            // Повертаємо новий список, де до поточного додано нову звичку
            currentList + newHabit
        }
    }
}