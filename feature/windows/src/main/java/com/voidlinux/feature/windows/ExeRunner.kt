package com.voidlinux.feature.windows

import android.content.Context
import com.voidlinux.core.common.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Exécute des fichiers .exe via Wine + Box64.
 * Retourne la sortie standard et d'erreur en temps réel.
 */
class ExeRunner(
    private val context: Context,
    private val container: WineContainer,
    private val box64: Box64Loader
) {

    data class RunResult(
        val success: Boolean,
        val exitCode: Int,
        val stdout: String,
        val stderr: String
    )

    private var currentProcess: Process? = null

    suspend fun run(
        exeFile: File,
        args: List<String> = emptyList(),
        onStdout: (String) -> Unit = {},
        onStderr: (String) -> Unit = {}
    ): RunResult = withContext(Dispatchers.IO) {

        if (!exeFile.exists()) {
            return@withContext RunResult(
                false, -1, "", "Fichier introuvable : ${exeFile.absolutePath}"
            )
        }

        if (!box64.isAvailable()) {
            return@withContext RunResult(
                false, -1, "", "Box64 non disponible pour ${box64.getArchitecture()}"
            )
        }

        val wineBinary = findWineBinary()
            ?: return@withContext RunResult(false, -1, "", "Wine introuvable")

        val stdoutBuf = StringBuilder()
        val stderrBuf = StringBuilder()

        val command = buildList {
            if (box64.needsEmulation()) {
                add(box64.getBinaryPath() ?: "")
            }
            add(wineBinary.absolutePath)
            add(exeFile.absolutePath)
            addAll(args)
        }.filter { it.isNotEmpty() }

        return@withContext try {
            val pb = ProcessBuilder(command)
            pb.directory(container.usersDir)
            pb.environment().putAll(box64.getEnvVars())
            pb.environment()["WINEPREFIX"] = container.getPrefixPath()
            pb.redirectErrorStream(false)

            val process = pb.start()
            currentProcess = process

            val outThread = Thread {
                process.inputStream.bufferedReader().useLines { lines ->
                    lines.forEach {
                        stdoutBuf.appendLine(it)
                        onStdout(it)
                    }
                }
            }

            val errThread = Thread {
                process.errorStream.bufferedReader().useLines { lines ->
                    lines.forEach {
                        stderrBuf.appendLine(it)
                        onStderr(it)
                    }
                }
            }

            outThread.start()
            errThread.start()

            val exit = process.waitFor()
            outThread.join(1000)
            errThread.join(1000)
            currentProcess = null

            RunResult(
                success = exit == 0,
                exitCode = exit,
                stdout = stdoutBuf.toString(),
                stderr = stderrBuf.toString()
            )
        } catch (e: Exception) {
            Logger.e("Erreur exécution .exe", e)
            RunResult(false, -1, "", e.message ?: "Erreur inconnue")
        }
    }

    fun stop() {
        currentProcess?.destroy()
        currentProcess = null
    }

    /**
     * Cherche wine64 ou wine dans le conteneur Linux.
     */
    private fun findWineBinary(): File? {
        val candidates = listOf(
            File(context.filesDir, "proot/rootfs/kali/usr/bin/wine64"),
            File(context.filesDir, "proot/rootfs/kali/usr/bin/wine"),
            File(context.filesDir, "wine/bin/wine64"),
            File(context.filesDir, "wine/bin/wine")
        )
        return candidates.firstOrNull { it.exists() }
    }

    /**
     * Lance wineboot pour initialiser le préfixe.
     */
    suspend fun initializePrefix(): RunResult = withContext(Dispatchers.IO) {
        val wineBinary = findWineBinary()
            ?: return@withContext RunResult(false, -1, "", "Wine introuvable")

        container.initializeStructure()

        try {
            val pb = ProcessBuilder(
                listOf(wineBinary.absolutePath, "wineboot", "-i")
            )
            pb.environment().putAll(box64.getEnvVars())
            pb.environment()["WINEPREFIX"] = container.getPrefixPath()

            val process = pb.start()
            val exit = process.waitFor()

            RunResult(exit == 0, exit, "", "")
        } catch (e: Exception) {
            RunResult(false, -1, "", e.message ?: "Erreur wineboot")
        }
    }
}