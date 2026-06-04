package com.example.vibehabit

data class Habit(
    val id: Int,
    val name: String,
    val isFavorite: Boolean,
    val colorHex: String,
    val completedDates: Set<String> = emptySet(),
    val isCompleted: Boolean = false,
    val targetDays: Int = 7
)
