@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.kotlinfoundation.kmpstarterkit.example

import com.kotlinfoundation.kmpstarterkit.util.UiStateHolder
import com.kotlinfoundation.kmpstarterkit.util.uiStateHolderScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Demonstrates how to test a [UiStateHolder] that exposes a `StateFlow`.
 *
 * Because `UiStateHolder` extends `androidx.lifecycle.ViewModel`, its `viewModelScope`
 * runs on `Dispatchers.Main`. Tests must override Main with a `TestDispatcher` via
 * `Dispatchers.setMain(...)` and reset it after each test.
 *
 * Flow assertions use plain `kotlinx-coroutines-test` — collect emissions into a list
 * via a launched coroutine on an `UnconfinedTestDispatcher`, then advance the scheduler
 * until idle. No external library (e.g. Turbine) is needed.
 */
class SampleUiStateHolderTest {
    private val mainDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `incrementing emits initial state then updated state`() = runTest {
        val holder = SampleCounterUiStateHolder()
        val emissions = mutableListOf<SampleCounterUiState>()
        val collectorJob =
            launch(UnconfinedTestDispatcher(testScheduler)) {
                holder.uiState.toList(emissions)
            }

        holder.onUiEvent(SampleCounterUiEvent.Increment)
        holder.onUiEvent(SampleCounterUiEvent.Increment)
        advanceUntilIdle()

        assertEquals(SampleCounterUiState(count = 0), emissions.first())
        assertEquals(SampleCounterUiState(count = 2), emissions.last())
        collectorJob.cancel()
    }

    private data class SampleCounterUiState(val count: Int = 0)

    private sealed interface SampleCounterUiEvent {
        data object Increment : SampleCounterUiEvent
    }

    private class SampleCounterUiStateHolder : UiStateHolder() {
        private val _uiState = MutableStateFlow(SampleCounterUiState())
        val uiState: StateFlow<SampleCounterUiState> = _uiState.asStateFlow()

        @Suppress("unused")
        private val keepScope = uiStateHolderScope // touch the field so the import isn't pruned

        fun onUiEvent(event: SampleCounterUiEvent) {
            when (event) {
                SampleCounterUiEvent.Increment ->
                    _uiState.update { it.copy(count = it.count + 1) }
            }
        }
    }
}
