package com.voidlinux.core.common

object Constants {

    // Canaux de notification
    const val CHANNEL_SECURITY = "void_security"
    const val CHANNEL_SYSTEM = "void_system"
    const val CHANNEL_INSTALL = "void_install"

    // IDs de notification
    const val NOTIF_ID_SECURITY = 1001
    const val NOTIF_ID_INSTALL = 1002
    const val NOTIF_ID_TOR = 1003
    const val NOTIF_ID_FOREGROUND = 1004

    // Répertoires internes
    const val DIR_PROOT = "proot"
    const val DIR_ROOTFS = "rootfs"
    const val DIR_HOME = "home"
    const val DIR_TMP = "tmp"
    const val DIR_WINE = "wine"
    const val DIR_LOGS = "logs"

    // Distributions Linux supportées
    const val DISTRO_KALI = "kali"
    const val DISTRO_DEBIAN = "debian"
    const val DISTRO_UBUNTU = "ubuntu"
    const val DISTRO_ALPINE = "alpine"

    // URLs rootfs (Kali NetHunter)
    const val KALI_ROOTFS_ARM64_URL =
        "https://kali.download/nethunter-images/current/rootfs/kali-nethunter-rootfs-minimal-arm64.tar.xz"
    const val KALI_ROOTFS_ARMHF_URL =
        "https://kali.download/nethunter-images/current/rootfs/kali-nethunter-rootfs-minimal-armhf.tar.xz"

    const val KALI_ROOTFS_ARM64_NAME = "kali-arm64.tar.xz"
    const val KALI_ROOTFS_ARMHF_NAME = "kali-armhf.tar.xz"

    // Tor
    const val TOR_SOCKS_PORT = 9050
    const val TOR_HTTP_PORT = 8118

    // Terminal
    const val TERMINAL_DEFAULT_COLS = 80
    const val TERMINAL_DEFAULT_ROWS = 24

    // Sécurité
    const val SCAN_INTERVAL_MS = 30_000L
    const val BATTERY_DRAIN_THRESHOLD = 15
    const val NETWORK_USAGE_THRESHOLD_MB = 500L

    // Service foreground
    const val FOREGROUND_SERVICE_ID = 2001
}