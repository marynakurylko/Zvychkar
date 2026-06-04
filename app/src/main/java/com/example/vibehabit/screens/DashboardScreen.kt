package com.example.vibehabit.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vibehabit.components.HabitCard
import com.example.vibehabit.viewmodels.HabitsViewModel
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.PartySystem
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: HabitsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
                    onAddHabitClick: () -> Unit,
                    onHabitClick: (Int) -> Unit) {
    // Collect the state from the ViewModel
    val habits by viewModel.habits.collectAsState()

    var showConfetti by remember { mutableStateOf(false) }

    val party = Party(
        speed = 0f,
        maxSpeed = 30f,
        damping = 0.9f,
        spread = 360,
        colors = listOf(0x8A2BE2, 0x00BCD4, 0xFF9800, 0xE91E63, 0x4CAF50), // Наші фірмові кольори
        emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
        position = Position.Relative(0.5, 0.3) // Вибух трохи вище центру екрана
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "VibeHabit",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
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
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Habit")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(items = habits, key = { it.id }) { habit ->
                    val todayStr = LocalDate.now().toString()
                    val isCompletedToday = habit.completedDates.contains(todayStr)

                    HabitCard(
                        habit = habit,
                        onCheckedChange = {
                            // Якщо ми СТАВИМО галочку (а не знімаємо) — запускаємо свято!
                            if (!isCompletedToday) {
                                showConfetti = true
                            }
                            viewModel.toggleHabitCompletion(habit.id)
                        },
                        onFavoriteClick = { viewModel.toggleHabitFavorite(habit.id) },
                        onDeleteClick = { viewModel.deleteHabit(habit.id) },
                        onCardClick = { onHabitClick(habit.id) }
                    )
                }
            }

            // Шар із конфеті (малюється поверх усього)
            if (showConfetti) {
                KonfettiView(
                    modifier = Modifier.fillMaxSize(),
                    parties = listOf(party),
                    updateListener = object : OnParticleSystemUpdateListener {
                        override fun onParticleSystemEnded(system: PartySystem, activeSystems: Int) {
                            // Коли остання частинка падає — вимикаємо стан
                            if (activeSystems == 0) showConfetti = false
                        }
                    }
                )
            }
        }
    }
}