package io.oonid.proot.engine

import java.io.File

/**
 * Configuration d'une session proot.
 */
data class ProotConfig(
    val distroName: String,
    val rootfsPath: File,
    val command: List<String> = listOf("/bin/bash", "-l"),
    val workingDir: String = "/root",
    val envVars: Map<String, String> = emptyMap(),
    val bindMounts: List<BindMount> = emptyList(),
    val fakeRoot: Boolean = true,
    val fakeRootId: Int = 0,
    val kernelRelease: String = "5.15.0-voidlinux",
    val noSeccomp: Boolean = false,
    val linkLoader: Boolean = true
)

data class BindMount(
    val hostPath: File,
    val guestPath: String
)