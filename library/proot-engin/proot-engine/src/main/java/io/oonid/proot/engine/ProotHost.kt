package io.oonid.proot.engine

import java.io.File

/**
 * Interface que l'application hôte doit implémenter
 * pour fournir les chemins de stockage au moteur proot.
 */
interface ProotHost {

    /** Répertoire racine de l'installation proot */
    val prefixDir: File

    /** Répertoire des rootfs (une par distribution) */
    val rootfsDir: File

    /** Répertoire home (données utilisateur) */
    val homeDir: File

    /** Répertoire temporaire pour proot */
    val tmpDir: File

    /** Répertoire des binaires natifs (loader proot) */
    val nativeLibsDir: File

    /** Nom de package utilisé par le bootstrap Termux */
    val packageName: String
}