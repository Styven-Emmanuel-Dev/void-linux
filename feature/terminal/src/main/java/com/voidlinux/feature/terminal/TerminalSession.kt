package com.voidlinux.feature.terminal

import android.content.Context
import com.voidlinux.core.native.NativeBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Session terminal active.
 * Combine le PTY natif (NativeBridge) avec le tampon d'affichage.
 */
class TerminalSession(
    private val context: Context,
    private val buffer: TerminalBuffer,
    private val onOutput: (String) -> Unit
) {

    private var masterFd: Int = -1
    private var slaveFd: Int = -1
    private var pid: Int = -1

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var readerJob: Job? = null

    var isRunning: Boolean = false
        private set

    /** Démarre une session dans le PTY avec la commande donnée */
    fun start(command: String) {
        if (isRunning) return

        val ptys = NativeBridge.createPty(buffer.cols, buffer.rows)
        if (ptys == null || ptys.size < 2) {
            onOutput("\r\n[erreur] Impossible de créer le PTY\r\n")
            return
        }

        masterFd = ptys[0]
        slaveFd = ptys[1]

        pid = NativeBridge.execInPty(masterFd, command)
        isRunning = pid > 0

        startReading()
    }

    private fun startReading() {
        readerJob = scope.launch {
            while (isActive && isRunning) {
                val data = NativeBridge.readFromPty(masterFd, 4096)
                if (data == null || data.isEmpty()) {
                    delay(20)
                    continue
                }
                val text = String(data, Charsets.UTF_8)
                buffer.let { }
                onOutput(text)
            }
        }
    }

    /** Envoie une saisie utilisateur au PTY */
    fun write(data: String) {
        if (!isRunning) return
        scope.launch {
            NativeBridge.writeToPty(masterFd, data.toByteArray(Charsets.UTF_8))
        }
    }

    /** Redimensionne le PTY */
    fun resize(cols: Int, rows: Int) {
        if (masterFd >= 0) {
            NativeBridge.resizePty(masterFd, cols, rows)
        }
    }

    /** Termine la session */
    fun stop() {
        isRunning = false
        readerJob?.cancel()
        readerJob = null

        if (masterFd >= 0) {
            NativeBridge.closePty(masterFd)
            masterFd = -1
        }
        if (slaveFd >= 0) {
            NativeBridge.closePty(slaveFd)
            slaveFd = -1
        }
        pid = -1
    }

    fun getPid(): Int = pid
}