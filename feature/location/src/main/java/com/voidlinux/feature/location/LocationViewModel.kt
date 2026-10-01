package com.voidlinux.feature.location

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class LocationUiState(
    val active: Boolean = false,
    val currentLat: Double = 0.0,
    val currentLon: Double = 0.0,
    val presetName: String = "Paris",
    val gpsEnabled: Boolean = false,
    val networkEnabled: Boolean = false,
    val deviceRooted: Boolean = false,
    val statusMessage: String = "Fausse position inactive",
    val errorMessage: String? = null
)

class LocationViewModel(app: Application) : AndroidViewModel(app) {

    private val provider = MockLocationProvider(app)
    private val shield = LocationShield(app)

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        val status = shield.getStatus()
        val rooted = shield.isDeviceRooted()
        val active = provider.isActive()
        val loc = provider.getCurrentLocation()

        _uiState.value = _uiState.value.copy(
            active = active,
            gpsEnabled = status.gpsEnabled,
            networkEnabled = status.networkEnabled,
            deviceRooted = rooted,
            currentLat = loc?.latitude ?: 0.0,
            currentLon = loc?.longitude ?: 0.0,
            statusMessage = if (active) "Fausse position active" else "Fausse position inactive"
        )
    }

    fun toggleMock() {
        if (provider.isActive()) {
            provider.disable()
            _uiState.value = _uiState.value.copy(
                active = false,
                statusMessage = "Fausse position inactive"
            )
        } else {
            val ok = provider.enable()
            if (!ok) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Active d'abord les options développeur et sélectionne Void-Linux comme app de position simulée"
                )
                return
            }
            applyPreset(_uiState.value.presetName)
            _uiState.value = _uiState.value.copy(
                active = true,
                statusMessage = "Fausse position active"
            )
        }
    }

    fun applyPreset(name: String) {
        val preset = MockLocationProvider.PRESETS[name] ?: return
        val ok = provider.setLocation(preset)

        _uiState.value = _uiState.value.copy(
            presetName = name,
            currentLat = preset.latitude,
            currentLon = preset.longitude,
            errorMessage = if (ok) null else "Impossible d'appliquer la position"
        )
    }

    fun setCustomLocation(lat: Double, lon: Double) {
        val loc = MockLocationProvider.FakeLocation(lat, lon)
        val ok = provider.setLocation(loc)

        _uiState.value = _uiState.value.copy(
            currentLat = lat,
            currentLon = lon,
            presetName = "Personnalisé",
            errorMessage = if (ok) null else "Impossible d'appliquer la position"
        )
    }

    fun openLocationSettings() = shield.openLocationSettings()
    fun openDeveloperSettings() = shield.openDeveloperSettings()
    fun openMockSettings() = shield.openMockLocationSettings()
    fun openAppDetails() = shield.openAppDetailsSettings()

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}