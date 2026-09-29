package com.voidlinux.feature.linux

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.voidlinux.core.common.Constants
import com.voidlinux.core.common.VoidResult
import io.oonid.proot.engine.ProotManager
import io.oonid.proot.engine.ProotState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Orchestre l'installation d'une distribution Linux.
 * Affiche la progression via notification.
 */
class LinuxInstaller(
    private val context: Context,
    private val host: LinuxHost,
    private val prootManager: ProotManager
) {

    private val notifier = InstallationNotifier(context)

    suspend fun install(
        distro: DistroCatalog.Distro,
        onProgress: (Int) -> Unit = {}
    ): VoidResult<File> = withContext(Dispatchers.IO) {

        if (!host.hasNativeBinaries()) {
            return@withContext VoidResult.Error(
                "Binaires proot natifs manquants. Vérifie library/proot-engine/src/main/jniLibs/"
            )
        }

        notifier.showStart(distro.displayName)

        return@withContext try {
            val rootfs = prootManager.install(
                distro = distro.id,
                url = distro.url,
                archiveName = distro.archiveName
            ) { progress ->
                notifier.update(distro.displayName, progress)
                onProgress(progress)
            }

            notifier.showComplete(distro.displayName)
            VoidResult.Success(rootfs)
        } catch (e: Exception) {
            notifier.showError(distro.displayName, e.message ?: "Erreur inconnue")
            VoidResult.Error("Échec de l'installation de ${distro.displayName}", e)
        }
    }

    fun isInstalled(distro: String): Boolean =
        prootManager.isInstalled(distro)

    fun uninstall(distro: String): Boolean =
        prootManager.uninstall(distro)
}

/**
 * Gère les notifications de progression d'installation.
 */
private class InstallationNotifier(private val context: Context) {

    private val manager =
        context.getSystemService(NotificationManager::class.java)

    fun showStart(distroName: String) {
        val notif = NotificationCompat.Builder(context, Constants.CHANNEL_INSTALL)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Installation de $distroName")
            .setContentText("Préparation…")
            .setOngoing(true)
            .setProgress(100, 0, true)
            .build()
        manager.notify(Constants.NOTIF_ID_INSTALL, notif)
    }

    fun update(distroName: String, progress: Int) {
        val notif = NotificationCompat.Builder(context, Constants.CHANNEL_INSTALL)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Installation de $distroName")
            .setContentText("$progress %")
            .setOngoing(true)
            .setProgress(100, progress, false)
            .build()
        manager.notify(Constants.NOTIF_ID_INSTALL, notif)
    }

    fun showComplete(distroName: String) {
        val notif = NotificationCompat.Builder(context, Constants.CHANNEL_INSTALL)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("$distroName installé")
            .setContentText("Prêt à l'emploi")
            .setAutoCancel(true)
            .build()
        manager.notify(Constants.NOTIF_ID_INSTALL, notif)
    }

    fun showError(distroName: String, message: String) {
        val notif = NotificationCompat.Builder(context, Constants.CHANNEL_SECURITY)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Échec de l'installation")
            .setContentText("$distroName : $message")
            .setAutoCancel(true)
            .build()
        manager.notify(Constants.NOTIF_ID_INSTALL, notif)
    }
}