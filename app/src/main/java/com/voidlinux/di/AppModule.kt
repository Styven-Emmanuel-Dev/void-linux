package com.voidlinux.di

import android.content.Context
import com.voidlinux.feature.linux.LinuxRepository
import com.voidlinux.feature.security.ThreatDetector
import com.voidlinux.feature.tor.TorManager
import com.voidlinux.feature.windows.WindowsRepository

/**
 * Conteneur d'injection de dépendances simple.
 * Instancié une fois dans VoidApplication.
 */
class AppModule private constructor(context: Context) {

    val appContext: Context = context.applicationContext

    val linuxRepository: LinuxRepository by lazy {
        LinuxRepository(appContext)
    }

    val windowsRepository: WindowsRepository by lazy {
        WindowsRepository(appContext)
    }

    val torManager: TorManager by lazy {
        TorManager(appContext)
    }

    val threatDetector: ThreatDetector by lazy {
        ThreatDetector(appContext)
    }

    companion object {
        @Volatile
        private var instance: AppModule? = null

        fun init(context: Context): AppModule {
            return instance ?: synchronized(this) {
                instance ?: AppModule(context).also { instance = it }
            }
        }

        fun get(): AppModule =
            instance ?: error("AppModule non initialisé — appelle init() dans VoidApplication")

        fun isInitialized(): Boolean = instance != null
    }
}