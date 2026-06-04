package com.example.vibehabit.viewmodels

import androidx.lifecycle.ViewModel
import com.example.vibehabit.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class HabitsViewModel : ViewModel() {

    // Для тестування додамо кілька дат виконання для першої звички (сьогодні і вчора)
    private val today = LocalDate.now().toString()
    private val yesterday = LocalDate.now().minusDays(1).toString()

    private val initialHabits = listOf(
        Habit(id = 1, name = "Випити склянку води зранку", isFavorite = true, colorHex = "#00BCD4", completedDates = setOf(today, yesterday)),
        Habit(id = 2, name = "Прочитати 10 сторінок книги", isFavorite = false, colorHex = "#8A2BE2", completedDates = emptySet()),
        Habit(id = 3, name = "Тренування (30 хв)", isFavorite = true, colorHex = "#FF9800", completedDates = emptySet())
    )

    private val _habits = MutableStateFlow(initialHabits)
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    // НОВА логіка виконання: тепер ми відмічаємо звичку для конкретної дати
    fun toggleHabitCompletion(habitId: Int, dateStr: String = LocalDate.now().toString()) {
        _habits.update { currentList ->
            currentList.map { habit ->
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
        }
    }

    fun toggleHabitFavorite(habitId: Int) {
        _habits.update { currentList ->
            currentList.map { habit ->
                if (habit.id == habitId) habit.copy(isFavorite = !habit.isFavorite) else habit
            }
        }
    }

    fun addHabit(name: String, colorHex: String, iconName: String, targetDays: Int, frequency: String) {
        _habits.update { currentList ->
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
            currentList + newHabit
        }
    }

    fun deleteHabit(habitId: Int) {
        _habits.update { currentList ->
            currentList.filter { it.id != habitId }
        }
    }

    fun updateHabit(id: Int, name: String, colorHex: String, iconName: String, targetDays: Int, frequency: String) {
        _habits.update { currentList ->
            currentList.map { habit ->
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
        }
    }
}