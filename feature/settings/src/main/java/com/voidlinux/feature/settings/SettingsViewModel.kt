package com.voidlinux.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voidlinux.core.common.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val permissions: List<PermissionManager.PermissionStatus> = emptyList(),
    val steps: List<HardeningGuide.HardeningStep> = emptyList(),
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val permissions = PermissionManager(app)
    private val guide = HardeningGuide(app)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val perms = permissions.listCriticalPermissions()
            val steps = guide.getSteps()
            _uiState.value = _uiState.value.copy(
                permissions = perms,
                steps = steps
            )
            Logger.d("Settings rafraîchis")
        }
    }

    fun runStep(step: HardeningGuide.HardeningStep) {
        try {
            step.action.invoke()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Impossible d'ouvrir ${step.title}"
            )
        }
    }

    fun openAllPermissions() {
        guide.openAppSettings()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            infoMessage = null
        )
    }
}