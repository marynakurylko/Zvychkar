package com.example.vibehabit

data class Habit(
    val id: Int,
    val name: String,
    val isCompleted: Boolean,
    val isFavorite: Boolean,
    val colorHex: String
)