package com.voidlinux.feature.location

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import com.voidlinux.core.common.Logger

/**
 * Fournit des utilitaires pour durcir la localisation système :
 * - Vérifier l'état du GPS
 * - Ouvrir les bons écrans de paramètres
 * - Détecter si un mock provider externe tourne
 */
class LocationShield(private val context: Context) {

    private val lm: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    data class LocationStatus(
        val gpsEnabled: Boolean,
        val networkEnabled: Boolean,
        val mockAllowed: Boolean,
        val locationMode: Int
    )

    fun getStatus(): LocationStatus {
        val gps = try {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) { false }

        val net = try {
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) { false }

        val mockAllowed = try {
            @Suppress("DEPRECATION")
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ALLOW_MOCK_LOCATION, 0
            ) == 1
        } catch (e: Exception) { false }

        val mode = try {
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.LOCATION_MODE
            )
        } catch (e: Exception) { -1 }

        return LocationStatus(gps, net, mockAllowed, mode)
    }

    fun openLocationSettings() {
        openIntent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    }

    fun openDeveloperSettings() {
        openIntent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
    }

    fun openMockLocationSettings() {
        try {
            val intent = Intent("android.settings.APPLICATION_DEVELOPMENT_SETTINGS")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            openDeveloperSettings()
        }
    }

    fun openAppDetailsSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Logger.e("Erreur openAppDetails", e)
        }
    }

    private fun openIntent(action: String) {
        try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Logger.e("Erreur openIntent $action", e)
        }
    }

    /**
     * Vérifie si l'appareil est considéré comme rooté (heuristique).
     */
    fun isDeviceRooted(): Boolean {
        val paths = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        return paths.any { java.io.File(it).exists() }
    }
}