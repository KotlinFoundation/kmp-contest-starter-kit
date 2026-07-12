@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.kotlinfoundation.koko.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * Demonstrates how to test a `ViewModel` that exposes a `StateFlow`.
 *
 * Because a `ViewModel`'s `viewModelScope` runs on `Dispatchers.Main`, tests must
 * override Main with a `TestDispatcher` via
 * `Dispatchers.setMain(...)` and reset it after each test.
 *
 * Flow assertions use plain `kotlinx-coroutines-test` — collect emissions into a list
 * via a launched coroutine on an `UnconfinedTestDispatcher`, then advance the scheduler
 * until idle. No external library (e.g. Turbine) is needed.
 */
class SampleViewModelTest {
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
        val viewModel = SampleCounterViewModel()
        val emissions = mutableListOf<SampleCounterUiState>()
        val collectorJob =
            launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.toList(emissions)
            }

        viewModel.onUiEvent(SampleCounterUiEvent.Increment)
        viewModel.onUiEvent(SampleCounterUiEvent.Increment)
        advanceUntilIdle()

        assertEquals(SampleCounterUiState(count = 0), emissions.first())
        assertEquals(SampleCounterUiState(count = 2), emissions.last())
        collectorJob.cancel()
    }

    private data class SampleCounterUiState(val count: Int = 0)

    private sealed interface SampleCounterUiEvent {
        data object Increment : SampleCounterUiEvent
    }

    private class SampleCounterViewModel : ViewModel() {
        private val _uiState = MutableStateFlow(SampleCounterUiState())
        val uiState: StateFlow<SampleCounterUiState> = _uiState.asStateFlow()

        @Suppress("unused")
        private val keepScope = viewModelScope // touch the field so the import isn't pruned

        fun onUiEvent(event: SampleCounterUiEvent) {
            when (event) {
                SampleCounterUiEvent.Increment ->
                    _uiState.update { it.copy(count = it.count + 1) }
            }
        }
    }
}
