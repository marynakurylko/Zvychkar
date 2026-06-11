package com.example.vibehabit.viewmodels

import androidx.lifecycle.ViewModel
import com.example.vibehabit.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// 1. ЄДИНИЙ СТАН ЕКРАНА (UI State)
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

// 2. УСІ МОЖЛИВІ ДІЇ ЮЗЕРА (Events)
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

// 3. САМА VIEWMODEL
@HiltViewModel
class CreateHabitViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CreateHabitUiState())
    val uiState: StateFlow<CreateHabitUiState> = _uiState.asStateFlow()

    private var isInitialized = false

    fun initData(habit: Habit?) {
        if (isInitialized) return
        isInitialized = true

        if (habit != null) {
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
}