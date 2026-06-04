package com.example.vibehabit.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibehabit.components.HabitCalendar
import com.example.vibehabit.viewmodels.HabitsViewModel
import com.example.vibehabit.components.NeonProgressRing
import androidx.compose.material.icons.filled.Edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailsScreen(
    habitId: Int,
    viewModel: HabitsViewModel,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val habits by viewModel.habits.collectAsState()
    val habit = habits.find { it.id == habitId }

    if (habit == null) return

    val headerColor = runCatching { Color(android.graphics.Color.parseColor(habit.colorHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    // Додаємо скрол, щоб екран красиво прокручувався на маленьких телефонах
    val scrollState = rememberScrollState()

    val totalCompleted = habit.completedDates.size
    val progress = (totalCompleted.toFloat() / habit.targetDays).coerceAtMost(1f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Habit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Шапка
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(headerColor.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
//                    .padding(32.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = habit.name,
//                    fontSize = 24.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = headerColor,
//                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
//                )
//            }
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            // Ряд зі швидкою статистикою
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                // Міні-картка "Всього разів"
//                StatCard(
//                    title = "Всього виконано",
//                    value = "${habit.completedDates.size} днів",
//                    modifier = Modifier.weight(1f)
//                )
//                // Можна додати ще метрики в майбутньому (наприклад, "Найдовша серія")
//                StatCard(
//                    title = "Статус сьогодні",
//                    value = if (habit.completedDates.contains(java.time.LocalDate.now().toString())) "Виконано 🔥" else "Очікує ⏳",
//                    modifier = Modifier.weight(1f)
//                )
//            }
//
//            Spacer(modifier = Modifier.height(24.dp))

            NeonProgressRing(
                progress = progress,
                color = headerColor,
                centerText = "$totalCompleted / ${habit.targetDays}",
                subtitle = "Виконано",
                modifier = Modifier.padding(vertical = 32.dp)
            )

            // Заголовок звички
            Text(
                text = habit.name,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Text(
                text = "Частота: ${habit.frequency}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Наш новий Календар!
            HabitCalendar(
                completedDates = habit.completedDates,
                habitColor = headerColor,
                onDayClick = { clickedDateStr ->
                    viewModel.toggleHabitCompletion(habitId = habit.id, dateStr = clickedDateStr)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Допоміжний компонент для красивих карток статистики
@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}