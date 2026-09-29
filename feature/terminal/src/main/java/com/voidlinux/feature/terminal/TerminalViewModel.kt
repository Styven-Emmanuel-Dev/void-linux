package com.voidlinux.feature.terminal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.voidlinux.feature.linux.LinuxRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class TerminalUiState(
    val linuxReady: Boolean = false,
    val sessionActive: Boolean = false,
    val cols: Int = 80,
    val rows: Int = 24,
    val errorMessage: String? = null
)

class TerminalViewModel(app: Application) : AndroidViewModel(app) {

    private val linuxRepo = LinuxRepository(app)

    private val _uiState = MutableStateFlow(TerminalUiState())
    val uiState: StateFlow<TerminalUiState> = _uiState

    private var session: TerminalSession? = null

    fun checkLinuxReady() {
        val ready = linuxRepo.isInstalled()
        _uiState.value = _uiState.value.copy(linuxReady = ready)
    }

    fun startSession(
        buffer: TerminalBuffer,
        onOutput: (String) -> Unit
    ) {
        if (!linuxRepo.isInstalled()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Kali Linux n'est pas installé"
            )
            return
        }

        session?.stop()
        session = TerminalSession(getApplication(), buffer, onOutput).also {
            it.start("/bin/bash")
            _uiState.value = _uiState.value.copy(sessionActive = true)
        }
    }

    fun writeInput(data: String) {
        session?.write(data)
    }

    fun resize(cols: Int, rows: Int) {
        session?.resize(cols, rows)
        _uiState.value = _uiState.value.copy(cols = cols, rows = rows)
    }

    fun stopSession() {
        session?.stop()
        session = null
        _uiState.value = _uiState.value.copy(sessionActive = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        stopSession()
    }
}