package com.kotlinfoundation.koko.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun AsyncImageWithShimmer(
    modifier: Modifier = Modifier,
    showShimmerLoading: Boolean = true,
    model: String,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
) {
    Box(modifier = modifier) {
        var isLoading by remember(model) { mutableStateOf(true) }
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            onLoading = {
                isLoading = true
            },
            onSuccess = {
                isLoading = false
            },
            onError = { throwable ->
                isLoading = false
            },
        )

        if (isLoading && showShimmerLoading) {
            ShimmerBox(
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ShimmerBox(modifier: Modifier = Modifier) {
    val shimmerColors =
        listOf(
            LightGray.copy(alpha = 0.6f),
            LightGray.copy(alpha = 0.3f),
            LightGray.copy(alpha = 0.6f),
        )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim =
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "shimmerAnim",
        )

    val brush =
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnim.value, translateAnim.value),
            end = Offset(translateAnim.value + 200f, translateAnim.value + 200f),
        )

    Box(
        modifier = modifier.background(brush),
    )
}
