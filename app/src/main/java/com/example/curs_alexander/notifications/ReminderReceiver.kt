package com.example.curs_alexander.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.curs_alexander.MainActivity
import com.example.curs_alexander.R
import com.example.curs_alexander.data.db.DbProvider
import com.example.curs_alexander.data.reminders.ReminderType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SHOW_REMINDER) return
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1)
        if (reminderId <= 0) return

        // Чтобы не блокировать onReceive, читаем БД в корутине.
        CoroutineScope(Dispatchers.IO).launch {
            val db = DbProvider.get(context)
            val reminder = db.reminderDao().getById(reminderId) ?: return@launch

            if (!reminder.enabled) return@launch

            NotificationChannels.ensure(context)

            val (title, text, destinationId) = when (reminder.type) {
                ReminderType.BLOOD_PRESSURE -> Triple(
                    "Напоминание",
                    "Время измерить давление (учебное напоминание).",
                    R.id.healthMeasurementAddFragment
                )
                ReminderType.SYMPTOM -> Triple(
                    "Напоминание",
                    "Время записать симптом (учебное напоминание).",
                    R.id.symptomAddFragment
                )
            }

            val contentIntent = buildOpenScreenPendingIntent(context, destinationId)

            val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_REMINDERS)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            with(NotificationManagerCompat.from(context)) {
                notify(reminderId.toInt(), notification)
            }

            // Планируем следующий запуск (ежедневно)
            ReminderAlarmScheduler.scheduleDaily(context, reminderId, reminder.hour, reminder.minute)
        }
    }

    private fun buildOpenScreenPendingIntent(context: Context, destinationId: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DESTINATION_ID, destinationId)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, destinationId, intent, flags)
    }

    companion object {
        const val ACTION_SHOW_REMINDER = "com.example.curs_alexander.action.SHOW_REMINDER"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"

        // Для MainActivity
        const val EXTRA_DESTINATION_ID = "extra_destination_id"
    }
}

