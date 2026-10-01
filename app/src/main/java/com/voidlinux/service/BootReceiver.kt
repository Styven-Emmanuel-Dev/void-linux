package com.voidlinux.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Redémarre le service foreground au boot de l'appareil.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            VoidForegroundService.start(context)
        }
    }
}