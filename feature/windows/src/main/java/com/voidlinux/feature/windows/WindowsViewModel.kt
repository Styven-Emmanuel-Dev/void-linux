package com.voidlinux.feature.windows

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voidlinux.core.common.VoidResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

data class WindowsUiState(
    val wineReady: Boolean = false,
    val box64Available: Boolean = false,
    val architecture: String = "unknown",
    val running: Boolean = false,
    val output: String = "",
    val statusMessage: String = "",
    val errorMessage: String? = null
)

class WindowsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = WindowsRepository(app)

    private val _uiState = MutableStateFlow(WindowsUiState())
    val uiState: StateFlow<WindowsUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(
            wineReady = repo.isWineReady(),
            box64Available = repo.isBox64Available(),
            architecture = repo.getArchitecture(),
            statusMessage = when {
                !repo.isBox64Available() -> "Box64 non disponible"
                repo.isWineReady() -> "Wine prêt"
                else -> "Wine non initialisé"
            }
        )
    }

    fun initializeWine() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(statusMessage = "Initialisation de Wine…")

            val result = repo.initializeWine()

            _uiState.value = when (result) {
                is VoidResult.Success -> _uiState.value.copy(
                    wineReady = true,
                    statusMessage = "Wine prêt ✅"
                )
                is VoidResult.Error -> _uiState.value.copy(
                    errorMessage = result.message,
                    statusMessage = "Échec de l'initialisation"
                )
                else -> _uiState.value
            }
        }
    }

    fun runExe(file: File, args: List<String> = emptyList()) {
        if (_uiState.value.running) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                running = true,
                output = "",
                statusMessage = "Exécution de ${file.name}…"
            )

            val result = repo.runExe(
                exeFile = file,
                args = args,
                onStdout = { line ->
                    _uiState.value = _uiState.value.copy(
                        output = _uiState.value.output + line + "\n"
                    )
                },
                onStderr = { line ->
                    _uiState.value = _uiState.value.copy(
                        output = _uiState.value.output + "[err] " + line + "\n"
                    )
                }
            )

            _uiState.value = when (result) {
                is VoidResult.Success -> _uiState.value.copy(
                    running = false,
                    statusMessage = "Terminé (code ${result.data.exitCode})"
                )
                is VoidResult.Error -> _uiState.value.copy(
                    running = false,
                    errorMessage = result.message,
                    statusMessage = "Échec de l'exécution"
                )
                else -> _uiState.value.copy(running = false)
            }
        }
    }

    fun stopExecution() {
        repo.stop()
        _uiState.value = _uiState.value.copy(
            running = false,
            statusMessage = "Exécution arrêtée"
        )
    }

    fun cleanWine() {
        repo.cleanWine()
        refresh()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}