/*
 * terminal_jni.c
 * Pont JNI pour l'exécution de commandes dans le PTY.
 */

#include <jni.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/wait.h>
#include <android/log.h>

#define LOG_TAG "VoidTermJni"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

JNIEXPORT jint JNICALL
Java_com_voidlinux_core_native_NativeBridge_execInPty(JNIEnv *env, jclass clazz,
                                                     jint master_fd, jstring command) {
    const char *cmd = (*env)->GetStringUTFChars(env, command, NULL);
    if (!cmd) return -1;

    pid_t pid = fork();
    if (pid < 0) {
        (*env)->ReleaseStringUTFChars(env, command, cmd);
        return -1;
    }

    if (pid == 0) {
        // Enfant : attacher au slave
        setsid();
        dup2(master_fd, STDIN_FILENO);
        dup2(master_fd, STDOUT_FILENO);
        dup2(master_fd, STDERR_FILENO);

        execl("/system/bin/sh", "sh", "-c", cmd, NULL);
        _exit(127);
    }

    (*env)->ReleaseStringUTFChars(env, command, cmd);
    LOGI("execInPty pid=%d", pid);
    return (jint) pid;
}

JNIEXPORT jint JNICALL
Java_com_voidlinux_core_native_NativeBridge_writeToPty(JNIEnv *env, jclass clazz,
                                                      jint fd, jbyteArray data) {
    jsize len = (*env)->GetArrayLength(env, data);
    jbyte *bytes = (*env)->GetByteArrayElements(env, data, NULL);
    if (!bytes) return -1;

    ssize_t written = write(fd, bytes, len);
    (*env)->ReleaseByteArrayElements(env, data, bytes, JNI_ABORT);
    return (jint) written;
}

JNIEXPORT jbyteArray JNICALL
Java_com_voidlinux_core_native_NativeBridge_readFromPty(JNIEnv *env, jclass clazz,
                                                       jint fd, jint max_bytes) {
    char *buffer = malloc(max_bytes);
    if (!buffer) return NULL;

    ssize_t n = read(fd, buffer, max_bytes);
    if (n <= 0) {
        free(buffer);
        return NULL;
    }

    jbyteArray result = (*env)->NewByteArray(env, n);
    (*env)->SetByteArrayRegion(env, result, 0, n, (jbyte *) buffer);
    free(buffer);
    return result;
}