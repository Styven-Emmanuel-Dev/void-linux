package com.voidlinux.feature.linux

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voidlinux.core.common.Constants
import com.voidlinux.core.common.VoidResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LinuxUiState(
    val distroId: String = Constants.DISTRO_KALI,
    val distroName: String = "Kali Linux",
    val installed: Boolean = false,
    val installing: Boolean = false,
    val progress: Int = 0,
    val statusMessage: String = "",
    val errorMessage: String? = null,
    val nativeReady: Boolean = false
)

class LinuxViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = LinuxRepository(app)

    private val _uiState = MutableStateFlow(LinuxUiState())
    val uiState: StateFlow<LinuxUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        val distroId = _uiState.value.distroId
        val distro = repo.getDistroInfo(distroId)
        val installed = repo.isInstalled(distroId)
        val nativeReady = repo.hasNativeSupport()

        _uiState.value = _uiState.value.copy(
            distroName = distro?.displayName ?: distroId,
            installed = installed,
            nativeReady = nativeReady,
            statusMessage = when {
                !nativeReady -> "Binaires natifs manquants"
                installed -> "${distro?.displayName} prêt"
                else -> "${distro?.displayName} non installé"
            },
            errorMessage = null
        )
    }

    fun selectDistro(distroId: String) {
        _uiState.value = _uiState.value.copy(distroId = distroId)
        refresh()
    }

    fun install() {
        if (_uiState.value.installing) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                installing = true,
                progress = 0,
                errorMessage = null,
                statusMessage = "Installation en cours…"
            )

            val result = repo.installDistro(_uiState.value.distroId) { progress ->
                _uiState.value = _uiState.value.copy(progress = progress)
            }

            _uiState.value = when (result) {
                is VoidResult.Success -> _uiState.value.copy(
                    installing = false,
                    installed = true,
                    progress = 100,
                    statusMessage = "${_uiState.value.distroName} installé ✅"
                )
                is VoidResult.Error -> _uiState.value.copy(
                    installing = false,
                    errorMessage = result.message,
                    statusMessage = "Échec de l'installation"
                )
                else -> _uiState.value.copy(installing = false)
            }
        }
    }

    fun uninstall() {
        viewModelScope.launch {
            val ok = repo.uninstall(_uiState.value.distroId)
            _uiState.value = _uiState.value.copy(
                installed = !ok,
                statusMessage = if (ok) "Désinstallé" else "Échec de la désinstallation"
            )
            refresh()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}