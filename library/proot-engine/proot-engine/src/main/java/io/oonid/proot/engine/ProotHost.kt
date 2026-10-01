package io.oonid.proot.engine

import java.io.File

interface ProotHost {
    val prefixDir: File
    val rootfsDir: File
    val homeDir: File
    val tmpDir: File
    val nativeLibsDir: File
    val packageName: String
}
