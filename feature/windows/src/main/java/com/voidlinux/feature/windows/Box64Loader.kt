package com.voidlinux.feature.windows

import android.content.Context
import android.os.Build
import com.voidlinux.core.common.Logger
import java.io.File

/**
 * Gère le chargement de Box64 : émulateur x86_64 → ARM64.
 * Nécessaire pour exécuter les binaires Windows x86_64 sur ARM.
 */
class Box64Loader(private val context: Context) {

    private val nativeLibDir: File =
        File(context.applicationInfo.nativeLibraryDir)

    private val box64Binary: File by lazy {
        File(nativeLibDir, "libbox64.so")
    }

    private val box86Binary: File by lazy {
        File(nativeLibDir, "libbox86.so")
    }

    /**
     * Vérifie que Box64 est disponible pour cette architecture.
     */
    fun isAvailable(): Boolean {
        return when {
            isArm64() -> box64Binary.exists()
            isArm32() -> box86Binary.exists()
            isX86_64() -> true // Pas besoin d'émulation
            else -> false
        }
    }

    fun getBinaryPath(): String? {
        return when {
            isArm64() && box64Binary.exists() -> box64Binary.absolutePath
            isArm32() && box86Binary.exists() -> box86Binary.absolutePath
            else -> null
        }
    }

    fun needsEmulation(): Boolean {
        return isArm64() || isArm32()
    }

    /**
     * Variables d'environnement pour Box64.
     */
    fun getEnvVars(): Map<String, String> {
        val env = mutableMapOf<String, String>()

        if (needsEmulation()) {
            env["BOX64_NOBANNER"] = "1"
            env["BOX64_DYNAREC"] = "1"
            env["BOX64_DYNAREC_SAFEFLAGS"] = "2"
            env["BOX64_DYNAREC_BIGBLOCK"] = "2"
            env["BOX64_DYNAREC_STRONGMEM"] = "1"
            env["BOX64_DYNAREC_FASTNAN"] = "1"
            env["BOX64_DYNAREC_FASTROUND"] = "1"
            env["BOX64_DYNAREC_X87DOUBLE"] = "1"
            env["BOX64_LOG"] = "0"
            env["BOX64_NOBANNER"] = "1"
        }

        env["WINEPREFIX"] = File(context.filesDir, "wine/prefix").absolutePath
        env["WINEDEBUG"] = "-all"
        env["WINEARCH"] = "win64"
        env["DISPLAY"] = ":0"

        return env
    }

    private fun isArm64(): Boolean =
        Build.SUPPORTED_ABIS.any { it.equals("arm64-v8a", ignoreCase = true) }

    private fun isArm32(): Boolean =
        Build.SUPPORTED_ABIS.any { it.startsWith("armeabi", ignoreCase = true) }

    private fun isX86_64(): Boolean =
        Build.SUPPORTED_ABIS.any { it.equals("x86_64", ignoreCase = true) }

    fun getArchitecture(): String = when {
        isArm64() -> "arm64"
        isArm32() -> "arm32"
        isX86_64() -> "x86_64"
        else -> "unknown"
    }
}