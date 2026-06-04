package com.example.vibehabit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.ceil

@Composable
fun HabitCalendar(
    completedDates: Set<String>,
    habitColor: Color,
    modifier: Modifier = Modifier
) {
    val currentMonth = YearMonth.now()
    val daysInMonth = currentMonth.lengthOfMonth()
    // Визначаємо день тижня (1 - Пн, 7 - Нд)
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value

    // Ефект "матового скла" для підкладки календаря
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            // Заголовок (Місяць і Рік)
            val monthName = currentMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("uk", "UA"))
            Text(
                text = monthName.replaceFirstChar { it.uppercase() } + " " + currentMonth.year,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Дні тижня
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Сітка днів
            val totalCells = daysInMonth + firstDayOfWeek - 1
            val rows = ceil(totalCells / 7.0).toInt()
            var currentDay = 1

            for (i in 0 until rows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (j in 0 until 7) {
                        if ((i == 0 && j < firstDayOfWeek - 1) || currentDay > daysInMonth) {
                            // Пусті клітинки до початку і після кінця місяця
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            // Реальний день
                            val date = currentMonth.atDay(currentDay)
                            val dateStr = date.toString()
                            val isCompleted = completedDates.contains(dateStr)
                            val isToday = date == LocalDate.now()

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f) // Робить клітинку ідеально квадратною
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    // Якщо день виконано - зафарбовуємо кольором звички
                                    .background(
                                        when {
                                            isCompleted -> habitColor
                                            isToday -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                            else -> Color.Transparent
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentDay.toString(),
                                    color = if (isCompleted) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isToday || isCompleted) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }
                            currentDay++
                        }
                    }
                }
            }
        }
    }
}