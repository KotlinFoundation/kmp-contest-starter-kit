package com.kotlinfoundation.koko.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kotlinfoundation.koko.designsystem.components.AppButton
import com.kotlinfoundation.koko.designsystem.components.Chip
import com.kotlinfoundation.koko.designsystem.components.ChipSize
import com.kotlinfoundation.koko.designsystem.components.ChipStyle
import com.kotlinfoundation.koko.designsystem.components.ConfettiParticlesAnimated
import com.kotlinfoundation.koko.designsystem.components.LoadingProgress
import com.kotlinfoundation.koko.designsystem.components.LoadingProgressMode
import com.kotlinfoundation.koko.designsystem.components.ScreenWithToolbar
import com.kotlinfoundation.koko.designsystem.components.UserInput
import com.kotlinfoundation.koko.designsystem.components.addorchosefilecontainer.AddOrChooseFileContainer
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_coin_credits
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_sparkles
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.domain.model.generation.GenerationOutput
import com.kotlinfoundation.koko.generated.resources.Res
import com.kotlinfoundation.koko.generated.resources.btn_generate
import com.kotlinfoundation.koko.generated.resources.home_loading_text
import com.kotlinfoundation.koko.generated.resources.home_loading_title
import com.kotlinfoundation.koko.generated.resources.home_prompt_label
import com.kotlinfoundation.koko.generated.resources.home_prompt_placeholder
import com.kotlinfoundation.koko.generated.resources.home_reference_image_label
import com.kotlinfoundation.koko.generated.resources.title_screen_home
import com.kotlinfoundation.koko.root.AppConfiguration
import com.kotlinfoundation.koko.util.StoreDevice
import com.kotlinfoundation.koko.util.StoreScreenshot
import com.kotlinfoundation.koko.util.file.isCameraCaptureSupported
import com.kotlinfoundation.koko.util.file.openCameraPicker
import com.kotlinfoundation.koko.util.inappreview.rememberInAppReviewTrigger
import com.kotlinfoundation.koko.util.logging.AppLogger
import com.kotlinfoundation.koko.util.permissions.RequestPermissionOnEntry
import com.kotlinfoundation.koko.util.permissions.rememberNotificationPermissionState
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onPremiumRequired: () -> Unit,
    onMoreCreditsNeeded: () -> Unit,
    onAuthRequired: () -> Unit,
    onGenerationResult: (GenerationOutput) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showConfetti by remember { mutableStateOf(false) }

    // Push notifications (FCM via kmpnotifier) need the runtime permission on Android 13+ / iOS.
    RequestPermissionOnEntry(
        rememberNotificationPermissionState(onResult = { hasPermission ->
            AppLogger.d("HasNotification Permission: $hasPermission")
        }),
    )

    LaunchedEffect(uiState.isPremiumRequired) {
        if (uiState.isPremiumRequired) {
            onPremiumRequired()
            viewModel.onPremiumRequiredHandled()
        }
    }

    LaunchedEffect(uiState.isMoreCreditsRequired) {
        if (uiState.isMoreCreditsRequired) {
            onMoreCreditsNeeded()
            viewModel.onMoreCreditsRequiredHandled()
        }
    }

    LaunchedEffect(uiState.isAuthRequired) {
        if (uiState.isAuthRequired) {
            onAuthRequired()
            viewModel.onAuthRequiredHandled()
        }
    }

    LaunchedEffect(uiState.generatedResult) {
        uiState.generatedResult?.let { generatedResult ->
            showConfetti = true
            delay(100)
            onGenerationResult(generatedResult)
            viewModel.onGenerationResultHandled()
            showConfetti = false
        }
    }

    val inAppReviewTrigger = rememberInAppReviewTrigger()
    LaunchedEffect(uiState.isGenerationInProgress) {
        if (uiState.isGenerationInProgress) {
            inAppReviewTrigger.triggerWhileGenerationIsInProgress()
        }
    }

    HomeScreen(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
    )

    AnimatedVisibility(visible = showConfetti, enter = scaleIn() + fadeIn(), exit = fadeOut()) {
        ConfettiParticlesAnimated(modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onUiEvent: (HomeUiEvent) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val hapticFeedback = LocalHapticFeedback.current

    if (uiState.isGenerationInProgress) {
        LoadingProgress(
            mode = LoadingProgressMode.OVERLAY(
                title = stringResource(Res.string.home_loading_title),
                text = stringResource(Res.string.home_loading_text),
            ),
        )
    }

    ScreenWithToolbar(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
        },
        isScrollableContent = false,
        title = stringResource(Res.string.title_screen_home),
        includeBottomInsets = false, // Set to true if bottom nav is not visible
        toolbarExtraContent = {
            // Credit chip only when premium features are enabled.
            if (AppConfiguration.PREMIUM_FEATURES_ENABLED) {
                Chip(
                    text = "${uiState.creditBalance}",
                    style = ChipStyle.FILLED_ALPHA,
                    size = ChipSize.SMALL,
                    startIconRes = UiRes.drawable.ic_coin_credits,
                    onClick = {
                        onUiEvent(HomeUiEvent.OnClickToolbarCredits)
                    },
                )
            }
        },
    ) {
        Column(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.ime),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
        ) {
            HomeMainContent(
                modifier = Modifier.weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = AppTheme.spacing.outerSpacing),
                uiState = uiState,
                onUiEvent = onUiEvent,
            )

            AppButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.btn_generate),
                startIcon = UiRes.drawable.ic_sparkles,
                enabled = uiState.isGenerationButtonEnabled,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                    onUiEvent(HomeUiEvent.OnClickGenerate)
                },
            )
        }
    }
}

// Concrete, hand-written demo form: a text prompt + an optional reference image. Swap these
// out for whatever inputs your app needs — there is intentionally no generic form engine here.
@Composable
private fun HomeMainContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onUiEvent: (HomeUiEvent) -> Unit,
) {
    val galleryLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image,
        mode = FileKitMode.Single,
    ) { file ->
        onUiEvent(HomeUiEvent.OnReferenceImageSelected(file))
    }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
    ) {
        // Reference image section (first): the picker, then the optional-hint caption.
        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacingSmall),
        ) {
            AddOrChooseFileContainer(
                modifier = Modifier.fillMaxWidth(),
                uiState = uiState.referenceImage,
                onClickCaptureOrRecord = {
                    coroutineScope.launch {
                        val file = FileKit.openCameraPicker()
                        onUiEvent(HomeUiEvent.OnReferenceImageSelected(file))
                    }
                },
                onClickSelectFromGallery = { galleryLauncher.launch() },
                onFileRemoved = { onUiEvent(HomeUiEvent.OnReferenceImageRemoved(it)) },
                isCaptureSupported = isCameraCaptureSupported(),
            )
            Text(
                text = stringResource(Res.string.home_reference_image_label),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
        }

        // Prompt section: a title above the text field.
        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacingSmall),
        ) {
            Text(
                text = stringResource(Res.string.home_prompt_label),
                style = AppTheme.typography.h6,
                color = AppTheme.colors.text.primary,
            )
            UserInput(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.prompt,
                onValueChange = { onUiEvent(HomeUiEvent.OnPromptChanged(it)) },
                label = stringResource(Res.string.home_prompt_placeholder),
                minLines = 3,
                maxLines = 6,
            )
        }
    }
}

// Storefront screenshots — rendered by `./scripts/generate_store_screenshots.sh`.
@Preview
@StoreScreenshot(device = StoreDevice.IPHONE_6_5, locale = "en", tag = "01-home")
@Composable
private fun HomeStoreScreenshot_iPhone_en() {
    AppTheme {
        HomeScreen(uiState = HomeUiState(creditBalance = 12), onUiEvent = {})
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    AppTheme {
        HomeScreen(uiState = HomeUiState(creditBalance = 12), onUiEvent = {})
    }
}
