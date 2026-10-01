package io.oonid.proot.engine

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream
import org.tukaani.xz.XZInputStream

/**
 * Télécharge, extrait et prépare le rootfs.
 * Supporte .tar.gz et .tar.xz (Kali NetHunter).
 */
class ProotInstaller(
    private val context: Context,
    private val host: ProotHost
) {

    suspend fun installRootfs(
        distroName: String,
        sourceUrl: String,
        archiveName: String,
        onProgress: (Int) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {

        val targetDir = File(host.rootfsDir, distroName).apply { mkdirs() }
        val archive = File(host.tmpDir, archiveName)

        // 1. Téléchargement
        if (!archive.exists() || archive.length() == 0L) {
            downloadFile(sourceUrl, archive, onProgress)
        }

        // 2. Extraction
        extractArchive(archive, targetDir)

        // 3. Nettoyage
        archive.delete()

        // 4. Préparation (DNS, resolv.conf, sources apt)
        prepareRootfs(targetDir, distroName)

        targetDir
    }

    private fun downloadFile(url: String, target: File, onProgress: (Int) -> Unit) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 30_000
        connection.readTimeout = 30_000
        connection.connect()

        val total = connection.contentLengthLong
        var downloaded = 0L

        connection.inputStream.use { input ->
            FileOutputStream(target).use { output ->
                val buffer = ByteArray(64 * 1024)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                    downloaded += read
                    if (total > 0) {
                        onProgress(((downloaded * 100) / total).toInt())
                    }
                }
            }
        }
        connection.disconnect()
    }

    private fun extractArchive(archive: File, targetDir: File) {
        val rawInput: InputStream = archive.inputStream().buffered()

        val decompressed: InputStream = when {
            archive.name.endsWith(".xz") -> XZInputStream(rawInput)
            archive.name.endsWith(".gz") -> GZIPInputStream(rawInput)
            else -> rawInput
        }

        decompressed.use { input ->
            extractTar(input, targetDir)
        }
    }

    /**
     * Extrait un flux tar vers targetDir.
     * Implémentation minimaliste : gère fichiers et dossiers.
     */
    private fun extractTar(input: InputStream, targetDir: File) {
        val buffer = ByteArray(512)
        var bytesRead: Int

        while (input.read(buffer).also { bytesRead = it } == 512) {

            // Fin de l'archive : deux blocs de zéros
            if (buffer.all { it == 0.toByte() }) break

            val name = String(buffer, 0, 100, Charsets.UTF_8)
                .trimEnd('\u0000')
            if (name.isEmpty()) continue

            val sizeOctal = String(buffer, 124, 12, Charsets.UTF_8)
                .trimEnd('\u0000', ' ')
                .trim()
            val size = try {
                if (sizeOctal.isEmpty()) 0L
                else sizeOctal.toLong(8)
            } catch (e: NumberFormatException) { 0L }

            val typeFlag = buffer[156].toInt().toChar()

            val outFile = File(targetDir, name)

            when (typeFlag) {
                '5' -> outFile.mkdirs()
                '0', '\u0000' -> {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { out ->
                        var remaining = size
                        val chunk = ByteArray(64 * 1024)
                        while (remaining > 0) {
                            val toRead = minOf(remaining, chunk.size.toLong()).toInt()
                            val n = input.read(chunk, 0, toRead)
                            if (n <= 0) break
                            out.write(chunk, 0, n)
                            remaining -= n
                        }
                    }
                }
            }

            // Avancer au bloc suivant (arrondi à 512)
            val padding = (512 - (size % 512)) % 512
            if (padding > 0) {
                var skipped = 0L
                while (skipped < padding) {
                    val n = input.skip(padding - skipped)
                    if (n <= 0) break
                    skipped += n
                }
            }
        }
    }

    private fun prepareRootfs(rootfs: File, distro: String) {

        // resolv.conf : DNS Google + Cloudflare
        val resolv = File(rootfs, "etc/resolv.conf")
        resolv.parentFile?.mkdirs()
        resolv.writeText(
            """
            nameserver 1.1.1.1
            nameserver 8.8.8.8
            nameserver 9.9.9.9
            """.trimIndent()
        )

        // sources.list pour Kali
        if (distro == "kali") {
            val sources = File(rootfs, "etc/apt/sources.list")
            sources.parentFile?.mkdirs()
            sources.writeText(
                "deb http://http.kali.org/kali kali-rolling main contrib non-free non-free-firmware\n"
            )
        }

        // Hostname
        File(rootfs, "etc/hostname").writeText("void-linux\n")

        // Hosts
        File(rootfs, "etc/hosts").writeText(
            """
            127.0.0.1   localhost
            127.0.1.1   void-linux
            """.trimIndent()
        )
    }

    fun isRootfsInstalled(distroName: String): Boolean {
        val dir = File(host.rootfsDir, distroName)
        return dir.exists() && File(dir, "bin").exists()
    }
}