package com.voidlinux.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.voidlinux.MainActivity
import com.voidlinux.R
import com.voidlinux.core.common.Constants

/**
 * Service foreground qui maintient Void-Linux en vie :
 * - Surveillance sécurité
 * - Proxy Tor
 * - Sessions proot actives
 */
class VoidForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForeground(Constants.FOREGROUND_SERVICE_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, Constants.CHANNEL_SYSTEM)
            .setContentTitle(getString(R.string.notif_foreground_title))
            .setContentText(getString(R.string.notif_foreground_text))
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        fun start(context: android.content.Context) {
            val intent = Intent(context, VoidForegroundService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: android.content.Context) {
            context.stopService(Intent(context, VoidForegroundService::class.java))
        }
    }
}