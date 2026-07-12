package com.kotlinfoundation.koko.designsystem.components

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper
import com.kotlinfoundation.koko.designsystem.util.fillWidthOfParent

@Composable
fun HorizontalScrollableList(
    modifier: Modifier = Modifier,
    ignoreParentHorizontalPadding: Boolean = false,
    contentPadding: PaddingValues =
        PaddingValues(
            horizontal =
            if (ignoreParentHorizontalPadding) {
                AppTheme.spacing.outerSpacing
            } else {
                0.dp
            },
        ),
    state: LazyListState = rememberLazyListState(),
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(AppTheme.spacing.horizontalItemSpacing),
    content: LazyListScope.() -> Unit,
) {
    val updatedModifier =
        if (ignoreParentHorizontalPadding) {
            val horizontalPaddingInPx =
                with(LocalDensity.current) {
                    val startPaddingInPx =
                        contentPadding.calculateStartPadding(LocalLayoutDirection.current).toPx()
                    val endPaddingInPx =
                        contentPadding.calculateEndPadding(LocalLayoutDirection.current).toPx()
                    startPaddingInPx + endPaddingInPx
                }
            modifier.fillWidthOfParent(totalParentHorizontalPaddingInPx = horizontalPaddingInPx)
        } else {
            modifier
        }

    LazyRow(
        state = state,
        modifier = updatedModifier,
        flingBehavior = flingBehavior,
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement,
        content = content,
    )
}

@Preview
@Composable
fun HorizontalScrollableListPreview() {
    PreviewHelper {
        HorizontalScrollableList(
            ignoreParentHorizontalPadding = true,
            contentPadding = PaddingValues(horizontal = AppTheme.spacing.outerSpacing),
        ) {
            items(10) {
                Text("Item $it")
            }
        }
    }
}
