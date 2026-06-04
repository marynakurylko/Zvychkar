package com.example.vibehabit

import java.time.LocalDate

data class Habit(
    val id: Int,
    val name: String,
    val isFavorite: Boolean = false,
    val colorHex: String,
    val completedDates: Set<String> = emptySet(),
    val targetDays: Int = 7,
    val iconName: String = "Bolt",
    val frequency: String = "Daily",
    val reminderTime: String? = null
) {
    // Обчислювана властивість: Поточна серія
    val currentStreak: Int
        get() {
            if (completedDates.isEmpty()) return 0

            // Перетворюємо рядки в дати і сортуємо від найновішої до найстарішої
            val dates = completedDates.map { LocalDate.parse(it) }.sortedDescending()
            var streak = 0
            var currentDate = LocalDate.now()

            // Перевіряємо, чи є взагалі активний стрік (чи виконано сьогодні або хоча б вчора)
            if (dates.contains(currentDate)) {
                streak = 1
            } else if (dates.contains(currentDate.minusDays(1))) {
                streak = 1
                currentDate = currentDate.minusDays(1)
            } else {
                return 0 // Якщо вчора і сьогодні пусто - серія перервана
            }

            // Рахуємо попередні безперервні дні
            for (i in 1 until dates.size) {
                currentDate = currentDate.minusDays(1)
                if (dates.contains(currentDate)) {
                    streak++
                } else {
                    break // Як тільки знаходимо пропуск - зупиняємось
                }
            }
            return streak
        }

    // Обчислювана властивість: Найкращий результат за весь час
    val bestStreak: Int
        get() {
            if (completedDates.isEmpty()) return 0

            // Сортуємо від найстарішої до найновішої
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
                    tempStreak = 1 // Скидаємо лічильник при розриві
                }
            }
            return maxStreak
        }
}