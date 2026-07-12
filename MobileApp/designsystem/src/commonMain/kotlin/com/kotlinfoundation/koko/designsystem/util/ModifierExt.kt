package com.kotlinfoundation.koko.designsystem.util

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.AnnotatedString.Builder
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import com.kotlinfoundation.koko.designsystem.theme.AppSpacing

// This is to force layout to go beyond the borders of its parent
fun Modifier.fillWidthOfParent(totalParentHorizontalPaddingInPx: Float) = this.layout { measurable, constraints ->
    val placeable =
        measurable.measure(
            constraints.copy(maxWidth = constraints.maxWidth + totalParentHorizontalPaddingInPx.fastRoundToInt()),
        )

    val startOffset =
        if (placeable.width <= constraints.maxWidth) {
            // Few items: shift left to ignore parent padding
            -totalParentHorizontalPaddingInPx.toInt() / 2
        } else {
            // Many items: no shift
            0
        }

    layout(placeable.width, placeable.height) {
        // Shift the content to ignore parent's horizontal padding
        placeable.place(startOffset, 0)
    }
}

inline fun Builder.appendLinkIfNotEmpty(
    url: String,
    text: String,
) {
    if (url.isEmpty()) {
        append(text)
        return
    }

    val linkAnnotation = LinkAnnotation.Url(url = url)
    return withLink(linkAnnotation) {
        append(text)
    }
}

@Stable
fun Modifier.defaultScreenPadding(appSpacing: AppSpacing): Modifier = this.then(
    Modifier.padding(
        top = appSpacing.defaultSpacing,
        start = appSpacing.outerSpacing,
        end = appSpacing.outerSpacing,
        bottom = appSpacing.outerSpacing,
    ),
)

@Stable
fun Modifier.scrollableScreenPadding(
    scrollState: ScrollState,
    appSpacing: AppSpacing,
    innerPadding: PaddingValues = PaddingValues(0.dp),
): Modifier = this.then(
    Modifier
        .verticalScroll(scrollState)
        .padding(innerPadding)
        .defaultScreenPadding(appSpacing),
)
