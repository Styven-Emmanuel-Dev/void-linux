package com.voidlinux.feature.windows

import android.content.Context
import com.voidlinux.core.common.Logger
import java.io.File

/**
 * Gère le conteneur Wine : préfixe, DLL, configuration.
 * Le préfixe Wine est le "disque C:" virtuel de Windows.
 */
class WineContainer(private val context: Context) {

    val wineDir: File by lazy {
        File(context.filesDir, "wine").apply { mkdirs() }
    }

    val prefixDir: File by lazy {
        File(wineDir, "prefix").apply { mkdirs() }
    }

    val driveC: File by lazy {
        File(prefixDir, "drive_c").apply { mkdirs() }
    }

    val usersDir: File by lazy {
        File(driveC, "users/void").apply { mkdirs() }
    }

    val tempDir: File by lazy {
        File(wineDir, "tmp").apply { mkdirs() }
    }

    val installedDir: File by lazy {
        File(driveC, "Program Files").apply { mkdirs() }
    }

    /**
     * Vérifie si le préfixe Wine est initialisé.
     */
    fun isInitialized(): Boolean {
        val systemReg = File(prefixDir, "system.reg")
        val userReg = File(prefixDir, "user.reg")
        return systemReg.exists() && userReg.exists()
    }

    /**
     * Initialise la structure du préfixe Wine.
     * wineboot est lancé ensuite pour finaliser.
     */
    fun initializeStructure(): Boolean {
        return try {
            val dirs = listOf(
                driveC,
                usersDir,
                tempDir,
                installedDir,
                File(driveC, "windows"),
                File(driveC, "windows/system32"),
                File(driveC, "Program Files (x86)"),
                File(usersDir, "Desktop"),
                File(usersDir, "Documents"),
                File(usersDir, "Downloads"),
                File(usersDir, "AppData/Roaming"),
                File(usersDir, "AppData/Local")
            )
            dirs.forEach { it.mkdirs() }
            Logger.d("Structure Wine initialisée dans ${prefixDir.absolutePath}")
            true
        } catch (e: Exception) {
            Logger.e("Erreur init Wine", e)
            false
        }
    }

    /**
     * Retourne le chemin Windows-style d'un fichier dans drive_c.
     */
    fun toWindowsPath(file: File): String {
        val relative = file.absolutePath.removePrefix(driveC.absolutePath)
        return "C:" + relative.replace("/", "\\")
    }

    /**
     * Retourne le chemin Unix-style du drive C pour Wine.
     */
    fun getUnixDriveCPath(): String = driveC.absolutePath

    fun getPrefixPath(): String = prefixDir.absolutePath

    fun clean() {
        try {
            wineDir.deleteRecursively()
            wineDir.mkdirs()
            Logger.d("Conteneur Wine nettoyé")
        } catch (e: Exception) {
            Logger.e("Erreur clean Wine", e)
        }
    }
}