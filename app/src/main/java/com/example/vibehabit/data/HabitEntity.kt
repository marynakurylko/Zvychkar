package com.example.vibehabit.data

/*
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
*/
data class HabitEntity(
    // @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val isCompleted: Boolean,
    val isFavorite: Boolean,
    val colorHex: String
)
