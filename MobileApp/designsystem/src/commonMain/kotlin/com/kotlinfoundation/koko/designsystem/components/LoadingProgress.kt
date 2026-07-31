package com.kotlinfoundation.koko.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed interface LoadingProgressMode {
    data object CIRCULAR : LoadingProgressMode

    data class OVERLAY(
        val title: String = "Loading is in progress...",
        val text: String = "",
    ) : LoadingProgressMode

    data object FULLSCREEN : LoadingProgressMode
}

@Composable
fun LoadingProgress(mode: LoadingProgressMode = LoadingProgressMode.CIRCULAR) {
    when (mode) {
        LoadingProgressMode.CIRCULAR -> CircularLoadingBar()

        is LoadingProgressMode.OVERLAY ->
            LoadingProgressOverlay(
                title = mode.title,
                text = mode.text,
            )

        LoadingProgressMode.FULLSCREEN -> FullScreenLoading()
    }
}

@Composable
private fun CircularLoadingBar() {
    CircularProgressIndicator(color = AppTheme.colors.primary)
}

@Composable
private fun FullScreenLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularLoadingBar()
    }
}

// Shows modal dialog with a loading progress bar
@Composable
private fun LoadingProgressOverlay(
    title: String = "Loading is in progress...",
    text: String = "",
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.8f),
            colors =
            CardDefaults.cardColors(
                containerColor = AppTheme.colors.surfaceContainer,
                contentColor = AppTheme.colors.text.primary,
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.spacing.dialogContentSpacing)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
            ) {
                CircularLoadingBar()
                Text(
                    text = title,
                    style = AppTheme.typography.h5,
                    color = AppTheme.colors.text.primary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                )

                if (text.isNotEmpty()) {
                    Text(
                        text = text,
                        style = AppTheme.typography.bodyExtraLarge,
                        color = AppTheme.colors.text.secondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
internal fun LoadingProgressPreview() {
    var isDialogVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    PreviewHelper {
        CircularLoadingBar()
        AppButton("Show Progress Overlay", onClick = { isDialogVisible = true })
        if (isDialogVisible) {
            LoadingProgressOverlay()
            coroutineScope.launch {
                delay(2000)
                isDialogVisible = false
            }
        }
    }
}
