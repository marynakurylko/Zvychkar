package com.example.vibehabit.core.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class HabitWidgetReceiver : GlanceAppWidgetReceiver() {
    // Вказуємо, яким саме віджетом керує цей ресивер
    override val glanceAppWidget: GlanceAppWidget = HabitWidget()
}