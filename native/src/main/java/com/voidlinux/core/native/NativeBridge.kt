package com.voidlinux.core.native

object NativeBridge {

    init {
        System.loadLibrary("voidlinux_jni")
    }

    // --- Proot loader ---

    external fun loadElf(path: String): Int

    external fun checkWxSupported(): Int

    // --- PTY ---

    external fun createPty(cols: Int, rows: Int): IntArray?

    external fun resizePty(fd: Int, cols: Int, rows: Int)

    external fun closePty(fd: Int)

    // --- Terminal ---

    external fun execInPty(masterFd: Int, command: String): Int

    external fun writeToPty(fd: Int, data: ByteArray): Int

    external fun readFromPty(fd: Int, maxBytes: Int): ByteArray?
}