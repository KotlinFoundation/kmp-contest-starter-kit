package com.kotlinfoundation.koko.example

import com.kotlinfoundation.koko.data.BackgroundExecutor
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Demonstrates how to test a `suspend` function whose async behavior is parameterized
 * by a [kotlin.coroutines.CoroutineContext]. Inject a `StandardTestDispatcher` tied to
 * the surrounding `runTest` scheduler so dispatching is deterministic.
 *
 * NOTE: this only covers the happy path. Exception-wrapping behavior of
 * [BackgroundExecutor.execute] calls into `AppLogger` (Koin-backed), which would
 * require a running Koin context to test cleanly. Wire that up alongside the first
 * real repository test that needs it.
 */
class BackgroundExecutorTest {
    @Test
    fun `execute returns success result from happy-path block`() = runTest {
        val executor = BackgroundExecutor(StandardTestDispatcher(testScheduler))

        val result = executor.execute { Result.success(42) }

        assertTrue(result.isSuccess)
        assertEquals(42, result.getOrNull())
    }
}
