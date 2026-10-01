package com.voidlinux

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.voidlinux.core.common.Constants
import com.voidlinux.core.common.Logger
import com.voidlinux.di.AppModule

class VoidApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        AppModule.init(this)

        createNotificationChannels()
        Logger.d("Void-Linux démarré")
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val security = NotificationChannel(
                Constants.CHANNEL_SECURITY,
                "Security Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertes de sécurité Void-Linux"
            }

            val system = NotificationChannel(
                Constants.CHANNEL_SYSTEM,
                "System",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "État du système Void-Linux"
            }

            val install = NotificationChannel(
                Constants.CHANNEL_INSTALL,
                "Installations",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Progression des installations"
            }

            manager.createNotificationChannel(security)
            manager.createNotificationChannel(system)
            manager.createNotificationChannel(install)
        }
    }
}