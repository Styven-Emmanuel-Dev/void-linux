package com.voidlinux.core.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import java.io.File
import java.text.DecimalFormat

fun Context.isNetworkAvailable(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun Context.isWifiConnected(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
}

fun File.sizeReadable(): String {
    val df = DecimalFormat("#.##")
    val kb = length() / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1 -> "${df.format(gb)} GB"
        mb >= 1 -> "${df.format(mb)} MB"
        else -> "${df.format(kb)} KB"
    }
}

fun File.ensureDir(): File {
    if (!exists()) mkdirs()
    return this
}

fun File.deleteRecursive() {
    if (isDirectory) listFiles()?.forEach { it.deleteRecursive() }
    delete()
}

fun Long.toMb(): Long = this / (1024 * 1024)

fun isArm64(): Boolean =
    Build.SUPPORTED_ABIS.any { it.contains("arm64") }

fun isArm(): Boolean =
    Build.SUPPORTED_ABIS.any { it.contains("armeabi") }

fun isX86(): Boolean =
    Build.SUPPORTED_ABIS.any { it.contains("x86") }