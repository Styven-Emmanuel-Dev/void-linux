cmake_minimum_required(VERSION 3.22.1)
project(voidlinux_native C CXX)

# Bibliothèque JNI principale
add_library(voidlinux_jni SHARED
    src/main/cpp/proot_loader.c
    src/main/cpp/terminal_jni.c
    src/main/cpp/pty_helper.c
)

target_include_directories(voidlinux_jni PRIVATE
    src/main/cpp
)

target_link_libraries(voidlinux_jni
    android
    log
    dl
)