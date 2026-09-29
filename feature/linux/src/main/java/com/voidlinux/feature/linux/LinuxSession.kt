package com.voidlinux.feature.linux

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Représente une session proot active avec son cycle de vie.
 * Utilise un buffer de sortie pour être consommé par le terminal.
 */
class LinuxSession(
    private val context: Context,
    private val distroId: String,
    private val onOutput: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onExit: (Int) -> Unit
) {

    private val repo = LinuxRepository(context)
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var running = false

    fun start(command: List<String>? = null) {
        if (running) return
        running = true

        scope.launch {
            repo.launchShell(
                distroId = distroId,
                command = command,
                onStdout = { line -> onOutput("$line\n") },
                onStderr = { line -> onError("$line\n") },
                onExit = { code ->
                    running = false
                    onExit(code)
                }
            )
        }
    }

    fun stop() {
        repo.stopShell()
        running = false
    }

    fun isRunning(): Boolean = running
}