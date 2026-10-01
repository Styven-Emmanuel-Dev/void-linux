package com.voidlinux.feature.location

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import com.voidlinux.core.common.Logger

/**
 * Fournit une fausse position GPS à Android.
 * Nécessite que l'utilisateur ait activé les options dev + sélectionné
 * cette app comme fournisseur de position simulée.
 */
class MockLocationProvider(private val context: Context) {

    private val lm: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private var providerName: String = LocationManager.GPS_PROVIDER
    private var active = false
    private var currentLocation: FakeLocation? = null

    data class FakeLocation(
        val latitude: Double,
        val longitude: Double,
        val altitude: Double = 0.0,
        val accuracy: Float = 5f,
        val speed: Float = 0f,
        val bearing: Float = 0f
    )

    fun isMockEnabled(): Boolean {
        return try {
            @Suppress("DEPRECATION")
            val providers = lm.getProviders(true)
            providers.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    fun enable(): Boolean {
        return try {
            lm.addTestProvider(
                providerName,
                false, false, false, false,
                true, true, true,
                PowerManager.PROVIDER_POWER_ON,
                1
            )
            lm.setTestProviderEnabled(providerName, true)
            active = true
            Logger.d("Mock location activé")
            true
        } catch (e: SecurityException) {
            Logger.e("Mock location refusé — options dev requises", e)
            false
        } catch (e: Exception) {
            Logger.e("Erreur enable mock", e)
            false
        }
    }

    fun disable(): Boolean {
        return try {
            if (active) {
                lm.setTestProviderEnabled(providerName, false)
                lm.removeTestProvider(providerName)
                active = false
                Logger.d("Mock location désactivé")
            }
            true
        } catch (e: Exception) {
            Logger.e("Erreur disable mock", e)
            false
        }
    }

    fun setLocation(location: FakeLocation): Boolean {
        if (!active) return false
        return try {
            val loc = Location(providerName).apply {
                latitude = location.latitude
                longitude = location.longitude
                altitude = location.altitude
                accuracy = location.accuracy
                speed = location.speed
                bearing = location.bearing
                time = System.currentTimeMillis()
                elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    verticalAccuracyMeters = 1f
                    speedAccuracyMetersPerSecond = 1f
                    bearingAccuracyDegrees = 1f
                }
            }
            lm.setTestProviderLocation(providerName, loc)
            currentLocation = location
            true
        } catch (e: Exception) {
            Logger.e("Erreur setLocation", e)
            false
        }
    }

    fun isActive(): Boolean = active

    fun getCurrentLocation(): FakeLocation? = currentLocation

    companion object {
        /** Positions prédéfinies utiles */
        val PRESETS = mapOf(
            "Paris" to FakeLocation(48.8566, 2.3522),
            "Lyon" to FakeLocation(45.7640, 4.8357),
            "Marseille" to FakeLocation(43.2965, 5.3698),
            "Toulouse" to FakeLocation(43.6047, 1.4442),
            "Bruxelles" to FakeLocation(50.8503, 4.3517),
            "Genève" to FakeLocation(46.2044, 6.1432),
            "Montréal" to FakeLocation(45.5017, -73.5673),
            "New York" to FakeLocation(40.7128, -74.0060),
            "Tokyo" to FakeLocation(35.6762, 139.6503),
            "Moscou" to FakeLocation(55.7558, 37.6173)
        )
    }
}