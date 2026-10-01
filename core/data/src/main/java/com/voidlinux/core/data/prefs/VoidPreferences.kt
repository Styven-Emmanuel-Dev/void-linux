package com.voidlinux.core.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Préférences persistantes de Void-Linux.
 */
class VoidPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("void_prefs", Context.MODE_PRIVATE)

    var selectedDistro: String
        get() = prefs.getString(KEY_DISTRO, "kali") ?: "kali"
        set(value) = prefs.edit { putString(KEY_DISTRO, value) }

    var torEnabled: Boolean
        get() = prefs.getBoolean(KEY_TOR, false)
        set(value) = prefs.edit { putBoolean(KEY_TOR, value) }

    var securityMonitoring: Boolean
        get() = prefs.getBoolean(KEY_SECURITY, false)
        set(value) = prefs.edit { putBoolean(KEY_SECURITY, value) }

    var mockLocationEnabled: Boolean
        get() = prefs.getBoolean(KEY_MOCK, false)
        set(value) = prefs.edit { putBoolean(KEY_MOCK, value) }

    var lastMockPreset: String
        get() = prefs.getString(KEY_MOCK_PRESET, "Paris") ?: "Paris"
        set(value) = prefs.edit { putString(KEY_MOCK_PRESET, value) }

    var firstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit { putBoolean(KEY_FIRST_LAUNCH, value) }

    var debugMode: Boolean
        get() = prefs.getBoolean(KEY_DEBUG, false)
        set(value) = prefs.edit { putBoolean(KEY_DEBUG, value) }

    fun clearAll() {
        prefs.edit { clear() }
    }

    companion object {
        private const val KEY_DISTRO = "selected_distro"
        private const val KEY_TOR = "tor_enabled"
        private const val KEY_SECURITY = "security_monitoring"
        private const val KEY_MOCK = "mock_location"
        private const val KEY_MOCK_PRESET = "mock_preset"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_DEBUG = "debug_mode"
    }
}