package com.kotlinfoundation.koko.presentation.screens.gallery

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kotlinfoundation.koko.designsystem.components.AppButton
import com.kotlinfoundation.koko.designsystem.components.AsyncImageWithShimmer
import com.kotlinfoundation.koko.designsystem.components.EmptyContentView
import com.kotlinfoundation.koko.designsystem.components.LoadingProgress
import com.kotlinfoundation.koko.designsystem.components.LoadingProgressMode
import com.kotlinfoundation.koko.designsystem.components.ScreenWithToolbar
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_sparkles
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.domain.model.generation.GenerationOutput
import com.kotlinfoundation.koko.generated.resources.Res
import com.kotlinfoundation.koko.generated.resources.btn_generate
import com.kotlinfoundation.koko.generated.resources.empty_generations_state_description
import com.kotlinfoundation.koko.generated.resources.empty_generations_state_title
import com.kotlinfoundation.koko.generated.resources.title_screen_gallery
import com.kotlinfoundation.koko.util.StoreDevice
import com.kotlinfoundation.koko.util.StoreScreenshot
import org.jetbrains.compose.resources.stringResource

@Composable
fun GalleryScreen(
    modifier: Modifier = Modifier,
    viewModel: GalleryViewModel,
    onNavigateToResult: (id: String) -> Unit = {},
    onNavigateToHome: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GalleryScreen(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onUiEvent = { event ->
            when (event) {
                is GalleryUiEvent.OnClickItem -> onNavigateToResult(event.item.id)
                GalleryUiEvent.OnClickGenerate -> onNavigateToHome()
                else -> viewModel.onUiEvent(event)
            }
        },
    )
}

@Composable
internal fun GalleryScreen(
    modifier: Modifier = Modifier,
    uiState: GalleryUiState,
    onUiEvent: (GalleryUiEvent) -> Unit,
) {
    ScreenWithToolbar(
        modifier = modifier,
        isScrollableContent = false, // Set to false if content itself has scrollable content such as LazyColumn
        title = stringResource(Res.string.title_screen_gallery),
        includeBottomInsets = false, // Set to true if bottom nav is not visible
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing)) {
            when {
                uiState.isLoading -> {
                    LoadingProgress(mode = LoadingProgressMode.FULLSCREEN)
                }

                uiState.isLoading.not() && uiState.generations.isEmpty() -> {
                    EmptyContentView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        title = stringResource(Res.string.empty_generations_state_title),
                        text = stringResource(Res.string.empty_generations_state_description),
                    )
                }

                else -> {
                    GenerationList(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        generations = uiState.generations,
                        onClickItem = { item ->
                            onUiEvent(GalleryUiEvent.OnClickItem(item))
                        },
                    )
                }
            }

            AppButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.btn_generate),
                startIcon = UiRes.drawable.ic_sparkles,
                onClick = {
                    onUiEvent(GalleryUiEvent.OnClickGenerate)
                },
            )
        }
    }
}

@Composable
private fun GenerationList(
    modifier: Modifier = Modifier,
    generations: List<GenerationOutput>,
    onClickItem: (GenerationOutput) -> Unit,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 150.dp),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.horizontalItemSpacing),
        verticalItemSpacing = AppTheme.spacing.verticalListItemSpacingSmall,
        contentPadding = PaddingValues(bottom = AppTheme.spacing.outerSpacing),
        modifier = modifier,
    ) {
        items(items = generations, key = { item -> item.id }) { item ->
            GenerationItem(
                modifier = Modifier.animateItem(),
                item = item,
                onClickItem = {
                    onClickItem(item)
                },
            )
        }
    }
}

@Composable
private fun GenerationItem(
    item: GenerationOutput,
    modifier: Modifier = Modifier,
    onClickItem: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    item.output?.let { imageSrc ->
        AsyncImageWithShimmer(
            model = imageSrc,
            modifier = modifier
                .clickable {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClickItem()
                }
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, AppTheme.colors.outline, shape = RoundedCornerShape(6.dp)),
            contentDescription = null,
        )
    }
}

@Preview
@StoreScreenshot(device = StoreDevice.IPHONE_6_5, locale = "en", tag = "02-gallery")
@Composable
private fun GalleryStoreScreenshot_iPhone_en() {
    AppTheme {
        GalleryScreen(uiState = GalleryUiState(), onUiEvent = {})
    }
}

@Preview
@Composable
private fun GalleryScreenPreview() {
    AppTheme {
        GalleryScreen(uiState = GalleryUiState(), onUiEvent = {})
    }
}
