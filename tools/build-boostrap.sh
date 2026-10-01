#!/bin/bash
# Compile un bootstrap Termux pour Void-Linux.
# Prérequis : termux-packages cloné, Docker ou environnement Linux ARM64.

set -e

ABI="${1:-aarch64}"
OUTPUT_DIR="library/termux-bootstrap/src/main/assets"

echo "[*] Compilation du bootstrap Termux pour $ABI"

# Vérifie que termux-packages est cloné
if [ ! -d "../termux-packages" ]; then
    echo "[!] Clone d'abord termux-packages :"
    echo "    git clone https://github.com/termux/termux-packages.git"
    exit 1
fi

cd ../termux-packages

# Compile le bootstrap
./scripts/build-bootstraps.sh --architectures "$ABI"

# Copie le résultat
BOOTSTRAP_FILE="bootstrap-$ABI.zip"
if [ -f "$BOOTSTRAP_FILE" ]; then
    cp "$BOOTSTRAP_FILE" "../Void-Linux/$OUTPUT_DIR/"
    echo "[✓] Bootstrap copié dans $OUTPUT_DIR/$BOOTSTRAP_FILE"
else
    echo "[!] Fichier $BOOTSTRAP_FILE introuvable"
    exit 1
fi

cd ../Void-Linux
echo "[✓] Terminé"