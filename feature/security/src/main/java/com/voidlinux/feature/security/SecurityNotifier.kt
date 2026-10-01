package com.voidlinux.feature.security

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.voidlinux.core.common.Constants

/**
 * Affiche les événements de sécurité en notification push.
 */
class SecurityNotifier(private val context: Context) {

    private val manager: NotificationManager =
        context.getSystemService(NotificationManager::class.java)

    fun notify(event: SecurityEvent) {
        val channel = when (event.severity) {
            SecurityEvent.Severity.CRITICAL,
            SecurityEvent.Severity.HIGH -> Constants.CHANNEL_SECURITY
            else -> Constants.CHANNEL_SYSTEM
        }

        val notif = NotificationCompat.Builder(context, channel)
            .setSmallIcon(iconFor(event))
            .setContentTitle(event.title)
            .setContentText(event.description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(event.description))
            .setPriority(priorityFor(event))
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        manager.notify(event.id.toInt(), notif)
    }

    private fun iconFor(event: SecurityEvent): Int = when (event.severity) {
        SecurityEvent.Severity.CRITICAL -> android.R.drawable.stat_sys_warning
        SecurityEvent.Severity.HIGH -> android.R.drawable.stat_notify_error
        SecurityEvent.Severity.MEDIUM -> android.R.drawable.stat_sys_warning
        SecurityEvent.Severity.LOW -> android.R.drawable.stat_notify_sync
    }

    private fun priorityFor(event: SecurityEvent): Int = when (event.severity) {
        SecurityEvent.Severity.CRITICAL,
        SecurityEvent.Severity.HIGH -> NotificationCompat.PRIORITY_HIGH
        SecurityEvent.Severity.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
        SecurityEvent.Severity.LOW -> NotificationCompat.PRIORITY_LOW
    }

    fun clearAll() {
        manager.cancelAll()
    }
}