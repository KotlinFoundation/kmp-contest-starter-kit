package com.kotlinfoundation.koko.root

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kotlinfoundation.koko.designsystem.AllComponentsGallery
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.presentation.navigation.AppNavigation
import com.kotlinfoundation.koko.util.UiMessage
import com.kotlinfoundation.koko.util.extensions.ObserveFlowAsEvent
import com.kotlinfoundation.koko.util.logging.AppLogger
import com.kotlinfoundation.koko.util.logging.logAppOpened
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/** Root composable — the single entry point every platform (Android/iOS/Desktop/Web) renders. */
@Composable
fun App() {
    AppTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Set this to true for showing app, or false for ui components gallery
            val isPreviewComponentsMode = false
            if (isPreviewComponentsMode) {
                AllComponentsGallery()
            } else {
                AppScaffold()
            }
        }
    }
}

@Composable
private fun AppScaffold() {
    val snackbarHostState = remember { SnackbarHostState() }
    var uiMessage by remember { mutableStateOf<UiMessage?>(null) }

    ObserveFlowAsEvent(AppGlobalUiState.uiMessageFlow) { uiMessage = it }
    LaunchedEffect(Unit) {
        AppLogger.logAppOpened()
    }

    uiMessage?.value?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message = message)
            uiMessage = null
        }
    }
    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) {
        AppNavigation()
    }
}

/**
 * App-wide bus for one-off UI messages (snackbars). Lets any layer surface a transient message
 * without holding a screen reference — [AppScaffold] collects [uiMessageFlow] and shows it.
 */
object AppGlobalUiState {
    private val uiMessageChannel = Channel<UiMessage>(Channel.BUFFERED)
    val uiMessageFlow = uiMessageChannel.receiveAsFlow()

    fun showUiMessage(uiMessage: UiMessage) {
        uiMessageChannel.trySend(uiMessage)
    }
}
