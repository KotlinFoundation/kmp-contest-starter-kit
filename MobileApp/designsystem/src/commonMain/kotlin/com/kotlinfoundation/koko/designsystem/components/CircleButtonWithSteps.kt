package com.kotlinfoundation.koko.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper

@Composable
fun CircleButtonWithSteps(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    nbSteps: Int = 3,
    selectedStep: Int = 0,
    buttonColor: ButtonColors = ButtonDefaults.buttonColors(),
    selectedStepColor: Color = buttonColor.containerColor,
    unselectedStepColor: Color = buttonColor.containerColor.copy(alpha = 0.5f),
    icon: ImageVector = Icons.AutoMirrored.Filled.ArrowForward,
    archPadding: Dp = 6.dp,
    strokeWidth: Dp = 4.dp,
    extraPaddingBetweenButtonAndSteps: Dp = 2.dp,
    onClick: () -> Unit = {},
) {
    val strokeWidthInPx = with(LocalDensity.current) { strokeWidth.toPx() }
    val sizeInPx = with(LocalDensity.current) { size.toPx() }
    val archPaddingInPx = with(LocalDensity.current) { archPadding.toPx() }

    Box(
        contentAlignment = Alignment.Center,
        modifier =
        modifier
            .size(size)
            .drawBehind {
                val radius = (sizeInPx - strokeWidthInPx) / 2
                val arcSweep = 360f / nbSteps
                for (i in 0 until nbSteps) {
                    val isSelected = i <= selectedStep
                    drawArc(
                        color = if (isSelected) selectedStepColor else unselectedStepColor,
                        startAngle = i * arcSweep,
                        sweepAngle = arcSweep - archPaddingInPx,
                        useCenter = false,
                        style = Stroke(width = strokeWidthInPx, cap = StrokeCap.Round),
                        size = Size(radius * 2, radius * 2),
                        topLeft = Offset(center.x - radius, center.y - radius),
                    )
                }
            }.padding(strokeWidth + extraPaddingBetweenButtonAndSteps),
    ) {
        Button(
            contentPadding = PaddingValues(0.dp),
            colors = buttonColor,
            onClick = { onClick() },
            modifier =
            Modifier
                .fillMaxSize()
                .clip(CircleShape),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Next",
            )
        }
    }
}

@Composable
@Preview
internal fun CircleButtonWithStepsPreview() {
    PreviewHelper {
        var selectedStep by remember { mutableStateOf(0) }
        CircleButtonWithSteps(
            selectedStep = selectedStep,
            nbSteps = 3,
            onClick = { selectedStep++ },
        )
    }
}
