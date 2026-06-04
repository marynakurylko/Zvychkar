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

    var habitName by remember { mutableStateOf(habitToEdit?.name ?: "") }
    var targetDays by remember { mutableIntStateOf(habitToEdit?.targetDays ?: 7) }
    var selectedColor by remember { mutableStateOf(habitToEdit?.colorHex ?: neonColors[0]) }

    // Шукаємо індекс іконки
    var selectedIconIndex by remember {
        val index = iconNames.indexOf(habitToEdit?.iconName)
        mutableIntStateOf(if (index >= 0) index else 0)
    }

    val screenTitle = if (habitToEdit == null) "Create Habit" else "Edit Habit"

    // Визначаємо частоту
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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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

            // 1. Блок Title
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Title", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                TextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    placeholder = { Text("Наприклад: Ранкова пробіжка", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent, // Прибираємо нижню лінію
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }

            // 2. Блок Icon
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Icon", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                IconSelector(
                    icons = icons,
                    selectedIndex = selectedIconIndex,
                    onIconSelected = { selectedIconIndex = it }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Color", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                ColorSelector(colors = neonColors, selectedColorHex = selectedColor, onColorSelected = { selectedColor = it })
            }

            // 3. Блок Frequency
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Frequency", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                SegmentedControl(items = frequencies, selectedIndex = selectedFrequencyIndex, onItemSelection = { selectedFrequencyIndex = it })

                // Плавна анімація появи днів тижня
                AnimatedVisibility(
                    visible = frequencies[selectedFrequencyIndex] == "Custom",
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")
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
                Text("Target count", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                NumberStepper(value = targetDays, onValueChange = { targetDays = it })
            }

            Spacer(modifier = Modifier.weight(1f))

            // Великий неоновий Button
            Button(
                onClick = {
                    if (habitName.isNotBlank()) {
                        // Якщо "Custom", до назви частоти можна додати кількість днів
                        val frequencyLabel = if (frequencies[selectedFrequencyIndex] == "Custom") {
                            "Custom (${customDays.size} days)"
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
                    containerColor = Color(android.graphics.Color.parseColor(selectedColor)), // Кнопка фарбується в обраний колір!
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                enabled = habitName.isNotBlank()
            ) {
                Text("Save Habit", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// Прев'ю для швидкого перегляду
@Preview(showBackground = true)
@Composable
fun CreateHabitScreenPreview() {
    HabitTrackerTheme {
        CreateHabitScreen(onBackClick = {}, onSaveClick = { _, _, _, _, _ -> })
    }
}
