package com.voidlinux.feature.linux

import android.content.Context
import com.voidlinux.core.common.VoidResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Gestion bas niveau du système de fichiers d'un rootfs.
 * Permet de vérifier l'espace disque, nettoyer, sauvegarder.
 */
class RootfsManager(private val context: Context) {

    private val host = LinuxHost(context)

    suspend fun getSize(distro: String): Long = withContext(Dispatchers.IO) {
        val dir = host.rootfsFor(distro)
        if (!dir.exists()) return@withContext 0L
        dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    suspend fun getFreeSpace(): Long = withContext(Dispatchers.IO) {
        val stat = android.os.StatFs(context.filesDir.absolutePath)
        stat.availableBytes
    }

    suspend fun hasEnoughSpace(requiredBytes: Long): Boolean = withContext(Dispatchers.IO) {
        getFreeSpace() > requiredBytes
    }

    suspend fun cleanTmp(): VoidResult<Unit> = withContext(Dispatchers.IO) {
        try {
            host.tmpDir.listFiles()?.forEach { it.deleteRecursively() }
            VoidResult.Success(Unit)
        } catch (e: Exception) {
            VoidResult.Error("Échec du nettoyage", e)
        }
    }

    suspend fun backupHome(): VoidResult<File> = withContext(Dispatchers.IO) {
        try {
            val backup = File(
                context.getExternalFilesDir(null),
                "void-home-${System.currentTimeMillis()}.tar"
            )
            // TODO: implémenter archive tar de homeDir
            VoidResult.Success(backup)
        } catch (e: Exception) {
            VoidResult.Error("Échec du backup", e)
        }
    }

    fun getHomePath(): File = host.homeDir

    fun getRootfsPath(distro: String): File = host.rootfsFor(distro)

    fun listInstalledDistros(): List<String> {
        return host.rootfsDir.listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.name }
            ?: emptyList()
    }
}