package com.kotlinfoundation.koko.util

import com.kotlinfoundation.koko.util.logging.AppLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * App-lifetime coroutine scope for work that must outlive any screen (sync, analytics, caches).
 * SupervisorJob so one failed child doesn't cancel the rest; uncaught errors are logged.
 */
class ApplicationScope : CoroutineScope {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        AppLogger.e("Unknown exception in application scope", throwable)
    }

    override val coroutineContext: CoroutineContext
        get() = CoroutineScope(SupervisorJob() + defaultAsyncDispatcher + coroutineExceptionHandler).coroutineContext
}
