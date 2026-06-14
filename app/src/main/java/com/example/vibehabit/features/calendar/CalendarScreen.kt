package com.example.vibehabit.features.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibehabit.R
import com.example.vibehabit.core.ui.UiState
import com.example.vibehabit.features.dashboard.DashboardViewModel
import com.example.vibehabit.shared_viewmodels.HabitsViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: DashboardViewModel) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val habitsState by viewModel.habitsState.collectAsState()
    val habits = (habitsState as? UiState.Success)?.data ?: emptyList()

    val dates = remember {
        val today = LocalDate.now()
        (-7..7L).map { today.plusDays(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.calendar_title), fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.basicMarquee()) },
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
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(vertical = 16.dp)
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(dates) { date ->
                        val isSelected = date == selectedDate
                        val isToday = date == LocalDate.now()
                        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

                        Column(
                            modifier = Modifier
                                .width(56.dp)
                                .height(72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { selectedDate = date }
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = dayOfWeek.replaceFirstChar { it.uppercase() },
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = date.dayOfMonth.toString(),
                                fontSize = 18.sp,
                                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (selectedDate == LocalDate.now()) stringResource(R.string.today_label) else selectedDate.toString(),
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            val onToggleCompletion: (String, String) -> Unit = remember(viewModel) {
                { habitId, dateStr -> viewModel.toggleHabitCompletion(habitId, dateStr) }
            }

            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = stringResource(R.string.calendar_empty_state),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(habits) { habit ->
                        val isCompletedOnSelectedDate = habit.completedDates.contains(selectedDate.toString())
                        val habitColor = runCatching { Color(android.graphics.Color.parseColor(habit.colorHex)) }
                            .getOrDefault(MaterialTheme.colorScheme.surface)
                        val isPastOrToday = !selectedDate.isAfter(LocalDate.now())

                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .defaultMinSize(minHeight = 64.dp)
                                .clip(RoundedCornerShape(size = 16.dp))
                                .border(
                                    border = if (isPressed) BorderStroke(
                                        0.dp, Color.Transparent
                                    ) else BorderStroke(0.dp, Color.Transparent),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .background(
                                    color = if (isCompletedOnSelectedDate) habitColor.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = LocalIndication.current,
                                    enabled = isPastOrToday
                                ) {
                                    onToggleCompletion(habit.id, selectedDate.toString())
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = habit.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                modifier = Modifier
                                    .weight(1f)
                                    .basicMarquee()
                                    .padding(end = 8.dp)
                            )

                            if (isCompletedOnSelectedDate) {
                                Box(
                                    modifier = Modifier
                                        .background(habitColor, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.status_completed),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier.defaultMinSize(minHeight = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isPastOrToday) stringResource(R.string.status_missed) else stringResource(R.string.status_waiting),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}