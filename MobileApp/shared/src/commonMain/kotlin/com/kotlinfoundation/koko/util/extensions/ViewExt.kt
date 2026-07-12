package com.kotlinfoundation.koko.util.extensions

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Collects [flow] as one-off events while the screen is STARTED (lifecycle-aware, on the main
 * dispatcher). Use for transient signals — snackbars, navigation — not for rendered state.
 */
@Composable
fun <T> ObserveFlowAsEvent(flow: Flow<T>, onEvent: (T) -> Unit) {
    val lifecycle = LocalLifecycleOwner.current
    val updatedOnEvent by rememberUpdatedState(onEvent)
    LaunchedEffect(flow, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                flow.collect(updatedOnEvent)
            }
        }
    }
}

/** Heuristic: true when the soft keyboard (IME) is showing, based on its bottom inset. */
@Composable
fun isKeyboardOpen(): Boolean {
    val bottomInset = WindowInsets.ime.getBottom(LocalDensity.current)
    return rememberUpdatedState(bottomInset > 300).value
}
