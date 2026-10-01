package com.voidlinux.feature.linux

import android.content.Context
import com.voidlinux.core.common.Constants
import com.voidlinux.core.common.VoidResult
import io.oonid.proot.engine.ProotManager
import io.oonid.proot.engine.ProotState
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Point d'accès unique aux fonctionnalités Linux.
 */
class LinuxRepository(private val context: Context) {

    private val host = LinuxHost(context)
    private val prootManager = ProotManager(context, host)
    private val installer = LinuxInstaller(context, host, prootManager)

    val prootState: StateFlow<ProotState> = prootManager.state

    suspend fun installDistro(
        distroId: String = Constants.DISTRO_KALI,
        onProgress: (Int) -> Unit = {}
    ): VoidResult<File> {
        val distro = DistroCatalog.byId(distroId)
            ?: return VoidResult.Error("Distribution inconnue : $distroId")
        return installer.install(distro, onProgress)
    }

    suspend fun launchShell(
        distroId: String = Constants.DISTRO_KALI,
        command: List<String>? = null,
        onStdout: (String) -> Unit,
        onStderr: (String) -> Unit,
        onExit: (Int) -> Unit
    ): VoidResult<Unit> {
        val distro = DistroCatalog.byId(distroId)
            ?: return VoidResult.Error("Distribution inconnue : $distroId")

        if (!installer.isInstalled(distroId)) {
            return VoidResult.Error("${distro.displayName} n'est pas installé")
        }

        val cmd = command ?: listOf(distro.defaultShell, "-l")

        return try {
            prootManager.start(
                distro = distroId,
                command = cmd,
                onStdout = onStdout,
                onStderr = onStderr,
                onExit = onExit
            )
            VoidResult.Success(Unit)
        } catch (e: Exception) {
            VoidResult.Error("Échec du lancement", e)
        }
    }

    fun stopShell() = prootManager.stop()

    fun isInstalled(distroId: String = Constants.DISTRO_KALI): Boolean =
        installer.isInstalled(distroId)

    fun uninstall(distroId: String = Constants.DISTRO_KALI): Boolean =
        installer.uninstall(distroId)

    fun getDistroInfo(distroId: String): DistroCatalog.Distro? =
        DistroCatalog.byId(distroId)

    fun hasNativeSupport(): Boolean = host.hasNativeBinaries()
}