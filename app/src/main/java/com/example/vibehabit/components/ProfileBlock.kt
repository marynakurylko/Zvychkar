package com.example.vibehabit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibehabit.R
import com.example.vibehabit.viewmodels.HabitsViewModel
import com.example.vibehabit.core.UiState

@Composable
fun ProfileBlock(viewModel: HabitsViewModel) {
    val username by viewModel.username.collectAsState()
    val habitsState by viewModel.habitsState.collectAsState()
    val habits = (habitsState as? UiState.Success)?.data ?: emptyList()

    // Стейт для режиму редагування імені
    var isEditing by remember { mutableStateOf(false) }
    var nameInput by remember(username) { mutableStateOf(username) }

    // Маленька гейміфікація: вираховуємо ранг залежно від кількості звичок
    val userRank = when {
        habits.isEmpty() -> stringResource(R.string.rank_newbie)
        habits.sumOf { it.completedDates.size } > 20 -> stringResource(R.string.rank_ranger)
        habits.sumOf { it.completedDates.size } > 5 -> stringResource(R.string.rank_enthusiast)
        else -> stringResource(R.string.rank_beginner)
    }

    // Перша літера для аватарки
    val avatarLetter = if (username.isNotBlank()) username.first().uppercase() else "?"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(24.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Неонова градієнтна аватарка
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF9D4EDD), Color(0xFF00E5FF)) // Фіолетовий -> Блакитний неон
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatarLetter,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 2. Інформаційний блок профілю
        Column(modifier = Modifier.weight(1f)) {
            if (isEditing) {
                // Поле вводу, коли натиснули "Редагувати"
                TextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Статичне відображення імені та рангу
                Text(
                    text = username,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.rank_label, userRank),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 3. Кнопка перемикання режимів (Олівець / Галочка)
        IconButton(
            onClick = {
                if (isEditing) {
                    if (nameInput.isNotBlank()) {
                        viewModel.updateUsername(nameInput.trim())
                    }
                    isEditing = false
                } else {
                    isEditing = true
                }
            }
        ) {
            Icon(
                imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                contentDescription = stringResource(R.string.edit_profile_desc),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}