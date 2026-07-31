package com.kotlinfoundation.koko.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper
import kotlin.math.absoluteValue

@Composable
fun AnimatedHorizontalPager(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    pageContent: @Composable PagerScope.(page: Int) -> Unit,
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier,
    ) { pageIndex ->
        val pageOffset =
            ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction).absoluteValue

        Box(
            Modifier
                .graphicsLayer {
                    alpha =
                        lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f),
                        )
                    val scale =
                        lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f),
                        )
                    scaleX = scale
                    scaleY = scale
                },
        ) {
            pageContent(pageIndex)
        }
    }
}

@Preview
@Composable
internal fun AnimatedHorizontalPagerPreview() {
    PreviewHelper {
        val pages = listOf("Page One", "Page Two", "Page Three")
        val pagerState = rememberPagerState(initialPage = 0, pageCount = { pages.size })

        AnimatedHorizontalPager(
            pagerState = pagerState,
            modifier =
            Modifier
                .fillMaxWidth()
                .height(200.dp),
        ) { page ->
            Box(
                modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        when (page) {
                            0 -> Color(0xFFB3E5FC)
                            1 -> Color(0xFF81D4FA)
                            else -> Color(0xFF4FC3F7)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = pages[page],
                    style = AppTheme.typography.bodyLarge,
                    color = Color.White,
                )
            }
        }
    }
}
