package com.voidlinux.feature.security

import android.content.Context
import android.os.BatteryManager
import com.voidlinux.core.common.Constants

/**
 * Surveille la batterie et détecte les drains anormaux.
 * Un drain > seuil entre deux mesures = événement.
 */
class BatteryMonitor(private val context: Context) {

    private val bm: BatteryManager =
        context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    private var lastLevel: Int = -1
    private var lastTimestamp: Long = 0L

    data class BatterySnapshot(
        val level: Int,
        val timestamp: Long,
        val charging: Boolean,
        val temperature: Float
    )

    fun snapshot(): BatterySnapshot {
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        val intent = context.registerReceiver(
            null,
            android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
        )

        val charging = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            ?.let {
                it == BatteryManager.BATTERY_STATUS_CHARGING ||
                it == BatteryManager.BATTERY_STATUS_FULL
            } ?: false

        val temp = (intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f

        return BatterySnapshot(
            level = level,
            timestamp = System.currentTimeMillis(),
            charging = charging,
            temperature = temp
        )
    }

    /**
     * Compare le dernier snapshot au précédent et émet un événement
     * si la batterie descend trop vite.
     */
    fun detectDrain(onEvent: (SecurityEvent) -> Unit) {
        val snap = snapshot()
        if (lastLevel < 0) {
            lastLevel = snap.level
            lastTimestamp = snap.timestamp
            return
        }

        val deltaLevel = lastLevel - snap.level
        val deltaTimeMin = (snap.timestamp - lastTimestamp) / 60_000.0

        if (!snap.charging && deltaTimeMin > 1.0) {
            val perMinute = deltaLevel / deltaTimeMin
            if (perMinute >= Constants.BATTERY_DRAIN_THRESHOLD / 10.0) {
                onEvent(
                    SecurityEvent(
                        type = SecurityEvent.EventType.BATTERY_DRAIN,
                        severity = SecurityEvent.Severity.MEDIUM,
                        title = "Drain de batterie anormal",
                        description = "%.1f %%/min (temp %.1f°C)".format(
                            perMinute, snap.temperature
                        )
                    )
                )
            }
        }

        lastLevel = snap.level
        lastTimestamp = snap.timestamp
    }
}