package com.example.vibehabit.core.models

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class HabitTest {

    @Test
    fun `currentStreak is 3 when completed for 3 consecutive days including today`() {
        // Arrange
        val today = LocalDate.now()
        val completedDates = listOf(
            today.toString(),
            today.minusDays(1).toString(),
            today.minusDays(2).toString()
        )
        val habit = Habit(id = "1", completedDates = completedDates)

        // Act
        val streak = habit.currentStreak

        // Assert
        assertEquals(3, streak)
    }

    @Test
    fun `currentStreak is active if completed yesterday but not yet today`() {
        val today = LocalDate.now()
        val completedDates = listOf(
            today.minusDays(1).toString(),
            today.minusDays(2).toString()
        )
        val habit = Habit(id = "1", completedDates = completedDates)

        assertEquals(2, habit.currentStreak)
    }

    @Test
    fun `currentStreak is 0 if a day was missed`() {
        val today = LocalDate.now()
        val completedDates = listOf(
            today.minusDays(2).toString(),
            today.minusDays(3).toString()
        )
        val habit = Habit(id = "1", completedDates = completedDates)

        assertEquals(0, habit.currentStreak)
    }

    @Test
    fun `bestStreak calculates the absolute maximum consecutive days`() {
        val today = LocalDate.now()
        val completedDates = listOf(
            today.minusDays(10).toString(),
            today.minusDays(9).toString(),
            today.minusDays(8).toString(),

            today.minusDays(2).toString(),
            today.minusDays(1).toString()
        )
        val habit = Habit(id = "1", completedDates = completedDates)

        assertEquals(3, habit.bestStreak)
    }
}