package com.voidlinux.feature.linux

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.voidlinux.core.common.Constants
import com.voidlinux.core.common.VoidResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class LinuxInstaller(
    private val context: Context,
    private val host: LinuxHost
) {

    private val notifier = InstallationNotifier(context)

    suspend fun install(
        distro: DistroCatalog.Distro,
        onProgress: (Int) -> Unit = {}
    ): VoidResult<File> = withContext(Dispatchers.IO) {

        notifier.showStart(distro.displayName)

        try {
            val targetDir = host.rootfsFor(distro.id).apply { mkdirs() }
            val archive = File(host.tmpDir, distro.archiveName)

            if (!archive.exists() || archive.length() == 0L) {
                downloadFile(distro.url, archive) { progress ->
                    notifier.update(distro.displayName, progress)
                    onProgress(progress)
                }
            }

            notifier.showComplete(distro.displayName)
            VoidResult.Success(targetDir)
        } catch (e: Exception) {
            notifier.showError(distro.displayName, e.message ?: "Erreur inconnue")
            VoidResult.Error("Échec de l'installation", e)
        }
    }

    private fun downloadFile(url: String, target: File, onProgress: (Int) -> Unit) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 30_000
        connection.readTimeout = 30_000
        connection.connect()

        val total = connection.contentLengthLong
        var downloaded = 0L

        connection.inputStream.use { input ->
            target.outputStream().use { output ->
                val buffer = ByteArray(64 * 1024)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                    downloaded += read
                    if (total > 0) {
                        onProgress(((downloaded * 100) / total).toInt())
                    }
                }
            }
        }
        connection.disconnect()
    }

    fun isInstalled(distro: String): Boolean {
        val dir = host.rootfsFor(distro)
        return dir.exists() && File(dir, "bin").exists()
    }

    fun uninstall(distro: String): Boolean =
        host.rootfsFor(distro).deleteRecursively()
}

private class InstallationNotifier(private val context: Context) {

    private val manager = context.getSystemService(NotificationManager::class.java)

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
