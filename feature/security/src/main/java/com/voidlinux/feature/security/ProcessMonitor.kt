package com.voidlinux.feature.security

import android.app.usage.UsageStatsManager
import android.content.Context
import com.voidlinux.core.common.Logger

/**
 * Android moderne ne permet pas à une application classique d'énumérer librement
 * les processus/services des autres applications. getRunningServices() ne doit
 * donc pas être présenté comme un processus scanner.
 *
 * Cette classe fournit à la place les applications récemment utilisées lorsque
 * l'utilisateur a accordé l'accès aux statistiques d'utilisation.
 */
class ProcessMonitor(private val context: Context) {

    private val usageStats: UsageStatsManager? =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager

    data class RecentAppUsage(
        val packageName: String,
        val lastTimeUsed: Long,
        val totalTimeForegroundMs: Long
    )

    fun getRecentApps(windowMs: Long = 60 * 60 * 1000L): List<RecentAppUsage> {
        val manager = usageStats ?: return emptyList()
        val now = System.currentTimeMillis()
        val begin = now - windowMs.coerceAtLeast(1_000L)

        return try {
            manager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                begin,
                now
            ).orEmpty()
                .filter { it.totalTimeInForeground > 0L }
                .sortedByDescending { it.lastTimeUsed }
                .map {
                    RecentAppUsage(
                        packageName = it.packageName,
                        lastTimeUsed = it.lastTimeUsed,
                        totalTimeForegroundMs = it.totalTimeInForeground
                    )
                }
        } catch (e: SecurityException) {
            Logger.e("Accès aux statistiques d'utilisation refusé", e)
            emptyList()
        } catch (e: Exception) {
            Logger.e("Erreur UsageStats", e)
            emptyList()
        }
    }

    /**
     * Ne génère plus de faux positifs à partir de services Android inconnus.
     * L'appelant peut utiliser cette méthode pour vérifier que l'accès Usage Stats
     * fonctionne et afficher les applications récemment utilisées.
     */
    fun detectSuspicious(
        knownServices: Set<String>,
        onEvent: (SecurityEvent) -> Unit
    ) {
        @Suppress("UNUSED_VARIABLE")
        val ignored = knownServices
        val apps = getRecentApps()
        if (apps.isEmpty()) return

        // Aucun événement de menace n'est créé ici : l'usage récent n'est pas une preuve
        // de comportement malveillant. L'UI peut afficher les applications comme information.
        Logger.d("${apps.size} applications récemment utilisées détectées")
    }

    companion object {
        val DEFAULT_KNOWN: Set<String> = emptySet()
    }
}
