package com.example.vibehabit.features.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
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
import com.example.vibehabit.features.dashboard.components.DailySummaryCard
import com.example.vibehabit.features.dashboard.components.HabitCard
import com.example.vibehabit.core.models.Habit
import com.example.vibehabit.core.ui.UiState
import com.example.vibehabit.shared_viewmodels.HabitsViewModel
import kotlinx.coroutines.delay
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.PartySystem
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vibehabit.features.auth.AuthViewModel
import com.example.vibehabit.features.settings.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onAddHabitClick: () -> Unit,
    onHabitClick: (String) -> Unit
) {
    val habitsState by viewModel.habitsState.collectAsState()
    val username by profileViewModel.username.collectAsState()
    val isEmailVerified by authViewModel.isEmailVerified.collectAsState()
    var showConfetti by remember { mutableStateOf(false) }

    var resendCooldown by remember { mutableStateOf(0) }
    var bannerMessage by remember { mutableStateOf<String?>(null) }
    var isBannerError by remember { mutableStateOf(false) }

    LaunchedEffect(resendCooldown) {
        if (resendCooldown > 0) {
            delay(1000L)
            resendCooldown--
        }
    }

    var habitToDelete by remember { mutableStateOf<String?>(null) }

    val today = LocalDate.now()
    val todayStr = today.toString()

    val currentHabits = (habitsState as? UiState.Success)?.data ?: emptyList()
    val totalHabitsToday = currentHabits.size
    val completedHabitsToday = currentHabits.count { it.completedDates.contains(todayStr) }

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

    val sendingMsg = stringResource(R.string.sending_label)
    val successMsg = stringResource(R.string.email_sent_success)
    val checkingMsg = stringResource(R.string.checking_label)
    val notVerifiedMsg = stringResource(R.string.email_not_verified)

    val onFavoriteToggle: (String) -> Unit = remember(viewModel) {
        { habitId -> viewModel.toggleHabitFavorite(habitId) }
    }

    val onDeleteHabit: (String) -> Unit = remember {
        { habitId -> habitToDelete = habitId }
    }

    val onCardClicked: (String) -> Unit = remember(onHabitClick) {
        { habitId -> onHabitClick(habitId) }
    }

    val onHabitToggle: (Habit, Boolean) -> Unit = remember(viewModel, completedHabitsToday, totalHabitsToday, todayStr) {
        { habit, isCompletedToday ->
            if (!isCompletedToday) {
                val isLastForToday = (completedHabitsToday + 1) == totalHabitsToday
                val isTargetReached = (habit.completedDates.size + 1) == habit.targetDays
                if (isLastForToday || isTargetReached) {
                    showConfetti = true
                }
            }
            viewModel.toggleHabitCompletion(habit.id, todayStr)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.dashboard_greeting, username ?: ""),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 22.sp,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
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

                if (!isEmailVerified) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF9D4EDD).copy(alpha = 0.15f)
                            ),
                            border = BorderStroke(1.dp, Color(0xFF9D4EDD).copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = stringResource(R.string.email_label),
                                        tint = Color(0xFF9D4EDD)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.verify_email_title),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.verify_email_desc),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (bannerMessage != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = bannerMessage!!,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isBannerError) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            resendCooldown = 30
                                            bannerMessage = sendingMsg
                                            isBannerError = false

                                            authViewModel.resendVerificationEmail(
                                                onSuccess = {
                                                    isBannerError = false
                                                    bannerMessage = successMsg
                                                },
                                                onError = { error ->
                                                    isBannerError = true
                                                    bannerMessage = error
                                                    resendCooldown = 0
                                                }
                                            )
                                        },
                                        enabled = resendCooldown == 0
                                    ) {
                                        Text(
                                            text = if (resendCooldown > 0) stringResource(R.string.resend_cooldown, resendCooldown) else stringResource(R.string.resend_email),
                                            color = if (resendCooldown > 0) Color.Gray else Color(0xFF9D4EDD)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            bannerMessage = checkingMsg
                                            isBannerError = false

                                            authViewModel.reloadUser(
                                                onResult = { verified ->
                                                    if (!verified) {
                                                        isBannerError = true
                                                        bannerMessage = notVerifiedMsg
                                                    }
                                                },
                                                onError = { error ->
                                                    isBannerError = true
                                                    bannerMessage = error
                                                }
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD))
                                    ) {
                                        Text(stringResource(R.string.i_confirmed))
                                    }
                                }
                            }
                        }
                    }
                }

                when (val state = habitsState) {
                    is UiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    is UiState.Error -> {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("⚠️", fontSize = 48.sp, modifier = Modifier.padding(bottom = 16.dp))
                                Text(
                                    text = "Халепа!",
                                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = state.message,
                                    fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                    is UiState.Success -> {
                        val habitsList = state.data
                        val sortedHabits = habitsList.sortedWith(
                            compareBy<Habit> { it.completedDates.contains(todayStr) }
                                .thenByDescending { it.isFavorite }
                                .thenBy { it.id }
                        )

                        if (sortedHabits.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "🚀",
                                        fontSize = 64.sp,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.empty_habits_title),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.empty_habits_desc),
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 32.dp)
                                    )
                                }
                            }
                        } else {
                            items(items = sortedHabits, key = { it.id }) { habit ->
                                val isCompletedToday = habit.completedDates.contains(todayStr)

                                HabitCard(
                                    habit = habit,
                                    onCheckedChange = { onHabitToggle(habit, isCompletedToday) },
                                    onFavoriteClick = { onFavoriteToggle(habit.id) },
                                    onDeleteClick = { onDeleteHabit(habit.id) },
                                    onCardClick = { onCardClicked(habit.id) },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
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
                            text = stringResource(R.string.delete_habit_dialog_title),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    text = {
                        Text(stringResource(R.string.delete_habit_dialog_desc))
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteHabit(habitToDelete!!)
                                habitToDelete = null
                            }
                        ) {
                            Text(stringResource(R.string.delete_desc), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { habitToDelete = null }) {
                            Text(stringResource(R.string.cancel_button), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                )
            }
        }
    }
}