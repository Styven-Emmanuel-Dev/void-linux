package com.voidlinux.feature.tor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.voidlinux.core.common.Logger
import info.guardianproject.netcipher.NetCipher
import info.guardianproject.netcipher.proxy.OrbotHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Gère le cycle de vie d'Orbot et l'activation du proxy Tor global.
 *
 * - Détection d'Orbot
 * - Démarrage automatique
 * - Activation du proxy SOCKS pour HttpURLConnection
 * - Surveillance du statut via broadcasts
 */
class TorManager(private val context: Context) {

    private val orbotHelper: OrbotHelper by lazy {
        OrbotHelper.get(context)
    }

    private val _state = MutableStateFlow<TorState>(TorState.Stopped)
    val state: StateFlow<TorState> = _state

    private var receiverRegistered = false

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                OrbotHelper.ACTION_STATUS -> {
                    val status = intent.getStringExtra(OrbotHelper.EXTRA_STATUS)
                    Logger.d("Orbot status: $status")
                    when (status) {
                        OrbotHelper.STATUS_ON -> _state.value = TorState.Running
                        OrbotHelper.STATUS_OFF -> _state.value = TorState.Stopped
                        OrbotHelper.STATUS_STARTING -> _state.value = TorState.Starting
                        OrbotHelper.STATUS_STOPPING -> _state.value = TorState.Stopped
                    }
                }
            }
        }
    }

    /** Vérifie si Orbot est installé */
    fun isOrbotInstalled(): Boolean =
        orbotHelper.isOrbotInstalled

    /** Vérifie si Tor tourne actuellement */
    fun isTorRunning(): Boolean =
        orbotHelper.isOrbotRunning

    /** Rafraîchit l'état depuis Orbot */
    fun refreshState() {
        _state.value = when {
            !isOrbotInstalled() -> TorState.OrbotMissing
            isTorRunning() -> TorState.Running
            else -> TorState.Stopped
        }
    }

    /** Demande à Orbot de démarrer Tor */
    fun requestStart() {
        if (!isOrbotInstalled()) {
            _state.value = TorState.OrbotMissing
            return
        }
        _state.value = TorState.Starting
        orbotHelper.requestStartTor(context)
    }

    /** Propose d'installer Orbot (ouvre F-Droid ou Play Store) */
    fun promptInstall() {
        try {
            orbotHelper.promptToInstall(context)
        } catch (e: Exception) {
            _state.value = TorState.Error("Impossible d'ouvrir la page d'installation")
        }
    }

    /** Active le proxy Tor pour toutes les HttpURLConnection */
    fun enableGlobalProxy() {
        try {
            NetCipher.useTor()
            NetCipher.useGlobalProxy()
            Logger.d("Proxy Tor global activé")
        } catch (e: Exception) {
            Logger.e("Erreur activation proxy Tor", e)
            _state.value = TorState.Error("Échec activation proxy : ${e.message}")
        }
    }

    /** Désactive le proxy global */
    fun disableGlobalProxy() {
        try {
            NetCipher.disableGlobalProxy()
            Logger.d("Proxy Tor global désactivé")
        } catch (e: Exception) {
            Logger.e("Erreur désactivation proxy", e)
        }
    }

    /** Enregistre le receiver de statut Orbot */
    fun registerReceiver() {
        if (receiverRegistered) return
        val filter = IntentFilter(OrbotHelper.ACTION_STATUS)
        ContextCompat.registerReceiver(
            context,
            statusReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        receiverRegistered = true
    }

    /** Désenregistre le receiver */
    fun unregisterReceiver() {
        if (!receiverRegistered) return
        try {
            context.unregisterReceiver(statusReceiver)
        } catch (_: Exception) { }
        receiverRegistered = false
    }

    /** Retourne le port SOCKS actuel */
    fun getSocksPort(): Int = orbotHelper.orchimenabledport()
}