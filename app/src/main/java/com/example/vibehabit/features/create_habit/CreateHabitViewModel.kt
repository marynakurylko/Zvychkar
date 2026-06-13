package com.example.vibehabit.features.create_habit

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibehabit.R
import com.example.vibehabit.core.models.Habit
import com.example.vibehabit.core.notifications.NotificationHelper
import com.example.vibehabit.features.auth.AuthRepository
import com.example.vibehabit.core.ui.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateHabitUiState(
    val name: String = "",
    val colorHex: String = "#9D4EDD",
    val iconName: String = "Bolt",
    val frequencyType: String = "Daily",
    val customDays: Set<Int> = emptySet(),
    val targetDays: Int = 7,
    val isReminderEnabled: Boolean = false,
    val reminderTime: String = "09:00",
    val isSaving: Boolean = false
)

sealed class CreateHabitEvent {
    data class NameChanged(val name: String) : CreateHabitEvent()
    data class ColorChanged(val colorHex: String) : CreateHabitEvent()
    data class IconChanged(val iconName: String) : CreateHabitEvent()
    data class FrequencyChanged(val frequency: String) : CreateHabitEvent()
    data class CustomDayToggled(val day: Int) : CreateHabitEvent()
    data class TargetDaysChanged(val days: Int) : CreateHabitEvent()
    data class ReminderToggled(val enabled: Boolean) : CreateHabitEvent()
    data class ReminderTimeChanged(val time: String) : CreateHabitEvent()
    data class SetSaving(val isSaving: Boolean) : CreateHabitEvent()
}

@HiltViewModel
class CreateHabitViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val saveHabitUseCase: SaveHabitUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateHabitUiState())
    val uiState: StateFlow<CreateHabitUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private var isInitialized = false
    private var currentHabitId: String? = null // Змінено на String
    private var currentIsFavorite = false
    private var currentCompletedDates = emptyList<String>()

    fun initData(habit: Habit?) {
        if (isInitialized) return
        isInitialized = true

        if (habit != null) {
            currentHabitId = habit.id
            currentIsFavorite = habit.isFavorite
            currentCompletedDates = habit.completedDates

            val isCustom = habit.frequency.startsWith("Custom")
            _uiState.update {
                it.copy(
                    name = habit.name,
                    colorHex = habit.colorHex,
                    iconName = habit.iconName,
                    targetDays = habit.targetDays,
                    frequencyType = if (isCustom) "Custom" else habit.frequency,
                    isReminderEnabled = habit.reminderTime != null,
                    reminderTime = habit.reminderTime ?: "09:00"
                )
            }
        }
    }

    fun onEvent(event: CreateHabitEvent) {
        when (event) {
            is CreateHabitEvent.NameChanged -> _uiState.update { it.copy(name = event.name) }
            is CreateHabitEvent.ColorChanged -> _uiState.update { it.copy(colorHex = event.colorHex) }
            is CreateHabitEvent.IconChanged -> _uiState.update { it.copy(iconName = event.iconName) }
            is CreateHabitEvent.FrequencyChanged -> _uiState.update { it.copy(frequencyType = event.frequency) }
            is CreateHabitEvent.CustomDayToggled -> _uiState.update { state ->
                val days = state.customDays
                state.copy(customDays = if (days.contains(event.day)) days - event.day else days + event.day)
            }
            is CreateHabitEvent.TargetDaysChanged -> _uiState.update { it.copy(targetDays = event.days) }
            is CreateHabitEvent.ReminderToggled -> _uiState.update { it.copy(isReminderEnabled = event.enabled) }
            is CreateHabitEvent.ReminderTimeChanged -> _uiState.update { it.copy(reminderTime = event.time) }
            is CreateHabitEvent.SetSaving -> _uiState.update { it.copy(isSaving = event.isSaving) }
        }
    }

    fun saveHabit() {
        val state = _uiState.value
        val userId = authRepository.currentUser?.uid
        if (userId == null) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.Error(R.string.error_not_authorized)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val frequencyLabel = if (state.frequencyType == "Custom") {
                context.getString(R.string.custom_frequency_format, state.customDays.size)
            } else state.frequencyType

            val finalReminderTime = if (state.isReminderEnabled) state.reminderTime else null

            val habitId = currentHabitId ?: java.util.UUID.randomUUID().toString()

            saveHabitUseCase(
                userId = userId,
                habitId = habitId,
                name = state.name,
                colorHex = state.colorHex,
                iconName = state.iconName,
                targetDays = state.targetDays,
                frequency = frequencyLabel,
                reminderTime = finalReminderTime,
                isFavorite = currentIsFavorite,
                completedDates = currentCompletedDates
            ).onSuccess {
                handleReminder(habitId, state.name, finalReminderTime)
                _uiEvent.emit(UiEvent.Success)
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false) }
                _uiEvent.emit(UiEvent.ErrorText(error.localizedMessage ?: context.getString(R.string.error_unknown)))
            }
        }
    }

    private fun handleReminder(habitId: String, habitName: String, reminderTime: String?) {
        val notificationId = habitId.hashCode()

        if (reminderTime != null) {
            val parts = reminderTime.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toIntOrNull() ?: 9
                val minute = parts[1].toIntOrNull() ?: 0
                NotificationHelper.scheduleHabitReminder(context, notificationId, habitName, hour, minute)
            }
        } else {
            NotificationHelper.cancelHabitReminder(context, notificationId)
        }
    }
}