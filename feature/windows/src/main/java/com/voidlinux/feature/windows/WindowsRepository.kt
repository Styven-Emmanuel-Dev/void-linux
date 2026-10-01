package com.voidlinux.feature.windows

import android.content.Context
import com.voidlinux.core.common.VoidResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Façade unique du module Windows.
 */
class WindowsRepository(private val context: Context) {

    private val container = WineContainer(context)
    private val box64 = Box64Loader(context)
    private val runner = ExeRunner(context, container, box64)

    fun isWineReady(): Boolean = container.isInitialized()

    fun isBox64Available(): Boolean = box64.isAvailable()

    fun getArchitecture(): String = box64.getArchitecture()

    suspend fun initializeWine(): VoidResult<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            container.initializeStructure()
            val result = runner.initializePrefix()
            if (result.success) VoidResult.Success(Unit)
            else VoidResult.Error("Échec wineboot : ${result.stderr}")
        } catch (e: Exception) {
            VoidResult.Error("Erreur initialisation Wine", e)
        }
    }

    suspend fun runExe(
        exeFile: File,
        args: List<String> = emptyList(),
        onStdout: (String) -> Unit = {},
        onStderr: (String) -> Unit = {}
    ): VoidResult<ExeRunner.RunResult> = withContext(Dispatchers.IO) {

        if (!isWineReady()) {
            val init = initializeWine()
            if (init is VoidResult.Error) return@withContext init
        }

        val result = runner.run(exeFile, args, onStdout, onStderr)

        if (result.success) VoidResult.Success(result)
        else VoidResult.Error("Échec : ${result.stderr}", code = result.exitCode)
    }

    fun stop() = runner.stop()

    fun cleanWine() = container.clean()

    fun getUsersDir(): File = container.usersDir
}