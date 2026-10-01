package io.oonid.proot.engine

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Point d'entrée unique du moteur proot.
 */
class ProotManager(
    private val context: Context,
    private val host: ProotHost
) {

    private val installer = ProotInstaller(context, host)
    private val launcher = ProotLauncher(context, host)

    private val _state = MutableStateFlow<ProotState>(ProotState.Idle)
    val state: StateFlow<ProotState> = _state

    private var currentProcess: Process? = null

    suspend fun install(
        distro: String,
        url: String,
        archiveName: String,
        onProgress: (Int) -> Unit = {}
    ): File {
        _state.value = ProotState.Installing(0)
        return try {
            val dir = installer.installRootfs(distro, url, archiveName) {
                _state.value = ProotState.Installing(it)
                onProgress(it)
            }
            _state.value = ProotState.Installed(distro)
            dir
        } catch (e: Exception) {
            _state.value = ProotState.Error(e.message ?: "Erreur inconnue", e)
            throw e
        }
    }

    suspend fun start(
        distro: String,
        command: List<String> = listOf("/bin/bash", "-l"),
        onStdout: (String) -> Unit = {},
        onStderr: (String) -> Unit = {},
        onExit: (Int) -> Unit = {}
    ): Process {
        val rootfs = File(host.rootfsDir, distro)
        val config = ProotConfig(
            distroName = distro,
            rootfsPath = rootfs,
            command = command
        )

        _state.value = ProotState.Running(distro)

        val process = launcher.launch(
            config = config,
            onStdout = onStdout,
            onStderr = onStderr,
            onExit = { code ->
                _state.value = ProotState.Idle
                onExit(code)
            }
        )
        currentProcess = process
        return process
    }

    fun stop() {
        currentProcess?.destroy()
        currentProcess = null
        _state.value = ProotState.Idle
    }

    fun isInstalled(distro: String): Boolean =
        installer.isRootfsInstalled(distro)

    fun uninstall(distro: String): Boolean {
        val dir = File(host.rootfsDir, distro)
        return dir.deleteRecursively()
    }
}

sealed class ProotState {
    object Idle : ProotState()
    data class Installing(val progress: Int) : ProotState()
    data class Installed(val distro: String) : ProotState()
    data class Running(val distro: String) : ProotState()
    data class Error(val message: String, val cause: Throwable? = null) : ProotState()
}