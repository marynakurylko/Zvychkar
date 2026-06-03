package com.example.vibehabit.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.vibehabit.Habit
import com.example.vibehabit.ui.theme.HabitTrackerTheme

@Composable
fun HabitCard(
    habit: Habit,
    onCheckedChange: (Boolean) -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Uses the KTX extension to safely parse the hex color
    val cardColor = runCatching { Color(habit.colorHex.toColorInt()) }
        .getOrDefault(MaterialTheme.colorScheme.surface)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    // Icons.Outlined.StarBorder requires the extended icons library
                    imageVector = if (habit.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (habit.isFavorite) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = habit.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Checkbox(
                checked = habit.isCompleted,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HabitCardPreview() {
    HabitTrackerTheme {
        HabitCard(
            habit = Habit(
                id = 1,
                name = "Ранкова йога та розтяжка",
                isCompleted = false,
                isFavorite = true,
                colorHex = "#8A2BE2"
            ),
            onCheckedChange = {},
            onFavoriteClick = {}
        )
    }
}