package com.kotlinfoundation.koko.util

import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.module.Module

/** The platform the app is currently running on. */
sealed interface Platform {
    data object Android : Platform
    data object Ios : Platform
    data object Desktop : Platform
    data object Web : Platform
}

expect fun getPlatform(): Platform

// Per-platform hooks resolved via expect/actual: the platform's Koin module, app-start side
// effects, environment flags, and the default background dispatcher.
internal expect val platformModule: Module
internal expect fun onApplicationStartPlatformSpecific()
internal expect val isAndroid: Boolean
internal expect val isDebug: Boolean

expect val defaultAsyncDispatcher: CoroutineDispatcher
