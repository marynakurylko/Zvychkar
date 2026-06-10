package com.example.vibehabit.widget

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.vibehabit.R
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

val HABIT_ID_KEY = ActionParameters.Key<Int>("habit_id")

class HabitWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            var habits by remember { mutableStateOf<List<Habit>>(emptyList()) }
            val currentUser = FirebaseAuth.getInstance().currentUser
            val todayStr = LocalDate.now().toString()

            LaunchedEffect(currentUser) {
                if (currentUser != null) {
                    FirebaseFirestore.getInstance().collection("users")
                        .document(currentUser.uid)
                        .collection("habits")
                        .addSnapshotListener { snapshot, _ ->
                            if (snapshot != null) {
                                habits = snapshot.documents.mapNotNull { it.toObject(Habit::class.java) }.sortedBy { it.id }
                            }
                        }
                } else {
                    habits = emptyList()
                }
            }

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xCC1E1E1E))
                    .padding(16.dp)
            ) {
                Text(
                    text = context.getString(R.string.notification_title),
                    style = TextStyle(
                        color = ColorProvider(day = Color.White, night = Color.White),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.height(12.dp))

                if (currentUser == null) {
                    Text(
                        text = context.getString(R.string.widget_login_prompt),
                        style = TextStyle(color = ColorProvider(day = Color.Gray, night = Color.Gray), fontSize = 14.sp)
                    )
                } else if (habits.isEmpty()) {
                    Text(
                        text = context.getString(R.string.widget_empty_state),
                        style = TextStyle(color = ColorProvider(day = Color.Gray, night = Color.Gray), fontSize = 14.sp)
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
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val todayStr = LocalDate.now().toString()
        val firestore = FirebaseFirestore.getInstance()

        val docRef = firestore.collection("users").document(currentUser.uid)
            .collection("habits").document(habitId.toString())

        try {
            val docSnapshot = Tasks.await(docRef.get())
            val habit = docSnapshot.toObject(Habit::class.java) ?: return

            val newDates = habit.completedDates.toMutableList()
            if (newDates.contains(todayStr)) newDates.remove(todayStr) else newDates.add(todayStr)

            Tasks.await(docRef.update("completedDates", newDates))
            HabitWidget().updateAll(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}