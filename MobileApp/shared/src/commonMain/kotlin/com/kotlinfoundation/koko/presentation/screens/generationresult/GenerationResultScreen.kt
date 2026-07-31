package com.kotlinfoundation.koko.presentation.screens.generationresult

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kotlinfoundation.koko.designsystem.components.AsyncImageWithShimmer
import com.kotlinfoundation.koko.designsystem.components.CircularActionButton
import com.kotlinfoundation.koko.designsystem.components.LoadingProgress
import com.kotlinfoundation.koko.designsystem.components.LoadingProgressMode
import com.kotlinfoundation.koko.designsystem.components.ScreenWithToolbar
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_back
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_download
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_share
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_warning
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.domain.model.generation.GenerationOutput
import com.kotlinfoundation.koko.generated.resources.Res
import com.kotlinfoundation.koko.generated.resources.btn_action_report
import com.kotlinfoundation.koko.generated.resources.btn_action_save
import com.kotlinfoundation.koko.generated.resources.btn_action_share
import com.kotlinfoundation.koko.generated.resources.title_screen_generation_result
import com.kotlinfoundation.koko.presentation.components.ReportDialog
import org.jetbrains.compose.resources.stringResource

@Composable
fun GenerationResultScreen(
    modifier: Modifier = Modifier,
    viewModel: GenerationResultViewModel,
    onNavigateToBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GenerationResultScreen(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onNavigateToBack = onNavigateToBack,
    )
}

@Composable
fun GenerationResultScreen(
    modifier: Modifier = Modifier,
    uiState: GenerationResultUiState,
    onUiEvent: (GenerationResultUiEvent) -> Unit,
    onNavigateToBack: () -> Unit,
) {
    ScreenWithToolbar(
        modifier = modifier,
        navigationIcon = UiRes.drawable.ic_back,
        onNavigationIconClick = {
            onNavigateToBack()
        },
        isScrollableContent = false, // Set to false if content itself has scrollable content such as LazyColumn
        title = stringResource(Res.string.title_screen_generation_result),
        includeBottomInsets = true, // Set to true if bottom nav is not visible
    ) {
        if (uiState.isReportDialogVisible) {
            ReportDialog(
                onDismiss = {
                    onUiEvent(GenerationResultUiEvent.OnDismissReportDialog)
                },
                onSubmitReport = {
                    onUiEvent(GenerationResultUiEvent.OnSubmitReport(it))
                },
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing)) {
            when {
                uiState.isLoading -> LoadingProgress(mode = LoadingProgressMode.FULLSCREEN)

                !uiState.isLoading && uiState.generatedOutput != null -> {
                    GenerationResult(
                        generationOutput = uiState.generatedOutput,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .align(Alignment.CenterHorizontally),
                    )

                    ActionButtons(
                        onUiEvent = onUiEvent,
                        isDownloadInProgress = uiState.isSaveToGalleryInProgress,
                    )
                }

                else -> Unit
            }
        }
    }
}

@Composable
private fun GenerationResult(
    generationOutput: GenerationOutput,
    modifier: Modifier = Modifier,
) {
    generationOutput.output?.let { imageSrc ->
        AsyncImageWithShimmer(
            model = imageSrc,
            modifier = modifier,
            contentDescription = null,
        )
    }
}

@Composable
private fun ActionButtons(
    modifier: Modifier = Modifier,
    isDownloadInProgress: Boolean = false,
    onUiEvent: (GenerationResultUiEvent) -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        CircularActionButton(
            icon = UiRes.drawable.ic_share,
            text = stringResource(Res.string.btn_action_share),
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                onUiEvent(GenerationResultUiEvent.OnClickShare)
            },
        )

        CircularActionButton(
            icon = UiRes.drawable.ic_download,
            isLoading = isDownloadInProgress,
            text = stringResource(Res.string.btn_action_save),
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                onUiEvent(GenerationResultUiEvent.OnClickDownload)
            },
        )

        CircularActionButton(
            icon = UiRes.drawable.ic_warning,
            text = stringResource(Res.string.btn_action_report),
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
                onUiEvent(GenerationResultUiEvent.OnClickReport)
            },
        )
    }
}

@Preview
@Composable
private fun GenerationResultScreenPreview() {
    AppTheme {
        GenerationResultScreen(
            uiState = GenerationResultUiState(),
            onUiEvent = {},
            onNavigateToBack = {},
        )
    }
}
