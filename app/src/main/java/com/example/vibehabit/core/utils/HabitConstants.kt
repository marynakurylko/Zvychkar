package com.example.vibehabit.core.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.graphics.vector.ImageVector

object HabitConstants {
    val NEON_COLORS = listOf(
        "#8A2BE2",
        "#00BCD4",
        "#FF9800",
        "#E91E63",
        "#4CAF50",
        "#F44336"
    )

    enum class HabitIcon(val iconName: String, val imageVector: ImageVector) {
        BOLT("Bolt", Icons.Filled.Bolt),
        FAVORITE("Favorite", Icons.Filled.Favorite),
        BIKE("Bike", Icons.Filled.DirectionsBike),
        BOOK("Book", Icons.Filled.Book);

        companion object {
            fun getByName(name: String): ImageVector {
                return entries.find { it.iconName == name }?.imageVector ?: BOLT.imageVector
            }
        }
    }
}

