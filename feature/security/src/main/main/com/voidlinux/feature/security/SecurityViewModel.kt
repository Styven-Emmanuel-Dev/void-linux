package com.voidlinux.feature.security

import android.app.Application
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SecurityUiState(
    val monitoring: Boolean = false,
    val events: List<SecurityEvent> = emptyList(),
    val statusMessage: String = "Surveillance inactive",
    val errorMessage: String? = null
)

class SecurityViewModel(app: Application) : AndroidViewModel(app) {

    private val detector = ThreatDetector(app)
    private val notifier = SecurityNotifier(app)

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState

    init {
        val downloads = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        detector.configureWatch(downloads)

        viewModelScope.launch {
            detector.events.collect { event ->
                _uiState.value = _uiState.value.copy(
                    events = listOf(event) + _uiState.value.events.take(49)
                )
                notifier.notify(event)
            }
        }
    }

    fun toggleMonitoring() {
        if (_uiState.value.monitoring) {
            detector.stopMonitoring()
            _uiState.value = _uiState.value.copy(
                monitoring = false,
                statusMessage = "Surveillance inactive"
            )
        } else {
            detector.startMonitoring()
            _uiState.value = _uiState.value.copy(
                monitoring = true,
                statusMessage = "Surveillance active"
            )
        }
    }

    fun runManualScan() {
        viewModelScope.launch {
            detector.scanBattery()
            detector.scanProcesses()
        }
    }

    fun clearEvents() {
        _uiState.value = _uiState.value.copy(events = emptyList())
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}