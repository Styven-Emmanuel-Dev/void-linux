package com.voidlinux.feature.security

import android.app.ActivityManager
import android.content.Context
import com.voidlinux.core.common.Logger

/**
 * Détecte les services et processus suspects.
 * Sans root : on se limite à ActivityManager.getRunningServices.
 */
class ProcessMonitor(private val context: Context) {

    private val am: ActivityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    data class RunningServiceInfo(
        val packageName: String,
        val className: String,
        val foreground: Boolean,
        val pid: Int
    )

    fun getRunningServices(): List<RunningServiceInfo> {
        return try {
            @Suppress("DEPRECATION")
            am.getRunningServices(200).map { s ->
                RunningServiceInfo(
                    packageName = s.service.packageName,
                    className = s.service.className,
                    foreground = s.foreground,
                    pid = s.pid
                )
            }
        } catch (e: Exception) {
            Logger.e("Erreur ProcessMonitor", e)
            emptyList()
        }
    }

    /**
     * Détecte les services en arrière-plan suspects (ceux qui ne
     * devraient pas tourner en permanence).
     */
    fun detectSuspicious(
        knownServices: Set<String>,
        onEvent: (SecurityEvent) -> Unit
    ) {
        val services = getRunningServices()

        services.forEach { svc ->
            val key = "${svc.packageName}/${svc.className}"
            if (key !in knownServices && !svc.foreground) {
                onEvent(
                    SecurityEvent(
                        type = SecurityEvent.EventType.PROCESS_SUSPICIOUS,
                        severity = SecurityEvent.Severity.LOW,
                        title = "Service en arrière-plan",
                        description = key,
                        packageName = svc.packageName
                    )
                )
            }
        }
    }

    companion object {
        /** Liste des services connus et approuvés par défaut */
        val DEFAULT_KNOWN = setOf(
            "com.android.systemui/.SystemUIService",
            "com.android.settings/.Settings\$BatteryService"
        )
    }
}