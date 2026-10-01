/*
 * pty_helper.c
 * Crée un pseudo-terminal (PTY) pour le terminal intégré.
 */

#include <jni.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <pty.h>
#include <termios.h>
#include <sys/ioctl.h>
#include <android/log.h>

#define LOG_TAG "VoidPty"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

JNIEXPORT jintArray JNICALL
Java_com_voidlinux_core_native_NativeBridge_createPty(JNIEnv *env, jclass clazz,
                                                     jint cols, jint rows) {
    int master, slave;
    struct winsize ws;

    ws.ws_col = (unsigned short) cols;
    ws.ws_row = (unsigned short) rows;
    ws.ws_xpixel = 0;
    ws.ws_ypixel = 0;

    if (openpty(&master, &slave, NULL, NULL, &ws) < 0) {
        LOGI("openpty échoué");
        return NULL;
    }

    // Raw mode sur le slave
    struct termios tios;
    tcgetattr(slave, &tios);
    cfmakeraw(&tios);
    tcsetattr(slave, TCSANOW, &tios);

    jintArray result = (*env)->NewIntArray(env, 2);
    jint values[2] = { master, slave };
    (*env)->SetIntArrayRegion(env, result, 0, 2, values);

    LOGI("PTY créé master=%d slave=%d", master, slave);
    return result;
}

JNIEXPORT void JNICALL
Java_com_voidlinux_core_native_NativeBridge_resizePty(JNIEnv *env, jclass clazz,
                                                     jint fd, jint cols, jint rows) {
    struct winsize ws;
    ws.ws_col = (unsigned short) cols;
    ws.ws_row = (unsigned short) rows;
    ioctl(fd, TIOCSWINSZ, &ws);
}

JNIEXPORT void JNICALL
Java_com_voidlinux_core_native_NativeBridge_closePty(JNIEnv *env, jclass clazz, jint fd) {
    if (fd >= 0) close(fd);
}