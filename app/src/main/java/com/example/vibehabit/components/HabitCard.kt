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
import com.example.vibehabit.Habit
import com.example.vibehabit.ui.theme.HabitTrackerTheme
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.foundation.clickable
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCard(    habit: Habit,
                  onCheckedChange: (Boolean) -> Unit,
                  onFavoriteClick: () -> Unit,
                  onDeleteClick: () -> Unit,
                  onCardClick: () -> Unit,
                  modifier: Modifier = Modifier
) {
    // Визначаємо, чи виконана звичка сьогодні
    val today = LocalDate.now().toString()
    val isCompletedToday = habit.completedDates.contains(today)

    // 1. Анімація прозорості ТІЛЬКИ для внутрішнього контенту
    val contentAlpha by animateFloatAsState(
        targetValue = if (isCompletedToday) 0.5f else 1f,
        label = "contentAlpha"
    )

    val textDecoration = if (isCompletedToday) TextDecoration.LineThrough else TextDecoration.None

    val baseColor = runCatching { Color(android.graphics.Color.parseColor(habit.colorHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    // 2. Колір фону картки. Робимо його світлішим для виконаних, але він залишається НЕПРОЗОРИМ (завдяки compositeOver)
    val containerColor = if (isCompletedToday) {
        baseColor.copy(alpha = 0.08f).compositeOver(MaterialTheme.colorScheme.surface)
    } else {
        baseColor.copy(alpha = 0.18f).compositeOver(MaterialTheme.colorScheme.surface)
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDeleteClick()
                true
            } else false
        }
    )

    // 3. Оптимізована перевірка свайпу: червоний фон з'являється ТІЛЬКИ при русі вліво
    val isSwipingToDismiss = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart && dismissState.progress > 0.05f

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 6.dp)
                    .clickable { onCardClick() }
                    .background(
                        // Фон стає червоним тільки якщо тягнемо вліво
                        color = if (isSwipingToDismiss) Color.Red.copy(alpha = 0.8f) else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (isSwipingToDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Color.White
                    )
                }
            }
        },
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clickable { onCardClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isCompletedToday) 0.dp else 2.dp)
        ) {
            // 4. Застосовуємо alpha ТІЛЬКИ до Row (контенту)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp)
                    .alpha(contentAlpha), // Тепер прозорим стає текст і кнопки, а не фон картки
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (habit.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (habit.isFavorite) baseColor else Color.Gray
                    )
                }

                Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                    Text(
                        text = habit.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textDecoration = textDecoration
                    )
                }
                
                val progress = (habit.completedDates.size.toFloat() / habit.targetDays).coerceAtMost(1f)
                Box(contentAlignment = Alignment.Center) {
                    // Кільце прогресу (на тлі)
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(48.dp), // Трохи більше за чекбокс
                        color = baseColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 3.dp,
                    )

                    // Сам чекбокс (поверх кільця)
                    Checkbox(
                        checked = isCompletedToday,
                        onCheckedChange = onCheckedChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = baseColor, // Робимо галочку під колір звички!
                            checkmarkColor = Color.White
                        )
                    )
                }
            }
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
            onFavoriteClick = {},
            onDeleteClick = {},
            onCardClick = {}
        )
    }
}
