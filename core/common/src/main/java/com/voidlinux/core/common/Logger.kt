package com.voidlinux.core.common

import android.util.Log

object Logger {

    private const val TAG = "VoidLinux"
    private var debugEnabled = true

    fun setDebug(enabled: Boolean) {
        debugEnabled = enabled
    }

    fun d(message: String) {
        if (debugEnabled) Log.d(TAG, message)
    }

    fun i(message: String) {
        Log.i(TAG, message)
    }

    fun w(message: String) {
        Log.w(TAG, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
}