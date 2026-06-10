package com.example.vibehabit.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibehabit.R
import com.example.vibehabit.components.DailySummaryCard
import com.example.vibehabit.components.HabitCard
import com.example.vibehabit.viewmodels.HabitsViewModel
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.PartySystem
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: HabitsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onAddHabitClick: () -> Unit,
    onHabitClick: (Int) -> Unit
) {
    val habits by viewModel.habits.collectAsState()
    val username by viewModel.username.collectAsState()
    var showConfetti by remember { mutableStateOf(false) }

    var habitToDelete by remember { mutableStateOf<Int?>(null) }

    val today = LocalDate.now()
    val todayStr = today.toString()

    val sortedHabits = habits.sortedWith(
        compareBy<com.example.vibehabit.Habit> { it.completedDates.contains(todayStr) }
            .thenByDescending { it.isFavorite }
            .thenBy { it.id }
    )

    // Форматування дати тепер використовує ресурс
    val dateFormat = stringResource(R.string.dashboard_date_format)
    val dateFormatter = DateTimeFormatter.ofPattern(dateFormat)
    val dateText = today.format(dateFormatter).replaceFirstChar { it.uppercase() }

    val party = Party(
        speed = 0f,
        maxSpeed = 30f,
        damping = 0.9f,
        spread = 360,
        colors = listOf(0x8A2BE2, 0x00BCD4, 0xFF9800, 0xE91E63, 0x4CAF50),
        emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
        position = Position.Relative(0.5, 0.3)
    )

    val totalHabitsToday = sortedHabits.size
    val completedHabitsToday = sortedHabits.count { it.completedDates.contains(todayStr) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Привіт, $username! 👋",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHabitClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add, 
                    contentDescription = stringResource(R.string.add_habit_desc)
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                item {
                    DailySummaryCard(
                        dateText = dateText,
                        completedCount = completedHabitsToday,
                        totalCount = totalHabitsToday,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }

                // ДОДАЄМО ПЕРЕВІРКУ НА ПОРОЖНІЙ СТАН
                if (sortedHabits.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🚀",
                                fontSize = 64.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = "Твій шлях починається тут!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Додай свою першу звичку та почни формувати новий вайб.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                } else {
                    // ЯКЩО ЗВИЧКИ Є - ПОКАЗУЄМО ЇХ (ТВІЙ ІСНУЮЧИЙ КОД)
                    items(items = sortedHabits, key = { it.id }) { habit ->
                        val isCompletedToday = habit.completedDates.contains(todayStr)

                        HabitCard(
                            habit = habit,
                            onCheckedChange = {
                                if (!isCompletedToday) {
                                    val isLastForToday = (completedHabitsToday + 1) == totalHabitsToday
                                    val isTargetReached = (habit.completedDates.size + 1) == habit.targetDays
                                    if (isLastForToday || isTargetReached) {
                                        showConfetti = true
                                    }
                                }
                                viewModel.toggleHabitCompletion(habit.id, todayStr)
                            },
                            onFavoriteClick = { viewModel.toggleHabitFavorite(habit.id) },
                            onDeleteClick = { habitToDelete = habit.id },
                            onCardClick = { onHabitClick(habit.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }

            if (showConfetti) {
                KonfettiView(
                    modifier = Modifier.fillMaxSize(),
                    parties = listOf(party),
                    updateListener = object : OnParticleSystemUpdateListener {
                        override fun onParticleSystemEnded(system: PartySystem, activeSystems: Int) {
                            if (activeSystems == 0) showConfetti = false
                        }
                    }
                )
            }

            if (habitToDelete != null) {
                AlertDialog(
                    onDismissRequest = { habitToDelete = null },
                    title = {
                        Text(
                            text = "Видалити звичку?",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    text = {
                        Text("Ви впевнені, що хочете назавжди видалити цю звичку та всю її історію? Цю дію неможливо скасувати.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                // 1. Викликаємо метод ViewModel для реального видалення
                                viewModel.deleteHabit(habitToDelete!!)
                                // 2. Закриваємо модалку
                                habitToDelete = null
                            }
                        ) {
                            Text("Видалити", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { habitToDelete = null }) {
                            Text("Скасувати", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                )
            }
        }
    }
}