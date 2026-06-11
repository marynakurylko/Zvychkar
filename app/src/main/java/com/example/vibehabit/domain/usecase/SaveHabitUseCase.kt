package com.example.vibehabit.domain.usecase

import com.example.vibehabit.Habit
import com.example.vibehabit.repository.HabitRepository
import javax.inject.Inject

class SaveHabitUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(
        userId: String,
        habitId: Int,
        name: String,
        colorHex: String,
        iconName: String,
        targetDays: Int,
        frequency: String,
        reminderTime: String?,
        isFavorite: Boolean = false,
        completedDates: List<String> = emptyList()
    ): Result<Unit> {
        // 1. ВАЛІДАЦІЯ (Бізнес-правила)
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(Exception("Назва звички не може бути порожньою"))
        }
        if (targetDays <= 0) {
            return Result.failure(Exception("Цільова кількість днів має бути більшою за 0"))
        }

        // Тут ти можеш додати будь-яку складну логіку форматування "frequency",
        // щоб на екрані CreateHabitScreen не було "брудного" коду.

        // 2. СТВОРЕННЯ МОДЕЛІ
        val habitToSave = Habit(
            id = habitId,
            name = trimmedName,
            isFavorite = isFavorite,
            colorHex = colorHex,
            completedDates = completedDates,
            iconName = iconName,
            targetDays = targetDays,
            frequency = frequency,
            reminderTime = reminderTime
        )

        // 3. ПЕРЕДАЧА В РЕПОЗИТОРІЙ
        return habitRepository.saveHabit(userId, habitToSave)
    }
}