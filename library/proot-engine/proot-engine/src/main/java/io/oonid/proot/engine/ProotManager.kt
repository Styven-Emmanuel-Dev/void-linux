package io.oonid.proot.engine

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class ProotManager(
    private val context: Context,
    private val host: ProotHost
) {
    private val _state = MutableStateFlow<ProotState>(ProotState.Idle)
    val state: StateFlow<ProotState> = _state

    suspend fun install(
        distro: String,
        url: String,
        archiveName: String,
        onProgress: (Int) -> Unit = {}
    ): File {
        val target = File(host.rootfsDir, distro).apply { mkdirs() }
        return target
    }

    suspend fun start(
        distro: String,
        command: List<String> = listOf("/bin/bash", "-l"),
        onStdout: (String) -> Unit = {},
        onStderr: (String) -> Unit = {},
        onExit: (Int) -> Unit = {}
    ): Process? {
        return null
    }

    fun stop() {}

    fun isInstalled(distro: String): Boolean =
        File(host.rootfsDir, distro).exists()

    fun uninstall(distro: String): Boolean =
        File(host.rootfsDir, distro).deleteRecursively()
}

sealed class ProotState {
    object Idle : ProotState()
    data class Installing(val progress: Int) : ProotState()
    data class Installed(val distro: String) : ProotState()
    data class Running(val distro: String) : ProotState()
    data class Error(val message: String, val cause: Throwable? = null) : ProotState()
}
