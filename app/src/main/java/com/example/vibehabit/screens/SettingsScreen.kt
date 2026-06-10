package com.example.vibehabit.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vibehabit.R
import com.example.vibehabit.components.SegmentedControl
import com.example.vibehabit.viewmodels.SettingsViewModel
import com.example.vibehabit.viewmodels.HabitsViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp // НОВИЙ ІМПОРТ
import androidx.compose.ui.graphics.Color
import com.example.vibehabit.components.ProfileBlock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    habitsViewModel: HabitsViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val currentLanguage by settingsViewModel.language.collectAsState(initial = "en")
    val selectedLanguageIndex = if (currentLanguage == "uk") 1 else 0

    val languages = listOf(
        stringResource(R.string.language_english),
        stringResource(R.string.language_ukrainian)
    )

    // Стан для показу модалки підтвердження виходу
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }
    var deletionErrorByFirebase by remember { mutableStateOf<String?>(null) }

    var showFeedbackDialog by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf("") }
    var isFeedbackSubmitting by remember { mutableStateOf(false) }
    var feedbackSuccessMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            ProfileBlock(viewModel = habitsViewModel)
            Spacer(modifier = Modifier.height(24.dp))

            // Картка з налаштуванням теми
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dark_theme_label),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = onThemeChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.choose_language),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )

                SegmentedControl(
                    items = languages,
                    selectedIndex = selectedLanguageIndex,
                    onItemSelection = { index ->
                        val languageTag = if (index == 1) "uk" else "en"
                        settingsViewModel.setLanguage(languageTag)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { showFeedbackDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Відгук", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Залишити відгук", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.weight(1f)) // Відштовхуємо кнопку виходу в самий низ

            // Кнопка Sign Out
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.9f))
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Вийти", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Вийти з акаунта", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Видалити акаунт назавжди",
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Відображення помилки безпеки (якщо треба перелогінитись)
            if (authErrorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = authErrorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Модалка підтвердження
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = {
                    Text("Вихід з акаунта", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("Ви впевнені, що хочете вийти? Ваші звички безпечно збережені у хмарі і будуть чекати на ваше повернення.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            habitsViewModel.signOut() // Розлогінюємо через Firebase
                        }
                    ) {
                        Text("Так, вийти", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Скасувати", color = MaterialTheme.colorScheme.primary)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text("💥 Незворотна дія!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                },
                text = {
                    Text("Ви впевнені, що хочете видалити акаунт? Усі ваші створені звички, налаштування та історія стріків у хмарі будуть знищені назавжди без можливості відновлення.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            deletionErrorByFirebase = null // Скидаємо попередній стан
                            habitsViewModel.deleteAccount(
                                onSuccess = {
                                    habitsViewModel.signOut()
                                },
                                onError = { errorText ->
                                    deletionErrorByFirebase = errorText
                                }
                            )
                        }
                    ) {
                        Text("Так, видалити назавжди", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Скасувати", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
        }

        if (deletionErrorByFirebase != null) {
            AlertDialog(
                onDismissRequest = { deletionErrorByFirebase = null },
                title = {
                    Text("⚠️ Щось пішло не так", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                },
                text = {
                    Text(deletionErrorByFirebase!!)
                },
                confirmButton = {
                    TextButton(onClick = { deletionErrorByFirebase = null }) {
                        Text("Зрозуміло", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
        }

        // МОДАЛКА ЗВОРОТНОГО ЗВ'ЯЗКУ
        if (showFeedbackDialog) {
            AlertDialog(
                onDismissRequest = { if (!isFeedbackSubmitting) showFeedbackDialog = false },
                title = {
                    Text("Зворотний зв'язок 💜", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text(
                            text = "Знайшли баг чи маєте ідею? Напишіть нам, і ми зробимо застосунок ще кращим!",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = feedbackText,
                            onValueChange = { feedbackText = it },
                            placeholder = { Text("Ваше повідомлення...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (feedbackText.isNotBlank()) {
                                isFeedbackSubmitting = true
                                habitsViewModel.sendFeedback(
                                    message = feedbackText,
                                    onSuccess = {
                                        isFeedbackSubmitting = false
                                        showFeedbackDialog = false
                                        feedbackText = "" // Очищаємо поле
                                        feedbackSuccessMessage = "Дякуємо! Ваш відгук успішно надіслано ✨"
                                    },
                                    onError = { error ->
                                        isFeedbackSubmitting = false
                                        // Можна перевикористати існуючий стейт помилки
                                        deletionErrorByFirebase = error
                                    }
                                )
                            }
                        },
                        enabled = feedbackText.isNotBlank() && !isFeedbackSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isFeedbackSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Надіслати", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showFeedbackDialog = false },
                        enabled = !isFeedbackSubmitting
                    ) {
                        Text("Скасувати", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
        }

        // ПОВІДОМЛЕННЯ ПРО УСПІШНУ ВІДПРАВКУ
        if (feedbackSuccessMessage != null) {
            AlertDialog(
                onDismissRequest = { feedbackSuccessMessage = null },
                icon = { Icon(Icons.Default.Check, contentDescription = "Успіх", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp)) },
                title = { Text("Успішно", fontWeight = FontWeight.Bold) },
                text = { Text(feedbackSuccessMessage!!, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                confirmButton = {
                    TextButton(onClick = { feedbackSuccessMessage = null }) {
                        Text("Супер", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}