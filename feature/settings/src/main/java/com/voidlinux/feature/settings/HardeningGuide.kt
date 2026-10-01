package com.voidlinux.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.voidlinux.core.common.Logger

/**
 * Fournit les intents vers les écrans système pour durcir l'appareil.
 * Utilisé par le guide de durcissement.
 */
class HardeningGuide(private val context: Context) {

    data class HardeningStep(
        val id: String,
        val title: String,
        val description: String,
        val action: () -> Unit
    )

    fun openLocationSettings() = safeStart(Settings.ACTION_LOCATION_SOURCE_SETTINGS)

    fun openPrivacySettings() = safeStart(Settings.ACTION_PRIVACY_SETTINGS)

    fun openWifiSettings() = safeStart(Settings.ACTION_WIFI_SETTINGS)

    fun openBluetoothSettings() = safeStart(Settings.ACTION_BLUETOOTH_SETTINGS)

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        safeStart(intent)
    }

    fun openSecuritySettings() = safeStart(Settings.ACTION_SECURITY_SETTINGS)

    fun openNotificationSettings() {
        val intent = Intent("android.settings.APP_NOTIFICATION_SETTINGS").apply {
            putExtra("app_package", context.packageName)
            putExtra("app_uid", context.applicationInfo.uid)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        safeStart(intent)
    }

    fun openDeveloperSettings() =
        safeStart(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)

    fun openDataUsageSettings() =
        safeStart(Settings.ACTION_DATA_USAGE_SETTINGS)

    private fun safeStart(action: String) {
        try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Logger.e("Impossible d'ouvrir $action", e)
        }
    }

    private fun safeStart(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Logger.e("Impossible d'ouvrir l'intent", e)
        }
    }

    fun getSteps(): List<HardeningStep> = listOf(
        HardeningStep(
            id = "location",
            title = "Désactiver la localisation",
            description = "Coupe l'accès GPS pour toutes les applis",
            action = { openLocationSettings() }
        ),
        HardeningStep(
            id = "privacy",
            title = "Historique de localisation",
            description = "Efface et désactive l'historique Google",
            action = { openPrivacySettings() }
        ),
        HardeningStep(
            id = "wifi",
            title = "Wi-Fi",
            description = "À couper quand tu ne l'utilises pas",
            action = { openWifiSettings() }
        ),
        HardeningStep(
            id = "bluetooth",
            title = "Bluetooth",
            description = "À couper quand tu ne l'utilises pas",
            action = { openBluetoothSettings() }
        ),
        HardeningStep(
            id = "permissions",
            title = "Permissions de Void-Linux",
            description = "Révoque les permissions non nécessaires",
            action = { openAppSettings() }
        ),
        HardeningStep(
            id = "notifications",
            title = "Notifications",
            description = "Gère les alertes de sécurité",
            action = { openNotificationSettings() }
        ),
        HardeningStep(
            id = "data",
            title = "Usage des données",
            description = "Surveille la consommation réseau",
            action = { openDataUsageSettings() }
        ),
        HardeningStep(
            id = "security",
            title = "Paramètres de sécurité",
            description = "Vérifie le verrouillage et le chiffrement",
            action = { openSecuritySettings() }
        ),
        HardeningStep(
            id = "developer",
            title = "Options développeur",
            description = "Nécessaire pour la fausse position GPS",
            action = { openDeveloperSettings() }
        )
    )
}