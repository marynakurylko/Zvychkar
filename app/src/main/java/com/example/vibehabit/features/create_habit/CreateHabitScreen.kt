package com.example.vibehabit.features.create_habit

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vibehabit.core.models.Habit
import com.example.vibehabit.R
import com.example.vibehabit.core.shared_components.ColorSelector
import com.example.vibehabit.core.shared_components.IconSelector
import com.example.vibehabit.core.shared_components.NumberStepper
import com.example.vibehabit.core.shared_components.SegmentedControl
import com.example.vibehabit.core.theme.ui.theme.HabitTrackerTheme
import com.example.vibehabit.core.ui.UiEvent
import com.example.vibehabit.core.utils.HabitConstants
import com.example.vibehabit.shared_viewmodels.HabitsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabitScreen(
    habitToEdit: Habit? = null,
    onBackClick: () -> Unit,
    formViewModel: CreateHabitViewModel = hiltViewModel()
) {
    val uiState by formViewModel.uiState.collectAsState()

    LaunchedEffect(habitToEdit) {
        formViewModel.initData(habitToEdit)
    }

    val neonColors = HabitConstants.NEON_COLORS
    val icons = HabitConstants.HabitIcon.entries.map { it.imageVector }
    val iconNames = HabitConstants.HabitIcon.entries.map { it.iconName }
    val frequencies = listOf("Daily", "Weekly", "Custom")
    val frequencyLabels = listOf(
        stringResource(R.string.freq_daily),
        stringResource(R.string.freq_weekly),
        stringResource(R.string.freq_custom)
    )

    val selectedIconIndex = iconNames.indexOf(uiState.iconName).coerceAtLeast(0)
    val selectedFrequencyIndex = frequencies.indexOf(uiState.frequencyType).coerceAtLeast(0)

    val screenTitle = if (habitToEdit == null) stringResource(R.string.create_habit_title) else stringResource(R.string.edit_habit_title)

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    fun showTimePicker() {
        val parts = uiState.reminderTime.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        TimePickerDialog(context, { _, hour, minute ->
            formViewModel.onEvent(CreateHabitEvent.ReminderTimeChanged(String.format("%02d:%02d", hour, minute)))
        }, h, m, true).show()
    }

    LaunchedEffect(Unit) {
        formViewModel.uiEvent.collect { event ->
            formViewModel.onEvent(CreateHabitEvent.SetSaving(false))

            when (event) {
                is UiEvent.Success -> {
                    onBackClick()
                }
                is UiEvent.Error -> {
                    snackbarHostState.showSnackbar(context.getString(event.messageResId))
                }
                is UiEvent.ErrorText -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    val onNameChanged: (String) -> Unit = remember(formViewModel) {
        { formViewModel.onEvent(CreateHabitEvent.NameChanged(it)) }
    }
    val onIconSelected: (Int) -> Unit = remember(formViewModel, iconNames) {
        { index -> formViewModel.onEvent(CreateHabitEvent.IconChanged(iconNames[index])) }
    }
    val onColorSelected: (String) -> Unit = remember(formViewModel) {
        { color -> formViewModel.onEvent(CreateHabitEvent.ColorChanged(color)) }
    }
    val onFrequencySelected: (Int) -> Unit = remember(formViewModel, frequencies) {
        { index -> formViewModel.onEvent(CreateHabitEvent.FrequencyChanged(frequencies[index])) }
    }
    val onCustomDayToggled: (Int) -> Unit = remember(formViewModel) {
        { day -> formViewModel.onEvent(CreateHabitEvent.CustomDayToggled(day)) }
    }
    val onTargetDaysChanged: (Int) -> Unit = remember(formViewModel) {
        { days -> formViewModel.onEvent(CreateHabitEvent.TargetDaysChanged(days)) }
    }
    val onReminderToggled: (Boolean) -> Unit = remember(formViewModel) {
        { enabled -> formViewModel.onEvent(CreateHabitEvent.ReminderToggled(enabled)) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(screenTitle, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc)) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp, vertical = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.habit_title_label), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                TextField(
                    value = uiState.name,
                    onValueChange = onNameChanged,
                    placeholder = { Text(stringResource(R.string.habit_title_placeholder), color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                    singleLine = true
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.habit_icon_label), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                IconSelector(icons = icons, selectedIndex = selectedIconIndex, onIconSelected = onIconSelected)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.habit_color_label), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                ColorSelector(colors = neonColors, selectedColorHex = uiState.colorHex, onColorSelected = onColorSelected)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.habit_frequency_label), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                SegmentedControl(items = frequencyLabels, selectedIndex = selectedFrequencyIndex, onItemSelection = onFrequencySelected)

                AnimatedVisibility(visible = uiState.frequencyType == "Custom", enter = expandVertically(), exit = shrinkVertically()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        val weekDays = listOf(
                            stringResource(R.string.day_mon), stringResource(R.string.day_tue), stringResource(R.string.day_wed),
                            stringResource(R.string.day_thu), stringResource(R.string.day_fri), stringResource(R.string.day_sat), stringResource(R.string.day_sun)
                        )
                        weekDays.forEachIndexed { index, day ->
                            val dayNumber = index + 1
                            val isSelected = uiState.customDays.contains(dayNumber)
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onCustomDayToggled(dayNumber) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(day, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.target_count_label), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                NumberStepper(value = uiState.targetDays, onValueChange = onTargetDaysChanged)
            }

            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(stringResource(R.string.reminders_enable_label), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text(stringResource(R.string.reminders_enable_desc), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = uiState.isReminderEnabled, onCheckedChange = onReminderToggled, colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary))
                }

                AnimatedVisibility(visible = uiState.isReminderEnabled, enter = expandVertically(), exit = shrinkVertically()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp)).clickable { showTimePicker() }.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Notifications, contentDescription = stringResource(R.string.time_desc), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(R.string.reminders_time_label), fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(uiState.reminderTime, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Button(
                onClick = {
                    if (uiState.name.isNotBlank() && !uiState.isSaving) {
                        formViewModel.saveHabit()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(android.graphics.Color.parseColor(uiState.colorHex)), disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant),
                enabled = uiState.name.isNotBlank() && !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), color = Color.White, strokeWidth = 3.dp)
                } else {
                    Text(stringResource(R.string.save_habit_button), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateHabitScreenPreview() {
    HabitTrackerTheme {
        CreateHabitScreen(onBackClick = {})
    }
}