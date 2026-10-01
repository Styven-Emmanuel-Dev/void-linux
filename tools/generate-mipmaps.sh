#!/bin/bash
# Génère tous les dossiers mipmap-* depuis l'icône source.
# Source : app/src/main/res/mipmap-xxxhdpi/ic_launcher.png

set -e

SRC_DIR="app/src/main/res"
SRC_ICON="$SRC_DIR/mipmap-xxxhdpi/ic_launcher.png"
SRC_ROUND="$SRC_DIR/mipmap-xxxhdpi/ic_launcher_round.png"

if [ ! -f "$SRC_ICON" ]; then
    echo "[!] Icône source introuvable : $SRC_ICON"
    exit 1
fi

# Vérifie ImageMagick
if ! command -v convert &> /dev/null; then
    echo "[*] Installation d'ImageMagick..."
    sudo apt-get update -qq
    sudo apt-get install -y -qq imagemagick
fi

# Dimensions pour chaque densité
declare -A SIZES=(
    ["mdpi"]=48
    ["hdpi"]=72
    ["xhdpi"]=96
    ["xxhdpi"]=144
    ["xxxhdpi"]=192
)

# Icône carrée
for density in "${!SIZES[@]}"; do
    size="${SIZES[$density]}"
    out_dir="$SRC_DIR/mipmap-$density"
    mkdir -p "$out_dir"

    convert "$SRC_ICON" -resize "${size}x${size}" "$out_dir/ic_launcher.png"
    echo "[✓] mipmap-$density/ic_launcher.png ($size x $size)"
done

# Icône ronde
if [ -f "$SRC_ROUND" ]; then
    for density in "${!SIZES[@]}"; do
        size="${SIZES[$density]}"
        out_dir="$SRC_DIR/mipmap-$density"
        mkdir -p "$out_dir"

        convert "$SRC_ROUND" -resize "${size}x${size}" "$out_dir/ic_launcher_round.png"
        echo "[✓] mipmap-$density/ic_launcher_round.png ($size x $size)"
    done
else
    echo "[i] Pas d'icône ronde source, copie de ic_launcher.png"
    for density in "${!SIZES[@]}"; do
        cp "$SRC_DIR/mipmap-$density/ic_launcher.png" \
           "$SRC_DIR/mipmap-$density/ic_launcher_round.png"
    done
fi

# Icône adaptative (foreground + background)
declare -A ADAPTIVE_SIZES=(
    ["mdpi"]=108
    ["hdpi"]=162
    ["xhdpi"]=216
    ["xxhdpi"]=324
    ["xxxhdpi"]=432
)

for density in "${!ADAPTIVE_SIZES[@]}"; do
    size="${ADAPTIVE_SIZES[$density]}"
    out_dir="$SRC_DIR/mipmap-$density"
    mkdir -p "$out_dir"

    # Foreground : icône source redimensionnée avec marge (66% de la taille)
    convert "$SRC_ICON" -resize "$((size * 66 / 100))x$((size * 66 / 100))" \
        -background none -gravity center -extent "${size}x${size}" \
        "$out_dir/ic_launcher_foreground.png"

    # Background : fond noir Void-Linux
    convert -size "${size}x${size}" xc:"#050508" \
        "$out_dir/ic_launcher_background.png"

    echo "[✓] mipmap-$density foreground + background ($size x $size)"
done

# Créer les XML adaptatifs
mkdir -p "$SRC_DIR/mipmap-anydpi-v26"

cat > "$SRC_DIR/mipmap-anydpi-v26/ic_launcher.xml" << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@mipmap/ic_launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
    <monochrome android:drawable="@mipmap/ic_launcher_foreground" />
</adaptive-icon>
EOF

cat > "$SRC_DIR/mipmap-anydpi-v26/ic_launcher_round.xml" << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@mipmap/ic_launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
    <monochrome android:drawable="@mipmap/ic_launcher_foreground" />
</adaptive-icon>
EOF

echo ""
echo "[✓] Toutes les icônes mipmap générées avec succès"
echo "[i] Dossiers créés dans : $SRC_DIR"