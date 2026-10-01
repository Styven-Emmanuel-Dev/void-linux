package com.voidlinux.feature.linux

import android.content.Context
import com.voidlinux.core.common.Constants
import io.oonid.proot.engine.ProotHost
import java.io.File

/**
 * Implémentation de ProotHost pour Void-Linux.
 * Fournit tous les chemins nécessaires au moteur proot.
 */
class LinuxHost(private val context: Context) : ProotHost {

    override val prefixDir: File by lazy {
        File(context.filesDir, Constants.DIR_PROOT).ensureDir()
    }

    override val rootfsDir: File by lazy {
        File(prefixDir, Constants.DIR_ROOTFS).ensureDir()
    }

    override val homeDir: File by lazy {
        File(prefixDir, Constants.DIR_HOME).ensureDir()
    }

    override val tmpDir: File by lazy {
        File(context.cacheDir, Constants.DIR_TMP).ensureDir()
    }

    override val nativeLibsDir: File by lazy {
        File(context.applicationInfo.nativeLibraryDir)
    }

    override val packageName: String
        get() = context.packageName

    private fun File.ensureDir(): File {
        if (!exists()) mkdirs()
        return this
    }

    /** Retourne le répertoire rootfs d'une distro */
    fun rootfsFor(distro: String): File = File(rootfsDir, distro)

    /** Vérifie que les binaires proot natifs sont présents */
    fun hasNativeBinaries(): Boolean {
        val proot = File(nativeLibsDir, "libproot.so")
        val loader = File(nativeLibsDir, "libproot_loader.so")
        return proot.exists() && loader.exists()
    }
}