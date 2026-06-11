package com.example.vibehabit.core.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.vibehabit.R
import java.util.Calendar

object NotificationHelper {

    // Створюємо канал сповіщень (потрібно викликати один раз при старті застосунку)
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("habit_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Встановлюємо будильник
    fun scheduleHabitReminder(context: Context, habitId: Int, habitName: String, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Вказуємо, кого треба розбудити (наш HabitAlarmReceiver)
        val intent = Intent(context, HabitAlarmReceiver::class.java).apply {
            putExtra("HABIT_NAME", habitName)
            putExtra("HABIT_ID", habitId)
        }

        // Обгортка для інтенту, яка дозволяє AlarmManager-у виконати його в майбутньому
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId, // Унікальний ID, щоб різні звички не перезаписували одна одну
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Налаштовуємо час
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Якщо обраний час вже минув сьогодні, переносимо на завтра
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // Ставимо будильник. Використовуємо точний, якщо є дозвіл, інакше звичайний.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                // Фолбек на неточний будильник, якщо виникла помилка безпеки
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }

    // Функція для скасування нагадування (наприклад, при видаленні звички)
    fun cancelHabitReminder(context: Context, habitId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HabitAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, habitId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}