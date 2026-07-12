package com.kotlinfoundation.koko.data

import com.kotlinfoundation.koko.util.defaultAsyncDispatcher
import com.kotlinfoundation.koko.util.logging.AppLogger
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * Runs repository/data work off the main thread on [scope] and turns any thrown exception into
 * a `Result.failure` (logged) — so repositories return `Result` without writing their own
 * try/catch. `CancellationException` is rethrown so coroutine cancellation still works.
 */
class BackgroundExecutor(val scope: CoroutineContext = defaultAsyncDispatcher) {

    companion object {
        // Shared IO-dispatcher instance for repositories that don't inject their own.
        val IO by lazy { BackgroundExecutor(defaultAsyncDispatcher) }
    }

    suspend fun <T> execute(
        func: suspend () -> Result<T>,
    ): Result<T> = withContext(scope) {
        try {
            func.invoke()
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            } else {
                AppLogger.e("Error while executing background task: ${e.message}")
                Result.failure(e)
            }
        }
    }
}
