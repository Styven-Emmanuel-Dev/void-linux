#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <android/log.h>
#include <errno.h>
#include <sys/syscall.h>

#define LOG_TAG "VoidProotLoader"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#ifndef SYS_memfd_create
#define SYS_memfd_create __NR_memfd_create
#endif

static int create_memfd(const char *name) {
    return (int) syscall(SYS_memfd_create, name, 0);
}

static int load_elf_to_memfd(const char *path) {
    int fd = open(path, O_RDONLY);
    if (fd < 0) {
        LOGE("open(%s) : %s", path, strerror(errno));
        return -1;
    }

    struct stat st;
    if (fstat(fd, &st) < 0) {
        LOGE("fstat : %s", strerror(errno));
        close(fd);
        return -1;
    }

    size_t size = (size_t) st.st_size;
    void *mem = mmap(NULL, size, PROT_READ | PROT_WRITE,
                     MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    if (mem == MAP_FAILED) {
        LOGE("mmap : %s", strerror(errno));
        close(fd);
        return -1;
    }

    ssize_t read_bytes = read(fd, mem, size);
    close(fd);
    if (read_bytes != (ssize_t) size) {
        LOGE("read incomplet");
        munmap(mem, size);
        return -1;
    }

    int memfd = create_memfd("proot_loader");
    if (memfd < 0) {
        LOGE("memfd_create : %s", strerror(errno));
        munmap(mem, size);
        return -1;
    }

    if (write(memfd, mem, size) != (ssize_t) size) {
        LOGE("write memfd : %s", strerror(errno));
        close(memfd);
        munmap(mem, size);
        return -1;
    }

    munmap(mem, size);
    lseek(memfd, 0, SEEK_SET);
    return memfd;
}

JNIEXPORT jint JNICALL
Java_com_voidlinux_core_native_NativeBridge_loadElf(
        JNIEnv *env, jclass clazz, jstring path) {
    const char *cpath = (*env)->GetStringUTFChars(env, path, NULL);
    if (!cpath) return -1;

    int fd = load_elf_to_memfd(cpath);

    (*env)->ReleaseStringUTFChars(env, path, cpath);
    LOGI("loadElf -> fd=%d", fd);
    return fd;
}

JNIEXPORT jint JNICALL
Java_com_voidlinux_core_native_NativeBridge_checkWxSupported(
        JNIEnv *env, jclass clazz) {
    return 1;
}