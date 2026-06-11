package com.example.vibehabit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ColorPicker(
    selectedColorHex: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Наша палітра (включаючи твій фіолетовий)
    val colors = listOf(
        "#8A2BE2", // Фіолетовий (Primary)
        "#00BCD4", // Блакитний
        "#FF9800", // Помаранчевий
        "#E91E63", // Рожевий
        "#4CAF50", // Зелений
        "#F44336"  // Червоний
    )

    // LazyRow — це як LazyColumn, тільки горизонтальний скрол
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(colors) { hexString ->
            val color = Color(android.graphics.Color.parseColor(hexString))
            val isSelected = hexString == selectedColorHex

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color)
                    // Якщо колір обрано, малюємо навколо нього контрастну рамку
                    .border(
                        width = if (isSelected) 3.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(hexString) }
            )
        }
    }
}