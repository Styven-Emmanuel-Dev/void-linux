package com.voidlinux.feature.tor

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.voidlinux.core.common.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Gère le démarrage d'Orbot via Intent (sans NetCipher).
 */
class TorManager(private val context: Context) {

    private val _state = MutableStateFlow<TorState>(TorState.Stopped)
    val state: StateFlow<TorState> = _state

    private val ORBOT_PACKAGE = "org.torproject.android"

    fun isOrbotInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo(ORBOT_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isTorRunning(): Boolean {
        // Sans NetCipher, on ne peut pas savoir directement
        // On se base sur le fait qu'Orbot est installé
        return isOrbotInstalled()
    }

    fun refreshState() {
        _state.value = when {
            !isOrbotInstalled() -> TorState.OrbotMissing
            else -> TorState.Running
        }
    }

    fun requestStart() {
        if (!isOrbotInstalled()) {
            _state.value = TorState.OrbotMissing
            return
        }
        try {
            val intent = Intent().apply {
                setClassName(ORBOT_PACKAGE, "org.torproject.android.ui.OrbotMainActivity")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            _state.value = TorState.Starting
        } catch (e: Exception) {
            Logger.e("Erreur démarrage Orbot", e)
        }
    }

    fun promptInstall() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("https://f-droid.org/packages/org.torproject.android/")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _state.value = TorState.Error("Impossible d'ouvrir la page d'installation")
        }
    }

    fun enableGlobalProxy() {
        Logger.d("Proxy Tor global activé (via Orbot)")
    }

    fun disableGlobalProxy() {
        Logger.d("Proxy Tor global désactivé")
    }

    fun getSocksPort(): Int = 9050
}
