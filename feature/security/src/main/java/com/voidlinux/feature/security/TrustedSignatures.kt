package com.voidlinux.feature.security

/** Liste locale des certificats SHA-256 explicitement approuvés. */
object TrustedSignatures {

    /**
     * À remplir par le mainteneur avec les certificats réellement vérifiés.
     * Une signature absente de cette liste est simplement "inconnue", pas "malveillante".
     */
    val ALL: Set<String> = emptySet()

    private val userTrusted = mutableSetOf<String>()

    fun isTrusted(sha256: String): Boolean {
        val normalized = sha256.lowercase()
        return ALL.any { it.equals(normalized, ignoreCase = true) } || normalized in userTrusted
    }

    @Synchronized
    fun trust(sha256: String) {
        userTrusted.add(sha256.lowercase())
    }

    @Synchronized
    fun untrust(sha256: String) {
        userTrusted.remove(sha256.lowercase())
    }

    @Synchronized
    fun listUserTrusted(): Set<String> = userTrusted.toSet()
}
