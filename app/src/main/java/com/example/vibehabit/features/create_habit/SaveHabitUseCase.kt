package com.example.vibehabit.features.create_habit

import android.content.Context
import com.example.vibehabit.R
import com.example.vibehabit.core.models.Habit
import com.example.vibehabit.shared_viewmodels.HabitRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SaveHabitUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(
        userId: String,
        habitId: String,
        name: String,
        colorHex: String,
        iconName: String,
        targetDays: Int,
        frequency: String,
        reminderTime: String?,
        isFavorite: Boolean = false,
        completedDates: List<String> = emptyList()
    ): Result<Unit> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(Exception(context.getString(R.string.error_empty_habit_name)))
        }
        if (targetDays <= 0) {
            return Result.failure(Exception(context.getString(R.string.error_invalid_target_days)))
        }

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

        return habitRepository.saveHabit(userId, habitToSave)
    }
}