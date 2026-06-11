package com.example.vibehabit.features.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibehabit.R
import java.time.LocalDate

@Composable
fun HeatmapChart(heatmapData: Map<LocalDate, Int>) {
    val weeksToDisplay = 15 // Показуємо останні ~3.5 місяці
    val today = LocalDate.now()

    // Обчислюємо дату початку (Понеділок 15 тижнів тому)
    val currentDayOfWeek = today.dayOfWeek.value // 1 = Пн, 7 = Нд
    val daysToSubtract = (weeksToDisplay - 1) * 7 + (currentDayOfWeek - 1)
    val startDate = today.minusDays(daysToSubtract.toLong())

    // Дні тижня для підписів зліва - беремо з ресурсів
    val weekDaysLabels = listOf(
        stringResource(R.string.day_mon),
        stringResource(R.string.day_wed),
        stringResource(R.string.day_fri),
        stringResource(R.string.day_sun)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Підписи днів тижня
        Column(
            modifier = Modifier.padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (day in 0..6) {
                Box(modifier = Modifier.height(14.dp), contentAlignment = Alignment.Center) {
                    if (day % 2 == 0) { // Виводимо підпис через день (Пн, Ср, Пт, Нд)
                        Text(
                            text = weekDaysLabels.getOrNull(day / 2) ?: "",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Сама сітка хітмапи
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (week in 0 until weeksToDisplay) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (day in 0..6) {
                        val cellDate = startDate.plusDays((week * 7 + day).toLong())
                        val isFuture = cellDate.isAfter(today)
                        val count = heatmapData[cellDate] ?: 0

                        // Палітра інтенсивності (Неоновий блакитний)
                        val cellColor = when {
                            isFuture -> Color.Transparent
                            count == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            count == 1 -> Color(0xFF00606B) // Темно-бірюзовий
                            count == 2 -> Color(0xFF00A2B8) // Середній бірюзовий
                            count >= 3 -> Color(0xFF00E5FF) // Яскравий неон
                            else -> Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(cellColor)
                        )
                    }
                }
            }
        }
    }
}
