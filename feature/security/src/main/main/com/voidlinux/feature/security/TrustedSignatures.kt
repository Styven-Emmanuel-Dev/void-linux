package com.voidlinux.feature.security

/**
 * Liste blanche des signatures d'APK de confiance.
 * Ajoute ici les SHA-256 des signatures que tu considères comme sûres.
 */
object TrustedSignatures {

    /** Signatures par défaut (vides = tout est suspect) */
    val ALL: Set<String> = emptySet()

    /** Ajout dynamique par l'utilisateur */
    private val userTrusted = mutableSetOf<String>()

    fun isTrusted(sha256: String): Boolean =
        ALL.contains(sha256) || userTrusted.contains(sha256)

    fun trust(sha256: String) {
        userTrusted.add(sha256)
    }

    fun untrust(sha256: String) {
        userTrusted.remove(sha256)
    }

    fun listUserTrusted(): Set<String> = userTrusted.toSet()
}