package com.example.vibehabit.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibehabit.components.ColorPicker
import com.example.vibehabit.ui.theme.HabitTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabitScreen(
    onBackClick: () -> Unit,
    onSaveClick: (name: String, colorHex: String) -> Unit
) {
    // Тимчасовий стан для вводу тексту (по замовчуванню порожньо)
    var habitName by remember { mutableStateOf("") }
    // Тимчасовий стан для кольору (по замовчуванню наш фіолетовий)
    var selectedColor by remember { mutableStateOf("#8A2BE2") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Нова звичка", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(24.dp), // Робимо гарні відступи від країв
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // Поле для вводу тексту
            OutlinedTextField(
                value = habitName,
                onValueChange = { habitName = it },
                label = { Text("Назва звички") },
                placeholder = { Text("Наприклад: Ранкова пробіжка") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            // Блок вибору кольору
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Колір картки",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                ColorPicker(
                    selectedColorHex = selectedColor,
                    onColorSelected = { selectedColor = it }
                )
            }

            Spacer(modifier = Modifier.weight(1f)) // Виштовхує кнопку в самий низ екрана

            // Кнопка Зберегти
            Button(
                onClick = {
                    if (habitName.isNotBlank()) {
                        onSaveClick(habitName, selectedColor)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp), // Сучасна висока кнопка
                shape = RoundedCornerShape(16.dp),
                // Робимо кнопку неактивною, якщо назва порожня
                enabled = habitName.isNotBlank()
            ) {
                Text("Створити", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Прев'ю для швидкого перегляду
@Preview(showBackground = true)
@Composable
fun CreateHabitScreenPreview() {
    HabitTrackerTheme {
        CreateHabitScreen(onBackClick = {}, onSaveClick = { _, _ -> })
    }
}