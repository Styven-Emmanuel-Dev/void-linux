package com.voidlinux.feature.security

/**
 * Représente un événement de sécurité détecté.
 */
data class SecurityEvent(
    val id: Long = System.currentTimeMillis(),
    val type: EventType,
    val severity: Severity,
    val title: String,
    val description: String,
    val packageName: String? = null,
    val filePath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class EventType {
        APK_DETECTED,
        APK_UNTRUSTED,
        NETWORK_ANOMALY,
        BATTERY_DRAIN,
        PROCESS_SUSPICIOUS,
        PERMISSION_ABUSE,
        FILE_SUSPICIOUS,
        SYSTEM_TAMPER
    }

    enum class Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}