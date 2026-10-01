package com.voidlinux.feature.security

import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import com.voidlinux.core.common.Constants
import com.voidlinux.core.common.Logger

/**
 * Détecte les consommations réseau anormales par application.
 * Nécessite PACKAGE_USAGE_STATS (accordé manuellement par l'utilisateur).
 */
class NetworkMonitor(private val context: Context) {

    private val nsm: NetworkStatsManager? =
        context.getSystemService(Context.NETWORK_STATS_SERVICE)
            as? NetworkStatsManager

    data class AppNetworkUsage(
        val uid: Int,
        val rxBytes: Long,
        val txBytes: Long
    ) {
        val totalBytes: Long get() = rxBytes + txBytes
    }

    /**
     * Récupère l'usage réseau d'une app sur une période donnée.
     */
    fun getUsage(uid: Int, sinceMs: Long, untilMs: Long): AppNetworkUsage? {
        val manager = nsm ?: return null
        return try {
            val bucket = manager.queryDetailsForUid(
                ConnectivityManager.TYPE_WIFI,
                null,
                sinceMs,
                untilMs,
                uid
            )

            var rx = 0L
            var tx = 0L
            while (bucket.hasNext()) {
                val b = NetworkStatsManager.UsageBucket()
                bucket.getNextBucket(b)
                rx += b.rxBytes
                tx += b.txBytes
            }
            bucket.close()

            AppNetworkUsage(uid, rx, tx)
        } catch (e: Exception) {
            Logger.e("Erreur NetworkMonitor", e)
            null
        }
    }

    /**
     * Compare l'usage actuel au seuil et émet un événement si dépassement.
     */
    fun checkThreshold(
        uid: Int,
        sinceMs: Long,
        untilMs: Long,
        packageName: String?,
        onEvent: (SecurityEvent) -> Unit
    ) {
        val usage = getUsage(uid, sinceMs, untilMs) ?: return
        val mb = usage.totalBytes / (1024 * 1024)

        if (mb >= Constants.NETWORK_USAGE_THRESHOLD_MB) {
            onEvent(
                SecurityEvent(
                    type = SecurityEvent.EventType.NETWORK_ANOMALY,
                    severity = SecurityEvent.Severity.MEDIUM,
                    title = "Consommation réseau élevée",
                    description = "$packageName a utilisé $mb Mo",
                    packageName = packageName
                )
            )
        }
    }
}