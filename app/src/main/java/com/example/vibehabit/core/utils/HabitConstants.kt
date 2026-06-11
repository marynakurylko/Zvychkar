package com.example.vibehabit.core.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.graphics.vector.ImageVector

object HabitConstants {
    // Єдине джерело правди для палітри
    val NEON_COLORS = listOf(
        "#8A2BE2", // Фіолетовий (Primary)
        "#00BCD4", // Блакитний
        "#FF9800", // Помаранчевий
        "#E91E63", // Рожевий
        "#4CAF50", // Зелений
        "#F44336"  // Червоний
    )

    // Типізований Enum для іконок замість звичайних строк
    enum class HabitIcon(val iconName: String, val imageVector: ImageVector) {
        BOLT("Bolt", Icons.Filled.Bolt),
        FAVORITE("Favorite", Icons.Filled.Favorite),
        BIKE("Bike", Icons.Filled.DirectionsBike),
        BOOK("Book", Icons.Filled.Book);

        companion object {
            // Зручна функція для пошуку іконки за іменем з БД
            fun getByName(name: String): ImageVector {
                return entries.find { it.iconName == name }?.imageVector ?: BOLT.imageVector
            }
        }
    }
}

