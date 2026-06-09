package com.example.vibehabit

import java.time.LocalDate

data class Habit(
    val id: Int = 0,
    val name: String = "",
    val isFavorite: Boolean = false,
    val colorHex: String = "",
    val completedDates: List<String> = emptyList(),
    val targetDays: Int = 7,
    val iconName: String = "Bolt",
    val frequency: String = "Daily",
    val reminderTime: String? = null
) {
    val currentStreak: Int
        get() {
            if (completedDates.isEmpty()) return 0

            val dates = completedDates.map { LocalDate.parse(it) }.sortedDescending()
            var streak = 0
            var currentDate = LocalDate.now()

            if (dates.contains(currentDate)) {
                streak = 1
            } else if (dates.contains(currentDate.minusDays(1))) {
                streak = 1
                currentDate = currentDate.minusDays(1)
            } else {
                return 0
            }

            for (i in 1 until dates.size) {
                currentDate = currentDate.minusDays(1)
                if (dates.contains(currentDate)) {
                    streak++
                } else {
                    break
                }
            }
            return streak
        }

    val bestStreak: Int
        get() {
            if (completedDates.isEmpty()) return 0

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
            return maxStreak
        }
}