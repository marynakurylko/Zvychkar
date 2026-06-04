package com.example.vibehabit.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibehabit.Habit
import com.example.vibehabit.R
import com.example.vibehabit.ui.theme.HabitTrackerTheme
import java.time.LocalDate

fun getIconByName(name: String): ImageVector {
    return when (name) {
        "Favorite" -> Icons.Filled.Favorite
        "Bike" -> Icons.Filled.DirectionsBike
        "Book" -> Icons.Filled.Book
        else -> Icons.Filled.Bolt
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCard(
    habit: Habit,
    onCheckedChange: (Boolean) -> Unit,
    onFavoriteClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now().toString()
    val isCompletedToday = habit.completedDates.contains(today)

    val contentAlpha by animateFloatAsState(
        targetValue = if (isCompletedToday) 0.5f else 1f,
        label = "contentAlpha"
    )

    val textDecoration = if (isCompletedToday) TextDecoration.LineThrough else TextDecoration.None

    val cardColor = runCatching { Color(android.graphics.Color.parseColor(habit.colorHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    val containerColor = if (isCompletedToday) {
        cardColor.copy(alpha = 0.08f).compositeOver(MaterialTheme.colorScheme.surface)
    } else {
        cardColor.copy(alpha = 0.18f).compositeOver(MaterialTheme.colorScheme.surface)
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDeleteClick()
                true
            } else false
        }
    )

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
                    .background(
                        color = if (isSwipingToDismiss) Color.Red.copy(alpha = 0.8f) else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (isSwipingToDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.delete_desc),
                        tint = Color.White
                    )
                }
            }
        },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 12.dp)
                    .clickable { onCardClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = containerColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp)
                        .alpha(contentAlpha),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = cardColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getIconByName(habit.iconName),
                            contentDescription = habit.name,
                            tint = cardColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = habit.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textDecoration = textDecoration
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = habit.frequency, // Або stringResource(R.string.frequency)
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Показуємо вогник, тільки якщо стрік більше 0
                            if (habit.currentStreak > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🔥 ${habit.currentStreak}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800) // Яскраво-помаранчевий
                                )
                            }
                        }
                    }

                    val progress = (habit.completedDates.size.toFloat() / habit.targetDays).coerceAtMost(1f)
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(48.dp),
                            color = cardColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeWidth = 3.dp,
                        )

                        Checkbox(
                            checked = isCompletedToday,
                            onCheckedChange = onCheckedChange,
                            colors = CheckboxDefaults.colors(
                                checkedColor = cardColor,
                                checkmarkColor = Color.White
                            )
                        )
                    }
                }
            }

            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.background, shape = CircleShape)
                    .border(1.dp, if (habit.isFavorite) cardColor.copy(alpha = 0.5f) else Color.Transparent, CircleShape)
            ) {
                Icon(
                    imageVector = if (habit.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = stringResource(R.string.favorite_desc),
                    tint = if (habit.isFavorite) cardColor else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}