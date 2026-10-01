package com.voidlinux.feature.security

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import com.voidlinux.core.common.Constants
import com.voidlinux.core.common.Logger

/**
 * Mesure la consommation réseau connue par Android pour un UID.
 *
 * Cette classe ne prétend pas identifier un malware : un volume élevé peut être
 * parfaitement légitime (vidéo, téléchargement, synchronisation, etc.).
 */
class NetworkMonitor(private val context: Context) {

    private val nsm: NetworkStatsManager? =
        context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager

    data class AppNetworkUsage(
        val uid: Int,
        val rxBytes: Long,
        val txBytes: Long,
        val startMs: Long,
        val endMs: Long
    ) {
        val totalBytes: Long get() = rxBytes + txBytes
        val totalMegabytes: Long get() = totalBytes / (1024L * 1024L)
    }

    fun getUsage(uid: Int, sinceMs: Long, untilMs: Long): AppNetworkUsage? {
        if (sinceMs >= untilMs || uid < 0) return null
        val manager = nsm ?: return null

        return try {
            var rx = 0L
            var tx = 0L
            readTransport(manager, ConnectivityManager.TYPE_WIFI, uid, sinceMs, untilMs) { r, t ->
                rx += r
                tx += t
            }
            readTransport(manager, ConnectivityManager.TYPE_MOBILE, uid, sinceMs, untilMs) { r, t ->
                rx += r
                tx += t
            }
            AppNetworkUsage(uid, rx, tx, sinceMs, untilMs)
        } catch (e: SecurityException) {
            Logger.e("Accès aux statistiques réseau refusé : accord PACKAGE_USAGE_STATS requis", e)
            null
        } catch (e: Exception) {
            Logger.e("Erreur NetworkMonitor", e)
            null
        }
    }

    private fun readTransport(
        manager: NetworkStatsManager,
        transport: Int,
        uid: Int,
        sinceMs: Long,
        untilMs: Long,
        onBucket: (rx: Long, tx: Long) -> Unit
    ) {
        var stats: NetworkStats? = null
        try {
            stats = manager.queryDetailsForUid(transport, null, sinceMs, untilMs, uid)
            val bucket = NetworkStats.Bucket()
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)
                onBucket(bucket.rxBytes, bucket.txBytes)
            }
        } catch (e: SecurityException) {
            throw e
        } catch (e: IllegalArgumentException) {
            // Certains appareils ne fournissent pas de statistiques pour un transport.
            Logger.d("Statistiques indisponibles pour le transport $transport: ${e.message}")
        } finally {
            stats?.close()
        }
    }

    /**
     * Signale uniquement un volume élevé. Ce n'est pas une preuve d'attaque.
     */
    fun checkThreshold(
        uid: Int,
        sinceMs: Long,
        untilMs: Long,
        packageName: String?,
        onEvent: (SecurityEvent) -> Unit
    ) {
        val usage = getUsage(uid, sinceMs, untilMs) ?: return
        val mb = usage.totalMegabytes

        if (mb >= Constants.NETWORK_USAGE_THRESHOLD_MB) {
            onEvent(
                SecurityEvent(
                    type = SecurityEvent.EventType.NETWORK_ANOMALY,
                    severity = SecurityEvent.Severity.LOW,
                    title = "Usage réseau élevé",
                    description = buildString {
                        append(packageName ?: "Application inconnue")
                        append(" a utilisé environ $mb Mo sur la période analysée. ")
                        append("Un volume élevé n'est pas, à lui seul, une preuve de menace.")
                    },
                    packageName = packageName
                )
            )
        }
    }
}
