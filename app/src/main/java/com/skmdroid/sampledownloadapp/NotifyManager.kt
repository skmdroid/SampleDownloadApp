package com.skmdroid.sampledownloadapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import androidx.core.app.NotificationCompat

class NotifyManager(private val context: Context) {

    fun setUpNotification(urlText: String): NotificationCompat.Builder {
        val notificationManager =
            context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = NotificationChannel(
            "my_channel_id_01",
            "My Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )

        notificationChannel.description = "Channel description"
        notificationChannel.enableVibration(true)
        notificationManager.createNotificationChannel(notificationChannel)

        val notificationBuilder = NotificationCompat.Builder(context, "my_channel_id_01")

        return notificationBuilder
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Downloading file")
            .setContentText("URL: $urlText")
    }
}