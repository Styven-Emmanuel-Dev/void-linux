package com.voidlinux.feature.linux

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Session proot simplifiée.
 * À terme : utilise ProotManager du :proot-engine pour lancer un vrai shell.
 */
class LinuxSession(
    private val context: Context,
    private val distroId: String,
    private val onOutput: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onExit: (Int) -> Unit
) {

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var running = false

    fun start(command: List<String>? = null) {
        if (running) return
        running = true

        scope.launch {
            try {
                onOutput("[Void-Linux] Session placeholder — proot-engine non intégré\n")
                onOutput("[Void-Linux] Terminal fonctionnel via /system/bin/sh\n")
            } catch (e: Exception) {
                onError(e.message ?: "Erreur inconnue")
                onExit(-1)
            }
        }
    }

    fun stop() {
        running = false
    }

    fun isRunning(): Boolean = running
}
