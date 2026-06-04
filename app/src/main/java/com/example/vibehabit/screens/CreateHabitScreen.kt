package com.example.vibehabit.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibehabit.components.ColorSelector
import com.example.vibehabit.components.IconSelector
import com.example.vibehabit.components.NumberStepper
import com.example.vibehabit.components.SegmentedControl
import com.example.vibehabit.ui.theme.HabitTrackerTheme
import com.example.vibehabit.Habit
import androidx.compose.ui.res.stringResource
import com.example.vibehabit.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabitScreen(
    habitToEdit: Habit? = null,
    onBackClick: () -> Unit,
    onSaveClick: (name: String, colorHex: String, iconName: String, targetDays: Int, frequency: String) -> Unit
) {
    val neonColors = listOf("#9D4EDD", "#00E5FF", "#FF007F", "#00FF7F", "#FF9800")
    val icons = listOf(Icons.Filled.Bolt, Icons.Filled.Favorite, Icons.Filled.DirectionsBike, Icons.Filled.Book)
    val iconNames = listOf("Bolt", "Favorite", "Bike", "Book")
    val frequencies = listOf("Daily", "Weekly", "Custom")
    val frequencyLabels = listOf(
        stringResource(R.string.freq_daily),
        stringResource(R.string.freq_weekly),
        stringResource(R.string.freq_custom)
    )

    var habitName by remember { mutableStateOf(habitToEdit?.name ?: "") }
    var targetDays by remember { mutableIntStateOf(habitToEdit?.targetDays ?: 7) }
    var selectedColor by remember { mutableStateOf(habitToEdit?.colorHex ?: neonColors[0]) }

    var selectedIconIndex by remember {
        val index = iconNames.indexOf(habitToEdit?.iconName)
        mutableIntStateOf(if (index >= 0) index else 0)
    }

    val screenTitle = if (habitToEdit == null) {
        stringResource(R.string.create_habit_title)
    } else {
        stringResource(R.string.edit_habit_title)
    }

    var selectedFrequencyIndex by remember {
        val freq = habitToEdit?.frequency ?: "Daily"
        val index = if (freq.startsWith("Custom")) 2 else frequencies.indexOf(freq)
        mutableIntStateOf(if (index >= 0) index else 0)
    }

    var customDays by remember { mutableStateOf(setOf<Int>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.habit_title_label), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                TextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    placeholder = { Text(stringResource(R.string.habit_title_placeholder), color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.habit_icon_label), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                IconSelector(
                    icons = icons,
                    selectedIndex = selectedIconIndex,
                    onIconSelected = { selectedIconIndex = it }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.habit_color_label), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                ColorSelector(colors = neonColors, selectedColorHex = selectedColor, onColorSelected = { selectedColor = it })
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.habit_frequency_label), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                SegmentedControl(items = frequencyLabels, selectedIndex = selectedFrequencyIndex, onItemSelection = { selectedFrequencyIndex = it })

                AnimatedVisibility(
                    visible = frequencies[selectedFrequencyIndex] == "Custom",
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val weekDays = listOf(
                            stringResource(R.string.day_mon),
                            stringResource(R.string.day_tue),
                            stringResource(R.string.day_wed),
                            stringResource(R.string.day_thu),
                            stringResource(R.string.day_fri),
                            stringResource(R.string.day_sat),
                            stringResource(R.string.day_sun)
                        )
                        weekDays.forEachIndexed { index, day ->
                            val dayNumber = index + 1
                            val isSelected = customDays.contains(dayNumber)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        customDays = if (isSelected) customDays - dayNumber else customDays + dayNumber
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp, fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.target_count_label), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                NumberStepper(value = targetDays, onValueChange = { targetDays = it })
            }

            Spacer(modifier = Modifier.weight(1f))

            val customFreqFormat = stringResource(R.string.custom_frequency_format, customDays.size)
            Button(
                onClick = {
                    if (habitName.isNotBlank()) {
                        val frequencyLabel = if (frequencies[selectedFrequencyIndex] == "Custom") {
                            customFreqFormat
                        } else frequencies[selectedFrequencyIndex]

                        onSaveClick(
                            habitName,
                            selectedColor,
                            iconNames[selectedIconIndex],
                            targetDays,
                            frequencyLabel
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(android.graphics.Color.parseColor(selectedColor)),
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                enabled = habitName.isNotBlank()
            ) {
                Text(stringResource(R.string.save_habit_button), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateHabitScreenPreview() {
    HabitTrackerTheme {
        CreateHabitScreen(onBackClick = {}, onSaveClick = { _, _, _, _, _ -> })
    }
}