package com.voidlinux.feature.linux

import android.content.Context
import com.voidlinux.core.common.Constants
import com.voidlinux.core.common.VoidResult
import java.io.File

class LinuxRepository(context: Context) {

    private val host = LinuxHost(context)
    private val installer = LinuxInstaller(context, host)

    suspend fun installDistro(
        distroId: String = Constants.DISTRO_KALI,
        onProgress: (Int) -> Unit = {}
    ): VoidResult<File> {
        val distro = DistroCatalog.byId(distroId)
            ?: return VoidResult.Error("Distribution inconnue : $distroId")
        return installer.install(distro, onProgress)
    }

    fun isInstalled(distroId: String = Constants.DISTRO_KALI): Boolean =
        installer.isInstalled(distroId)

    fun uninstall(distroId: String = Constants.DISTRO_KALI): Boolean =
        installer.uninstall(distroId)

    fun getDistroInfo(distroId: String): DistroCatalog.Distro? =
        DistroCatalog.byId(distroId)

    fun hasNativeSupport(): Boolean = host.hasNativeBinaries()
}
