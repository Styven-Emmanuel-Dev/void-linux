package com.voidlinux.feature.security

import android.content.Context
import android.content.pm.PackageManager
import com.voidlinux.core.common.Logger
import java.io.File
import java.security.MessageDigest

/**
 * Analyse un APK sans l'installer.
 *
 * Important : une signature inconnue n'est pas synonyme de malware. Elle signifie
 * simplement qu'elle ne figure pas dans la liste de confiance locale.
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
        if (!apk.isFile || !apk.canRead()) return null

        val info = try {
            pm.getPackageArchiveInfo(
                apk.absolutePath,
                PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_PERMISSIONS
            )
        } catch (e: Exception) {
            Logger.e("Impossible d'analyser l'APK", e)
            return null
        } ?: return null

        // Depuis API 28, GET_SIGNATURES est déprécié. API min = 29 pour ce module.
        val signers = info.signingInfo?.apkContentsSigners.orEmpty()
        val hashes = signers.map { certificate ->
            sha256(certificate.toByteArray())
        }
        val primaryHash = hashes.firstOrNull()
        val trusted = primaryHash?.let(TrustedSignatures::isTrusted) == true

        return ApkInfo(
            packageName = info.packageName,
            versionName = info.versionName,
            signatureSha256 = primaryHash,
            permissions = info.requestedPermissions?.toList().orEmpty(),
            trusted = trusted
        )
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }

    /** Liste les permissions sensibles à présenter à l'utilisateur. */
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
