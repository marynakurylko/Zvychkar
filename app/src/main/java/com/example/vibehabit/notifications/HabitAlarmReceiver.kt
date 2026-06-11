package com.example.vibehabit.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.vibehabit.R

class HabitAlarmReceiver : BroadcastReceiver() {

    // Ця функція викличеться автоматично, коли спрацює AlarmManager
    override fun onReceive(context: Context, intent: Intent) {

        // Дістаємо дані, які ми поклали в інтент під час налаштування таймера
        val habitName = intent.getStringExtra("HABIT_NAME") ?: context.getString(R.string.notification_default_content)
        val habitId = intent.getIntExtra("HABIT_ID", 0)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Збираємо дизайн нашого пуш-сповіщення
        val notification = NotificationCompat.Builder(context, "habit_channel")
            // Обов'язкова іконка (використовуємо стандартну системну для старту)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(context.getString(R.string.notification_title)) // Заголовок
            .setContentText(context.getString(R.string.notification_content_prefix, habitName)) // Текст
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Високий пріоритет (зі звуком)
            .setAutoCancel(true) // Зникне після кліку
            .build()

        // Виводимо на екран!
        notificationManager.notify(habitId, notification)
    }
}