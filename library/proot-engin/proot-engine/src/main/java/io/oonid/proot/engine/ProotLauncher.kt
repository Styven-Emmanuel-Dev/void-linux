package io.oonid.proot.engine

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Construit et exécute la commande proot.
 */
class ProotLauncher(
    private val context: Context,
    private val host: ProotHost
) {

    private val prootBinary: File by lazy {
        File(host.nativeLibsDir, "libproot.so")
    }

    private val loaderBinary: File by lazy {
        File(host.nativeLibsDir, "libproot_loader.so")
    }

    suspend fun launch(
        config: ProotConfig,
        onStdout: (String) -> Unit = {},
        onStderr: (String) -> Unit = {},
        onExit: (Int) -> Unit = {}
    ): Process = withContext(Dispatchers.IO) {

        val args = buildProotArgs(config)
        val env = buildEnv(config)

        val pb = ProcessBuilder(listOf(prootBinary.absolutePath) + args)
        pb.environment().putAll(env)
        pb.directory(host.homeDir)
        pb.redirectErrorStream(false)

        val process = pb.start()

        // Thread stdout
        Thread {
            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach(onStdout)
            }
        }.start()

        // Thread stderr
        Thread {
            process.errorStream.bufferedReader().useLines { lines ->
                lines.forEach(onStderr)
            }
        }.start()

        // Thread exit
        Thread {
            val code = process.waitFor()
            onExit(code)
        }.start()

        process
    }

    private fun buildProotArgs(config: ProotConfig): List<String> {
        val args = mutableListOf<String>()

        // Rootfs
        args += "-r"
        args += config.rootfsPath.absolutePath

        // Working dir
        args += "-w"
        args += config.workingDir

        // Fake root
        if (config.fakeRoot) {
            args += "-0"
        }

        // Kernel release
        args += "-k"
        args += config.kernelRelease

        // Link loader
        if (config.linkLoader) {
            args += "--link2symlink"
        }

        // Loader proot
        args += "-l"
        args += loaderBinary.absolutePath

        // Bind mounts système
        args += "--bind=/dev"
        args += "--bind=/proc"
        args += "--bind=/sys"

        // Bind mounts custom
        config.bindMounts.forEach { mount ->
            args += "--bind=${mount.hostPath.absolutePath}:${mount.guestPath}"
        }

        // Home
        args += "--bind=${host.homeDir.absolutePath}:/root"

        // Env
        config.envVars.forEach { (k, v) ->
            args += "-E"
            args += "$k=$v"
        }

        // Commande
        args += "--"
        args += config.command

        return args
    }

    private fun buildEnv(config: ProotConfig): Map<String, String> {
        val env = mutableMapOf(
            "PROOT_TMP_DIR" to host.tmpDir.absolutePath,
            "PROOT_LOADER" to loaderBinary.absolutePath,
            "HOME" to "/root",
            "PATH" to "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
            "TERM" to "xterm-256color",
            "LANG" to "C.UTF-8",
            "LC_ALL" to "C.UTF-8",
            "USER" to "root",
            "SHELL" to "/bin/bash"
        )
        env.putAll(config.envVars)
        return env
    }

    fun isReady(): Boolean =
        prootBinary.exists() && loaderBinary.exists()
}