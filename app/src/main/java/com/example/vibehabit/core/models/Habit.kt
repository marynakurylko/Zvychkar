package com.example.vibehabit.core.models

import androidx.compose.runtime.Immutable
import com.google.firebase.firestore.Exclude
import java.time.LocalDate

@Immutable
data class Habit(
    val id: String = "",
    val name: String = "",
    val isFavorite: Boolean = false,
    val colorHex: String = "",
    val completedDates: List<String> = emptyList(),
    val targetDays: Int = 7,
    val iconName: String = "Bolt",
    val frequency: String = "Daily",
    val reminderTime: String? = null
) {
    @get:Exclude
    val currentStreak: Int by lazy {
        if (completedDates.isEmpty()) {
            0
        } else {
            val dates = completedDates.map { LocalDate.parse(it) }.sortedDescending()
            var streak = 0
            var currentDate = LocalDate.now()

            if (dates.contains(currentDate)) {
                streak = 1
            } else if (dates.contains(currentDate.minusDays(1))) {
                streak = 1
                currentDate = currentDate.minusDays(1)
            }

            if (streak == 0) {
                0
            } else {
                for (i in 1 until dates.size) {
                    currentDate = currentDate.minusDays(1)
                    if (dates.contains(currentDate)) {
                        streak++
                    } else {
                        break
                    }
                }
                streak
            }
        }
    }

    @get:Exclude
    val bestStreak: Int by lazy {
        if (completedDates.isEmpty()) {
            0
        } else {
            val dates = completedDates.map { LocalDate.parse(it) }.sorted()
            var maxStreak = 1
            var tempStreak = 1

            for (i in 1 until dates.size) {
                val expectedNextDay = dates[i - 1].plusDays(1)
                if (dates[i] == expectedNextDay) {
                    tempStreak++
                    if (tempStreak > maxStreak) {
                        maxStreak = tempStreak
                    }
                } else {
                    tempStreak = 1
                }
            }
            maxStreak
        }
    }
}