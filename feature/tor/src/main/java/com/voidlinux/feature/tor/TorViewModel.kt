package com.voidlinux.feature.tor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TorUiState(
    val orbotInstalled: Boolean = false,
    val torRunning: Boolean = false,
    val globalProxyEnabled: Boolean = false,
    val statusMessage: String = "",
    val errorMessage: String? = null,
    val pendingUrl: String? = null
)

class TorViewModel(app: Application) : AndroidViewModel(app) {

    private val manager = TorManager(app)

    private val _uiState = MutableStateFlow(TorUiState())
    val uiState: StateFlow<TorUiState> = _uiState

    init {
        manager.registerReceiver()
        refresh()
    }

    fun refresh() {
        val installed = manager.isOrbotInstalled()
        val running = manager.isTorRunning()

        _uiState.value = _uiState.value.copy(
            orbotInstalled = installed,
            torRunning = running,
            statusMessage = when {
                !installed -> "Orbot n'est pas installé"
                running -> "Tor actif ✅"
                else -> "Tor arrêté"
            },
            errorMessage = null
        )
    }

    fun startTor() {
        if (!manager.isOrbotInstalled()) {
            manager.promptInstall()
            return
        }
        manager.requestStart()
        manager.enableGlobalProxy()

        viewModelScope.launch {
            kotlinx.coroutines.delay(1500)
            refresh()
            _uiState.value = _uiState.value.copy(globalProxyEnabled = true)
        }
    }

    fun stopTor() {
        manager.disableGlobalProxy()
        _uiState.value = _uiState.value.copy(
            globalProxyEnabled = false,
            statusMessage = "Proxy Tor désactivé"
        )
    }

    fun openOnion(url: String) {
        if (!manager.isTorRunning()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Tor doit être actif pour charger un .onion"
            )
            return
        }
        _uiState.value = _uiState.value.copy(pendingUrl = url)
    }

    fun clearPendingUrl() {
        _uiState.value = _uiState.value.copy(pendingUrl = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        manager.unregisterReceiver()
    }
}