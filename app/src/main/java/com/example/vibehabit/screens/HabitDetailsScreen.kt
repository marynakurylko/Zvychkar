package com.example.vibehabit.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibehabit.R
import com.example.vibehabit.components.HabitCalendar
import com.example.vibehabit.components.NeonProgressRing
import com.example.vibehabit.core.UiState
import com.example.vibehabit.viewmodels.HabitsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailsScreen(
    habitId: Int,
    viewModel: HabitsViewModel,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val habitsState by viewModel.habitsState.collectAsState()
    val habits = (habitsState as? UiState.Success)?.data ?: emptyList()
    val habit = habits.find { it.id == habitId }

    if (habit == null) return

    val headerColor = runCatching { Color(android.graphics.Color.parseColor(habit.colorHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    val scrollState = rememberScrollState()

    val totalCompleted = habit.completedDates.size
    val progress = (totalCompleted.toFloat() / habit.targetDays).coerceAtMost(1f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.back_desc)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Filled.Edit, 
                            contentDescription = stringResource(R.string.edit_habit_desc)
                        )
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

            NeonProgressRing(
                progress = progress,
                color = headerColor,
                centerText = "$totalCompleted / ${habit.targetDays}",
                subtitle = stringResource(R.string.status_completed),
                modifier = Modifier.padding(vertical = 32.dp)
            )

            Text(
                text = habit.name,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Відображаємо частоту. Якщо це стандартні значення - перекладаємо.
            val displayFrequency = when (habit.frequency) {
                "Daily" -> stringResource(R.string.freq_daily)
                "Weekly" -> stringResource(R.string.freq_weekly)
                else -> habit.frequency // Для Custom вже збережено локалізований рядок або формат
            }

            Text(
                text = stringResource(R.string.habit_frequency_prefix, displayFrequency),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Картка "Поточна серія"
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.streak_format, habit.currentStreak),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                    Text(
                        text = stringResource(R.string.current_streak),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Вертикальний розділювач
                HorizontalDivider(
                    modifier = Modifier.height(40.dp).width(1.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                )

                // Картка "Рекорд"
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.best_streak_format, habit.bestStreak),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700) // Золотий колір
                    )
                    Text(
                        text = stringResource(R.string.best_streak),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val onDayClick: (String) -> Unit = remember(viewModel, habit.id) {
                { clickedDateStr -> viewModel.toggleHabitCompletion(habitId = habit.id, dateStr = clickedDateStr) }
            }

            HabitCalendar(
                completedDates = habit.completedDates.toSet(),
                habitColor = headerColor,
                onDayClick = onDayClick
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
