package com.kotlinfoundation.koko.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints.Companion.Infinity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.theme.MaterialColor
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiConfigUiState(
    val particleCount: Int,
    val particleSize: Dp,
    val verticalRange: Float,
    val colors: List<Color>,
) {
    companion object {
        val default =
            ConfettiConfigUiState(
                particleCount = 100,
                particleSize = 12.dp,
                verticalRange = 1f,
                colors =
                listOf(
                    Color(MaterialColor.RED),
                    Color(MaterialColor.PINK),
                    Color(MaterialColor.PURPLE),
                    Color(MaterialColor.DEEP_PURPLE),
                    Color(MaterialColor.INDIGO),
                    Color(MaterialColor.BLUE),
                    Color(MaterialColor.LIGHT_BLUE),
                    Color(MaterialColor.CYAN),
                    Color(MaterialColor.TEAL),
                    Color(MaterialColor.GREEN),
                    Color(MaterialColor.LIGHT_GREEN),
                    Color(MaterialColor.LIME),
                    Color(MaterialColor.YELLOW),
                    Color(MaterialColor.AMBER),
                    Color(MaterialColor.ORANGE),
                    Color(MaterialColor.DEEP_ORANGE),
                    Color(MaterialColor.BROWN),
                    Color(MaterialColor.GREY),
                    Color(MaterialColor.BLUE_GREY),
                ),
            )
    }
}

@Composable
fun ConfettiParticlesAnimated(
    modifier: Modifier = Modifier,
    config: ConfettiConfigUiState = ConfettiConfigUiState.default,
) {
    BoxWithConstraints(modifier = modifier) {
        val widthPx = constraints.maxWidth.toFloat()
        val boxMaxHeight =
            if (constraints.maxHeight == Infinity) 400f else constraints.maxHeight.toFloat()
        val heightPx = boxMaxHeight * config.verticalRange

        // Confetti particles
        repeat(config.particleCount) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "confetti$index")
            val startXOffsetFraction = remember(index) { Random.nextFloat() } // 0..1
            val startYOffsetFraction = remember(index) { Random.nextFloat() } // 0..1
            val randomColor = remember { config.colors.random() }

            val animatedYOffsetFraction by infiniteTransition.animateFloat(
                initialValue = startYOffsetFraction,
                targetValue = startYOffsetFraction + 0.5f,
                animationSpec =
                infiniteRepeatable(
                    animation = tween(2000 + index * 100, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "confettiY$index",
            )

            val animationRotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec =
                infiniteRepeatable(
                    animation = tween(1500 + index * 50, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "confettiRotation$index",
            )

            Box(
                modifier =
                Modifier
                    .offset {
                        IntOffset(
                            (startXOffsetFraction * widthPx).toInt(),
                            (animatedYOffsetFraction * heightPx).toInt(),
                        )
                    }.graphicsLayer {
                        rotationZ = animationRotation
                    }.size(config.particleSize)
                    .background(randomColor, RoundedCornerShape(2.dp)),
            )
        }
    }
}

@Composable
fun ConfettiParticlesAnimatedFromBottom(
    durationInMs: Long = 1500L,
    onComplete: () -> Unit = {},
) {
    var animationState by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        val duration = durationInMs
        val steps = 60
        val stepDelay = duration / steps

        repeat(steps) {
            animationState = (it + 1).toFloat() / steps
            delay(stepDelay)
        }
        onComplete()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val confettiColors =
            listOf(
                Color(0xFFFFD700), // Gold
                Color(0xFFFF69B4), // Hot Pink
                Color(0xFF00CED1), // Dark Turquoise
                Color(0xFFFF6347), // Tomato
                Color(0xFF32CD32), // Lime Green
            )

        repeat(50) { index ->
            val x =
                (size.width * (index % 10) / 10f) +
                    sin((animationState * 6 + index) * 0.5f) * 100f
            val y =
                size.height * (1 - animationState) +
                    cos((animationState * 4 + index) * 0.3f) * 50f
            val color = confettiColors[index % confettiColors.size]
            val alpha = (1 - animationState).coerceAtLeast(0f)

            drawCircle(
                color = color.copy(alpha = alpha),
                radius = 6f + sin((animationState * 8 + index) * 0.2f) * 3f,
                center = Offset(x, y),
            )
        }
    }
}

@Composable
fun ConfettiParticlesAnimatedFromTop() {
    val confettiItems = remember { (0..20).map { ConfettiItem() } }

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) { }, // Consume touch events
    ) {
        confettiItems.forEach { item ->
            AnimatedConfettiPiece(item)
        }
    }
}

data class ConfettiItem(
    val x: Float = Random.nextFloat() * 400,
    val color: Color =
        listOf(
            Color(0xFFFFD700),
            Color(0xFFFF69B4),
            Color(0xFF00CED1),
            Color(0xFF98FB98),
            Color(0xFFFF6347),
        ).random(),
    val duration: Int = Random.nextInt(2000, 4000),
)

@Composable
private fun AnimatedConfettiPiece(item: ConfettiItem) {
    val infiniteTransition = rememberInfiniteTransition()

    val yPosition by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 1000f,
        animationSpec =
        infiniteRepeatable(
            animation =
            tween(
                durationMillis = item.duration,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
        infiniteRepeatable(
            animation =
            tween(
                durationMillis = item.duration / 2,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
    )

    Box(
        modifier =
        Modifier
            .offset(x = item.x.dp, y = yPosition.dp)
            .size(8.dp)
            .rotate(rotation)
            .background(item.color, CircleShape),
    )
}

// `@Preview` intentionally omitted: confetti positions are randomized per render,
// which breaks Roborazzi snapshot determinism.
@Composable
internal fun ConfettiParticlesAnimatedPreview() {
    PreviewHelper {
        ConfettiParticlesAnimated(
            modifier = Modifier.height(400.dp).fillMaxWidth(),
            config = ConfettiConfigUiState.default.copy(particleCount = 100),
        )
    }
}
