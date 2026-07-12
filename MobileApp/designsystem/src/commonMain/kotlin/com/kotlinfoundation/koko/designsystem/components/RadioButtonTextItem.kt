package com.kotlinfoundation.koko.designsystem.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper

@Composable
fun AppRadioButtonWithText(
    text: String,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    horizontalSpacing: Dp = AppTheme.spacing.horizontalItemSpacing,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier.clickable { onClick() }.padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
    ) {
        AppRadioButton(
            selected = isSelected,
            onClick = { onClick() },
        )
        Text(
            text = text,
            fontWeight = FontWeight.Medium,
            color = AppTheme.colors.text.primary,
            style = AppTheme.typography.h6,
        )
    }
}

@Composable
fun AppRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    val iconSize = 24.0.dp
    val dotSize = 14.dp
    val strokeWidth = 2.dp

    val dotRadius by animateDpAsState(
        targetValue = if (selected) dotSize / 2 else 0.dp,
        animationSpec = tween(durationMillis = 100),
    )

    val selectableModifier =
        if (onClick != null) {
            Modifier.selectable(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
                role = Role.RadioButton,
                interactionSource = interactionSource,
                indication =
                ripple(bounded = false, radius = 20.dp),
            )
        } else {
            Modifier
        }
    val color = AppTheme.colors.primary
    Canvas(
        modifier
            .then(selectableModifier)
            .wrapContentSize(Alignment.Center)
            .requiredSize(iconSize),
    ) {
        // Draw the radio button
        val strokeWidthInPx = strokeWidth.toPx()
        drawCircle(
            color,
            radius = (iconSize / 2).toPx() - strokeWidthInPx / 2,
            style = Stroke(strokeWidthInPx),
        )
        if (dotRadius > 0.dp) {
            drawCircle(color, dotRadius.toPx() - strokeWidthInPx / 2, style = Fill)
        }
    }
}

@Composable
@Preview
internal fun RadioButtonPreview(modifier: Modifier = Modifier) {
    PreviewHelper {
        AppRadioButtonWithText(
            modifier = modifier,
            text = "Selected Radio Button",
            isSelected = true,
        )

        AppRadioButtonWithText(
            modifier = modifier,
            text = "Unselected Radio Button",
            isSelected = false,
        )
    }
}
