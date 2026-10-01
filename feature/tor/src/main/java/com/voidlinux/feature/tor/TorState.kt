package com.voidlinux.feature.tor

sealed class TorState {

    /** Orbot n'est pas installé sur l'appareil */
    object OrbotMissing : TorState()

    /** Orbot est installé mais Tor est arrêté */
    object Stopped : TorState()

    /** Tor est en cours de démarrage */
    object Starting : TorState()

    /** Tor est actif et prêt */
    object Running : TorState()

    /** Une erreur est survenue */
    data class Error(val message: String) : TorState()
}