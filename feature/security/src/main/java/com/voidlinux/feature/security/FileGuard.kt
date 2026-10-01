package com.voidlinux.feature.security

import android.content.Context
import android.os.FileObserver
import com.voidlinux.core.common.Logger
import java.io.File

/**
 * Surveille un répertoire et signale tout fichier suspect :
 * - APK nouvellement créé
 * - scripts exécutables (.sh, .bin, .dex, .so)
 * - archives inattendues
 */
class FileGuard(
    private val context: Context,
    private val watchDir: File,
    private val onEvent: (SecurityEvent) -> Unit
) {

    private var observer: FileObserver? = null
    private val scanner = ApkScanner(context)

    fun start() {
        if (!watchDir.exists()) {
            watchDir.mkdirs()
        }

        observer = object : FileObserver(
            watchDir,
            CREATE or MOVED_TO or CLOSE_WRITE
        ) {
            override fun onEvent(event: Int, path: String?) {
                path ?: return
                val file = File(watchDir, path)
                if (!file.isFile) return
                handleFile(file)
            }
        }.also { it.startWatching() }

        Logger.d("FileGuard démarré sur ${watchDir.absolutePath}")
    }

    fun stop() {
        observer?.stopWatching()
        observer = null
        Logger.d("FileGuard arrêté")
    }

    private fun handleFile(file: File) {
        val ext = file.extension.lowercase()

        when (ext) {
            "apk" -> handleApk(file)
            "sh", "bin", "dex", "so" -> handleExecutable(file)
            "zip", "tar", "gz", "xz" -> handleArchive(file)
        }
    }

    private fun handleApk(file: File) {
        val info = scanner.scan(file) ?: run {
            onEvent(
                SecurityEvent(
                    type = SecurityEvent.EventType.APK_DETECTED,
                    severity = SecurityEvent.Severity.HIGH,
                    title = "APK illisible",
                    description = "Le fichier ${file.name} n'a pas pu être analysé",
                    filePath = file.absolutePath
                )
            )
            return
        }

        val sensitive = scanner.findSensitivePermissions(info.permissions)

        if (!info.trusted) {
            onEvent(
                SecurityEvent(
                    type = SecurityEvent.EventType.APK_UNTRUSTED,
                    severity = if (sensitive.isNotEmpty())
                        SecurityEvent.Severity.CRITICAL
                    else SecurityEvent.Severity.HIGH,
                    title = "APK non signé de confiance",
                    description = buildString {
                        append("${file.name}\n")
                        append("Package : ${info.packageName}\n")
                        if (sensitive.isNotEmpty()) {
                            append("⚠ Permissions sensibles : ")
                            append(sensitive.joinToString { it.substringAfterLast('.') })
                        }
                    },
                    packageName = info.packageName,
                    filePath = file.absolutePath
                )
            )
        }
    }

    private fun handleExecutable(file: File) {
        onEvent(
            SecurityEvent(
                type = SecurityEvent.EventType.FILE_SUSPICIOUS,
                severity = SecurityEvent.Severity.MEDIUM,
                title = "Fichier exécutable détecté",
                description = "${file.name} (${file.length()} octets)",
                filePath = file.absolutePath
            )
        )
    }

    private fun handleArchive(file: File) {
        onEvent(
            SecurityEvent(
                type = SecurityEvent.EventType.FILE_SUSPICIOUS,
                severity = SecurityEvent.Severity.LOW,
                title = "Archive détectée",
                description = "${file.name} (${file.length()} octets)",
                filePath = file.absolutePath
            )
        )
    }
}