#!/bin/bash
# Télécharge le rootfs Kali NetHunter.

set -e

ARCH="${1:-arm64}"
OUTPUT_DIR="${2:-app/src/main/assets}"

case "$ARCH" in
    arm64)
        URL="https://kali.download/nethunter-images/current/rootfs/kali-nethunter-rootfs-minimal-arm64.tar.xz"
        FILENAME="kali-arm64.tar.xz"
        ;;
    armhf)
        URL="https://kali.download/nethunter-images/current/rootfs/kali-nethunter-rootfs-minimal-armhf.tar.xz"
        FILENAME="kali-armhf.tar.xz"
        ;;
    *)
        echo "[!] Architecture inconnue : $ARCH"
        echo "[i] Utilise 'arm64' ou 'armhf'"
        exit 1
        ;;
esac

mkdir -p "$OUTPUT_DIR"

if [ -f "$OUTPUT_DIR/$FILENAME" ]; then
    echo "[i] $FILENAME existe déjà, skip."
    exit 0
fi

echo "[*] Téléchargement de $FILENAME depuis Kali…"
if curl -L --fail --retry 3 -o "$OUTPUT_DIR/$FILENAME" "$URL"; then
    echo "[✓] Rootfs téléchargé : $OUTPUT_DIR/$FILENAME"
    echo "[i] Taille : $(du -h "$OUTPUT_DIR/$FILENAME" | cut -f1)"
else
    echo "[!] Échec du téléchargement — le build continuera sans rootfs."
    exit 0
fi