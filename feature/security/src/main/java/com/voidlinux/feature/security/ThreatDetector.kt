package com.voidlinux.feature.security

import android.content.Context
import com.voidlinux.core.common.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * Agrège tous les moniteurs et émet un flux d'événements de sécurité.
 */
class ThreatDetector(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val networkMonitor = NetworkMonitor(context)
    private val batteryMonitor = BatteryMonitor(context)
    private val processMonitor = ProcessMonitor(context)

    private val _events = MutableSharedFlow<SecurityEvent>(replay = 50)
    val events: SharedFlow<SecurityEvent> = _events

    private var guard: FileGuard? = null

    fun configureWatch(directory: File) {
        guard?.stop()
        guard = FileGuard(context, directory) { event ->
            emit(event)
        }
    }

    fun startMonitoring() {
        guard?.start()
        Logger.d("ThreatDetector démarré")
    }

    fun stopMonitoring() {
        guard?.stop()
        Logger.d("ThreatDetector arrêté")
    }

    fun scanBattery() {
        batteryMonitor.detectDrain { emit(it) }
    }

    fun scanProcesses() {
        processMonitor.detectSuspicious(ProcessMonitor.DEFAULT_KNOWN) { emit(it) }
    }

    fun scanNetwork(uid: Int, since: Long, until: Long, pkg: String?) {
        networkMonitor.checkThreshold(uid, since, until, pkg) { emit(it) }
    }

    private fun emit(event: SecurityEvent) {
        scope.launch {
            _events.emit(event)
        }
    }
}