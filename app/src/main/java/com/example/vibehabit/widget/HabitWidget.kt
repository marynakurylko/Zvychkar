package com.example.vibehabit.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.CheckboxDefaults
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.TextDecoration
import com.example.vibehabit.Habit
import com.example.vibehabit.viewmodels.dataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import java.time.LocalDate

// Ключі виносимо на рівень файлу, щоб і віджет, і Action мали до них доступ
val HABIT_ID_KEY = ActionParameters.Key<Int>("habit_id")
val HABITS_KEY = stringPreferencesKey("habits_json")

class HabitWidget : GlanceAppWidget() {

    private val gson = Gson()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Отримуємо початковий стан ДО запуску UI
        val initialPrefs = context.dataStore.data.first()

        provideContent {
            val preferences by context.dataStore.data.collectAsState(initial = initialPrefs)
            val json = preferences[HABITS_KEY]

            // Якщо даних немає, повертаємо порожній список
            val habits: List<Habit> = if (json != null) {
                val type = object : TypeToken<List<Habit>>() {}.type
                gson.fromJson(json, type)
            } else {
                emptyList()
            }

            val todayStr = LocalDate.now().toString()

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xCC1E1E1E))
                    .padding(16.dp)
            ) {
                Text(
                    text = "VibeHabit \uD83D\uDE80",
                    style = TextStyle(
                        color = ColorProvider(day = Color.White, night = Color.White),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.height(12.dp))

                if (habits.isEmpty()) {
                    Text(
                        text = "На сьогодні планів немає!",
                        style = TextStyle(
                            color = ColorProvider(day = Color.Gray, night = Color.Gray),
                            fontSize = 14.sp
                        )
                    )
                } else {
                    LazyColumn {
                        items(habits) { habit ->
                            val isCompletedToday = habit.completedDates.contains(todayStr)

                            val toggleAction = actionRunCallback<ToggleHabitAction>(
                                actionParametersOf(HABIT_ID_KEY to habit.id)
                            )

                            Row(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable(onClick = toggleAction),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CheckBox(
                                    checked = isCompletedToday,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = ColorProvider(day = Color(0xFF9D4EDD), night = Color(0xFF9D4EDD)),
                                        uncheckedColor = ColorProvider(day = Color.Gray, night = Color.Gray)
                                    ),
                                    modifier = GlanceModifier.padding(end = 12.dp)
                                )

                                val textColor = if (isCompletedToday) Color.Gray else Color.White
                                Text(
                                    text = habit.name,
                                    style = TextStyle(
                                        color = ColorProvider(day = textColor, night = textColor),
                                        fontSize = 14.sp,
                                        textDecoration = if (isCompletedToday) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class ToggleHabitAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val habitId = parameters[HABIT_ID_KEY] ?: return
        val dataStore = context.dataStore
        val gson = Gson()
        val todayStr = LocalDate.now().toString()

        dataStore.edit { prefs ->
            val json = prefs[HABITS_KEY]
            val habits: List<Habit> = if (json != null) {
                val type = object : TypeToken<List<Habit>>() {}.type
                gson.fromJson(json, type)
            } else {
                emptyList()
            }

            val updatedHabits = habits.map { habit ->
                if (habit.id == habitId) {
                    val newDates = habit.completedDates.toMutableSet()
                    if (newDates.contains(todayStr)) newDates.remove(todayStr) else newDates.add(todayStr)
                    habit.copy(completedDates = newDates)
                } else habit
            }
            prefs[HABITS_KEY] = gson.toJson(updatedHabits)
        }

        // Оновлюємо всі екземпляри віджета
        HabitWidget().updateAll(context)
    }
}