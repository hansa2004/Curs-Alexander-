package com.example.curs_alexander.export

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.curs_alexander.R
import com.example.curs_alexander.notifications.NotificationChannels

object ExportNotification {

    private const val NOTIF_ID = 1001

    fun showSaved(context: Context, uri: Uri) {
        NotificationChannels.ensure(context)

        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pi = PendingIntent.getActivity(context, 0, openIntent, flags)

        val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Отчёт сохранён")
            .setContentText("PDF-файл сохранён в папку Загрузки. Нажмите, чтобы открыть.")
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_ID, notification)
    }
}

