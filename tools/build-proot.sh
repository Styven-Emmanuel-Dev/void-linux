#!/bin/bash
# Compile proot pour Android.
# Prérequis : NDK r25+, sources proot clonées.

set -e

ABI="${1:-arm64-v8a}"
NDK_PATH="${ANDROID_NDK_HOME:-$HOME/Android/Sdk/ndk/25.2.9519653}"
OUTPUT_DIR="library/proot-engine/proot-engine/src/main/jniLibs/$ABI"

echo "[*] Compilation de proot pour $ABI"

if [ ! -d "../proot" ]; then
    echo "[!] Clone d'abord proot :"
    echo "    git clone https://github.com/proot-me/proot.git"
    exit 1
fi

cd ../proot/src

# Configure le toolchain selon l'ABI
case "$ABI" in
    arm64-v8a)
        TARGET="aarch64-linux-android"
        API=29
        ;;
    armeabi-v7a)
        TARGET="armv7a-linux-androideabi"
        API=29
        ;;
    x86_64)
        TARGET="x86_64-linux-android"
        API=29
        ;;
    *)
        echo "[!] ABI inconnu : $ABI"
        exit 1
        ;;
esac

TOOLCHAIN="$NDK_PATH/toolchains/llvm/prebuilt/linux-x86_64"
export CC="$TOOLCHAIN/bin/${TARGET}${API}-clang"
export AR="$TOOLCHAIN/bin/llvm-ar"
export STRIP="$TOOLCHAIN/bin/llvm-strip"

make clean || true
make -j$(nproc) LDFLAGS="-static"

# Copie le résultat
mkdir -p "../../Void-Linux/$OUTPUT_DIR"
cp src/proot "../../Void-Linux/$OUTPUT_DIR/libproot.so"
cp src/loader/loader "../../Void-Linux/$OUTPUT_DIR/libproot_loader.so" 2>/dev/null || true

cd ../../Void-Linux
echo "[✓] proot compilé dans $OUTPUT_DIR"