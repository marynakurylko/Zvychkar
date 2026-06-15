package com.example.vibehabit.features.habit_details

import androidx.compose.foundation.basicMarquee
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibehabit.R
import com.example.vibehabit.features.habit_details.components.HabitCalendar
import com.example.vibehabit.features.habit_details.components.NeonProgressRing
import com.example.vibehabit.core.ui.UiState
import com.example.vibehabit.features.dashboard.DashboardViewModel
import com.example.vibehabit.shared_viewmodels.HabitsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailsScreen(
    habitId: String,
    viewModel: DashboardViewModel,
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
                title = { Text(text = stringResource(R.string.stats_title), fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.basicMarquee()) },
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
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            NeonProgressRing(
                progress = progress,
                color = headerColor,
                centerText = stringResource(R.string.progress_format, totalCompleted, habit.targetDays),
                subtitle = stringResource(R.string.status_completed),
                modifier = Modifier.padding(vertical = 32.dp)
            )

            Text(
                text = habit.name,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )

            val displayFrequency = when (habit.frequency) {
                "Daily" -> stringResource(R.string.freq_daily)
                "Weekly" -> stringResource(R.string.freq_weekly)
                else -> habit.frequency
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

                HorizontalDivider(
                    modifier = Modifier.height(40.dp).width(1.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.best_streak_format, habit.bestStreak),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
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
