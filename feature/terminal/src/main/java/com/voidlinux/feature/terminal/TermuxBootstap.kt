package com.voidlinux.feature.terminal

import android.content.Context
import java.io.File
import java.io.FileOutputStream

/**
 * Gère l'installation du bootstrap Termux depuis les assets de l'APK.
 * Le bootstrap est un zip contenant un mini-rootfs avec sh, apt, dpkg, etc.
 */
class TermuxBootstrap(private val context: Context) {

    private val bootstrapDir: File by lazy {
        File(context.filesDir, "termux-bootstrap").apply { mkdirs() }
    }

    /**
     * Extrait le bootstrap depuis les assets vers le répertoire privé.
     * @param abi architecture cible ("aarch64", "arm", "x86_64")
     */
    fun install(abi: String = "aarch64"): Boolean {
        if (isInstalled()) return true

        val assetName = "bootstrap-$abi.zip"
        return try {
            context.assets.open(assetName).use { input ->
                val zipFile = File(context.cacheDir, assetName)
                FileOutputStream(zipFile).use { output ->
                    input.copyTo(output, bufferSize = 64 * 1024)
                }

                unzip(zipFile, bootstrapDir)
                zipFile.delete()

                // Rendre les binaires exécutables
                makeExecutable(File(bootstrapDir, "bin"))
                makeExecutable(File(bootstrapDir, "lib"))

                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun unzip(zip: File, target: File) {
        java.util.zip.ZipInputStream(zip.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val outFile = File(target, entry.name)
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { fos ->
                        zis.copyTo(fos, bufferSize = 32 * 1024)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    private fun makeExecutable(dir: File) {
        if (!dir.exists()) return
        dir.listFiles()?.forEach { file ->
            if (file.isFile) {
                file.setExecutable(true, false)
            } else if (file.isDirectory) {
                makeExecutable(file)
            }
        }
    }

    fun isInstalled(): Boolean =
        File(bootstrapDir, "bin/sh").exists()

    fun getBinPath(): String = File(bootstrapDir, "bin").absolutePath

    fun getPrefixDir(): File = bootstrapDir
}