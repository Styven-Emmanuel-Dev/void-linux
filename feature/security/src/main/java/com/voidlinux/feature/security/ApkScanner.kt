package com.voidlinux.feature.security

import android.content.Context
import android.content.pm.PackageManager
import com.voidlinux.core.common.Logger
import java.io.File
import java.security.MessageDigest

/**
 * Analyse un APK :
 * - calcule la signature SHA-256
 * - vérifie la liste blanche
 * - détecte les permissions sensibles
 */
class ApkScanner(private val context: Context) {

    private val pm: PackageManager = context.packageManager

    data class ApkInfo(
        val packageName: String?,
        val versionName: String?,
        val signatureSha256: String?,
        val permissions: List<String>,
        val trusted: Boolean
    )

    fun scan(apk: File): ApkInfo? {
        if (!apk.exists()) return null

        val info = pm.getPackageArchiveInfo(
            apk.absolutePath,
            PackageManager.GET_SIGNATURES or PackageManager.GET_PERMISSIONS
        ) ?: return null

        val hash = info.signatures?.firstOrNull()?.let {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.digest(it.toByteArray()).joinToString("") { b -> "%02x".format(b) }
        }

        val perms = info.requestedPermissions?.toList() ?: emptyList()
        val trusted = hash != null && TrustedSignatures.ALL.contains(hash)

        return ApkInfo(
            packageName = info.packageName,
            versionName = info.versionName,
            signatureSha256 = hash,
            permissions = perms,
            trusted = trusted
        )
    }

    /**
     * Liste des permissions considérées comme sensibles.
     */
    fun findSensitivePermissions(perms: List<String>): List<String> =
        perms.filter { it in SENSITIVE_PERMISSIONS }

    companion object {
        private val SENSITIVE_PERMISSIONS = setOf(
            "android.permission.READ_SMS",
            "android.permission.SEND_SMS",
            "android.permission.RECEIVE_SMS",
            "android.permission.READ_CALL_LOG",
            "android.permission.WRITE_CALL_LOG",
            "android.permission.CALL_PHONE",
            "android.permission.RECORD_AUDIO",
            "android.permission.CAMERA",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_BACKGROUND_LOCATION",
            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.SYSTEM_ALERT_WINDOW",
            "android.permission.REQUEST_INSTALL_PACKAGES",
            "android.permission.PACKAGE_USAGE_STATS"
        )
    }
}