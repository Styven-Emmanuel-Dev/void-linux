package com.voidlinux.feature.terminal

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Session terminal simplifiée.
 * À terme : PTY natif via NativeBridge et proot-engine.
 */
class TerminalSession(
    private val context: Context,
    private val buffer: TerminalBuffer,
    private val onOutput: (String) -> Unit
) {

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var readerJob: Job? = null

    var isRunning: Boolean = false
        private set

    fun start(command: String) {
        if (isRunning) return
        isRunning = true

        scope.launch {
            onOutput("\r\n[Void-Linux] Terminal placeholder\r\n")
            onOutput("$ ")
        }
    }

    fun write(data: String) {
        if (!isRunning) return
        scope.launch {
            // Echo simple en attendant le PTY natif
            if (data == "\r" || data == "\n") {
                onOutput("\r\n$ ")
            } else if (data == "\u007F") {
                onOutput("\b \b")
            } else {
                onOutput(data)
            }
        }
    }

    fun resize(cols: Int, rows: Int) {
        buffer.resize(cols, rows)
    }

    fun stop() {
        isRunning = false
        readerJob?.cancel()
        readerJob = null
    }
}
