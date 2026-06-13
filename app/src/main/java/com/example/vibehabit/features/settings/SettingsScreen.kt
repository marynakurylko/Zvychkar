package com.example.vibehabit.features.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
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
import com.example.vibehabit.core.shared_components.SegmentedControl
import com.example.vibehabit.shared_viewmodels.HabitsViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import com.example.vibehabit.features.settings.components.ProfileBlock
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vibehabit.features.auth.AuthViewModel
import com.example.vibehabit.features.dashboard.DashboardViewModel
import com.example.vibehabit.core.utils.AppConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val currentLanguage by settingsViewModel.language.collectAsState(initial = "en")
    val selectedLanguageIndex = if (currentLanguage == "uk") 1 else 0

    val languages = listOf(
        stringResource(R.string.language_english),
        stringResource(R.string.language_ukrainian)
    )

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }
    var deletionError by remember { mutableStateOf<String?>(null) }

    var showFeedbackDialog by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf("") }
    var isFeedbackSubmitting by remember { mutableStateOf(false) }
    var feedbackSuccessMessage by remember { mutableStateOf<String?>(null) }

    val onLanguageSelected: (Int) -> Unit = remember(settingsViewModel) {
        { index ->
            val languageTag = if (index == 1) "uk" else "en"
            settingsViewModel.setLanguage(languageTag)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                },
                actions = {
                    IconButton(onClick = { showAboutDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.about_app_title),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
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
            ProfileBlock(dashboardViewModel = dashboardViewModel, profileViewModel = profileViewModel)
            Spacer(modifier = Modifier.height(24.dp))

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
                    onItemSelection = onLanguageSelected
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { showFeedbackDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.leave_feedback), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.leave_feedback), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.9f))
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = stringResource(R.string.sign_out), tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.sign_out), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.delete_account),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (authErrorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = authErrorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // About
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = {
                    Text(stringResource(R.string.about_app_title), fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(AppConstants.APP_NAME, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.app_version, "1.0.0"), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedButton(
                            onClick = { uriHandler.openUri("https://docs.google.com/document/d/1w8yoFZIXWyQD6dbl_l5vyZVtg5GiDRxTke8PuyP76Bo/edit?usp=sharing") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(stringResource(R.string.privacy_policy), color = MaterialTheme.colorScheme.onBackground)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { uriHandler.openUri("https://docs.google.com/document/d/1pUGQhJPctMYZ1PHh1znBgcj3lLu2MLaeELSZ3T3O0J8/edit?usp=sharing") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(stringResource(R.string.terms_of_service), color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text(stringResource(R.string.ok_button), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = {
                    Text(stringResource(R.string.sign_out_title), fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(stringResource(R.string.sign_out_desc))
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            authViewModel.signOut()
                        }
                    ) {
                        Text(stringResource(R.string.confirm_sign_out), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text(stringResource(R.string.cancel_button), color = MaterialTheme.colorScheme.primary)
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
                    Text(stringResource(R.string.delete_account_warning), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                },
                text = {
                    Text(stringResource(R.string.delete_account_desc))
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            deletionError = null
                            authViewModel.deleteAccount(
                                onSuccess = {
                                    authViewModel.signOut()
                                },
                                onError = { errorText ->
                                    deletionError = errorText
                                }
                            )
                        }
                    ) {
                        Text(stringResource(R.string.confirm_delete_account), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(R.string.cancel_button), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
        }

        if (deletionError != null) {
            AlertDialog(
                onDismissRequest = { deletionError = null },
                title = {
                    Text(stringResource(R.string.error_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                },
                text = {
                    Text(deletionError!!)
                },
                confirmButton = {
                    TextButton(onClick = { deletionError = null }) {
                        Text(stringResource(R.string.ok_button), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
        }

        if (showFeedbackDialog) {
            AlertDialog(
                onDismissRequest = { if (!isFeedbackSubmitting) showFeedbackDialog = false },
                title = {
                    Text(stringResource(R.string.feedback_title), fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text(
                            text = stringResource(R.string.feedback_desc),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = feedbackText,
                            onValueChange = { feedbackText = it },
                            placeholder = { Text(stringResource(R.string.feedback_placeholder)) },
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
                                profileViewModel.sendFeedback(
                                    message = feedbackText,
                                    onSuccess = {
                                        isFeedbackSubmitting = false
                                        showFeedbackDialog = false
                                        feedbackText = ""
                                        feedbackSuccessMessage = context.getString(R.string.feedback_success_msg)
                                    },
                                    onError = { error ->
                                        isFeedbackSubmitting = false
                                        deletionError = error
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
                            Text(stringResource(R.string.send_button), fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showFeedbackDialog = false },
                        enabled = !isFeedbackSubmitting
                    ) {
                        Text(stringResource(R.string.cancel_button), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
        }

        if (feedbackSuccessMessage != null) {
            AlertDialog(
                onDismissRequest = { feedbackSuccessMessage = null },
                icon = { Icon(Icons.Default.Check, contentDescription = stringResource(R.string.success_title), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp)) },
                title = { Text(stringResource(R.string.success_title), fontWeight = FontWeight.Bold) },
                text = { Text(feedbackSuccessMessage!!, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                confirmButton = {
                    TextButton(onClick = { feedbackSuccessMessage = null }) {
                        Text(stringResource(R.string.feedback_success_button), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}