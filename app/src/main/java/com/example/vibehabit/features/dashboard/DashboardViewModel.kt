package com.example.vibehabit.features.dashboard

import android.app.Application
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibehabit.core.models.Habit
import com.example.vibehabit.core.ui.UiState
import com.example.vibehabit.core.notifications.NotificationHelper
import com.example.vibehabit.features.auth.AuthRepository
import com.example.vibehabit.core.widget.HabitWidget
import com.example.vibehabit.shared_viewmodels.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    application: Application,
    private val authRepository: AuthRepository,
    private val habitRepository: HabitRepository
) : AndroidViewModel(application) {

    private val _habitsState = MutableStateFlow<UiState<List<Habit>>>(UiState.Loading)
    val habitsState: StateFlow<UiState<List<Habit>>> = _habitsState.asStateFlow()

    private val currentHabits: List<Habit>
        get() = (_habitsState.value as? UiState.Success)?.data ?: emptyList()

    private val _heatmapStats = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())
    val heatmapStats: StateFlow<Map<LocalDate, Int>> = _heatmapStats.asStateFlow()

    private var habitsJob: Job? = null

    init {
        observeUserForData()
    }

    private fun observeUserForData() {
        viewModelScope.launch {
            authRepository.getAuthStateFlow().collect { user ->
                if (user != null) {
                    listenToHabits(user.uid)
                } else {
                    habitsJob?.cancel()
                    _habitsState.value = UiState.Success(emptyList())
                    _heatmapStats.value = emptyMap()
                }
            }
        }
    }

    private fun listenToHabits(userId: String) {
        habitsJob?.cancel()
        _habitsState.value = UiState.Loading

        habitsJob = viewModelScope.launch {
            habitRepository.getHabitsFlow(userId)
                .catch { exception ->
                    _habitsState.value = UiState.Error(
                        exception.localizedMessage ?: "Не вдалося завантажити звички"
                    )
                }
                .collect { habitsList ->
                    _habitsState.value = UiState.Success(habitsList)
                    updateHeatmap(habitsList)
                    HabitWidget().updateAll(getApplication<Application>())
                }
        }
    }

    private fun updateHeatmap(habitsList: List<Habit>) {
        val stats = mutableMapOf<LocalDate, Int>()
        habitsList.forEach { habit ->
            habit.completedDates.forEach { dateStr ->
                try {
                    val date = LocalDate.parse(dateStr)
                    stats[date] = stats.getOrDefault(date, 0) + 1
                } catch (e: Exception) { }
            }
        }
        _heatmapStats.value = stats
    }

    fun toggleHabitCompletion(habitId: String, dateStr: String = LocalDate.now().toString()) {
        val userId = authRepository.currentUser?.uid ?: return
        val habit = currentHabits.find { it.id == habitId } ?: return

        val newDates = habit.completedDates.toMutableList()
        if (newDates.contains(dateStr)) newDates.remove(dateStr) else newDates.add(dateStr)

        viewModelScope.launch {
            habitRepository.updateHabitDates(userId, habitId, newDates)
        }
    }

    fun toggleHabitFavorite(habitId: String) {
        val userId = authRepository.currentUser?.uid ?: return
        val habit = currentHabits.find { it.id == habitId } ?: return

        viewModelScope.launch {
            habitRepository.updateHabitFavorite(userId, habitId, !habit.isFavorite)
        }
    }

    fun deleteHabit(habitId: String) {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            habitRepository.deleteHabit(userId, habitId)
            NotificationHelper.cancelHabitReminder(getApplication<Application>(), habitId.hashCode())
        }
    }
}