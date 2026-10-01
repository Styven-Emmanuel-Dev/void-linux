package com.voidlinux.feature.linux

import com.voidlinux.core.common.Constants

/**
 * Catalogue des distributions Linux supportées.
 */
object DistroCatalog {

    data class Distro(
        val id: String,
        val displayName: String,
        val url: String,
        val archiveName: String,
        val defaultShell: String = "/bin/bash",
        val defaultUser: String = "root"
    )

    val KALI = Distro(
        id = Constants.DISTRO_KALI,
        displayName = "Kali Linux",
        url = Constants.KALI_ROOTFS_ARM64_URL,
        archiveName = Constants.KALI_ROOTFS_ARM64_NAME,
        defaultShell = "/bin/bash"
    )

    val DEBIAN = Distro(
        id = Constants.DISTRO_DEBIAN,
        displayName = "Debian",
        url = Constants.KALI_ROOTFS_ARM64_URL, // placeholder — remplacer par URL Debian
        archiveName = "debian-arm64.tar.xz"
    )

    val UBUNTU = Distro(
        id = Constants.DISTRO_UBUNTU,
        displayName = "Ubuntu",
        url = Constants.KALI_ROOTFS_ARM64_URL, // placeholder
        archiveName = "ubuntu-arm64.tar.xz"
    )

    val ALPINE = Distro(
        id = Constants.DISTRO_ALPINE,
        displayName = "Alpine",
        url = Constants.KALI_ROOTFS_ARM64_URL, // placeholder
        archiveName = "alpine-arm64.tar.xz",
        defaultShell = "/bin/ash"
    )

    val all = listOf(KALI, DEBIAN, UBUNTU, ALPINE)

    fun byId(id: String): Distro? = all.firstOrNull { it.id == id }
}