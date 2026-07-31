package com.kotlinfoundation.koko.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

enum class ChipStyle {
    FILLED,
    FILLED_ALPHA,
    INVERSE_FILLED,
    OUTLINE,
}

enum class ChipSize {
    LARGE,
    MEDIUM,
    SMALL,
}

@Composable
fun Chip(
    text: String,
    modifier: Modifier = Modifier,
    style: ChipStyle = ChipStyle.OUTLINE,
    size: ChipSize = ChipSize.MEDIUM,
    startIconRes: DrawableResource? = null,
    endIconRes: DrawableResource? = null,
    onClick: () -> Unit = {},
) {
    val shape = CircleShape
    val height =
        when (size) {
            ChipSize.LARGE -> 49.dp
            ChipSize.MEDIUM -> 42.dp
            ChipSize.SMALL -> 34.dp
        }
    val horizontalPadding =
        when (size) {
            ChipSize.LARGE -> 24.dp
            ChipSize.MEDIUM -> 20.dp
            ChipSize.SMALL -> 16.dp
        }

    val iconSize =
        when (size) {
            ChipSize.LARGE -> 20.dp
            ChipSize.MEDIUM -> 16.dp
            ChipSize.SMALL -> 14.dp
        }
    val textStyle =
        when (size) {
            ChipSize.LARGE -> AppTheme.typography.bodyExtraLarge
            ChipSize.MEDIUM -> AppTheme.typography.bodyLarge
            ChipSize.SMALL -> AppTheme.typography.bodyMedium
        }.copy(fontWeight = FontWeight.SemiBold)

    val textColor by animateColorAsState(
        when (style) {
            ChipStyle.FILLED -> AppTheme.colors.onPrimary
            ChipStyle.OUTLINE -> AppTheme.colors.text.primary
            ChipStyle.INVERSE_FILLED -> AppTheme.colors.primary
            ChipStyle.FILLED_ALPHA -> AppTheme.colors.primary
        },
        animationSpec = tween(),
    )

    val iconColor by animateColorAsState(
        when (style) {
            ChipStyle.FILLED -> AppTheme.colors.onPrimary
            ChipStyle.OUTLINE -> AppTheme.colors.primary
            ChipStyle.INVERSE_FILLED -> AppTheme.colors.primary
            ChipStyle.FILLED_ALPHA -> AppTheme.colors.primary
        },
        animationSpec = tween(),
    )

    val containerColor by animateColorAsState(
        when (style) {
            ChipStyle.FILLED -> AppTheme.colors.primary
            ChipStyle.OUTLINE -> Color.Transparent
            ChipStyle.INVERSE_FILLED -> AppTheme.colors.onPrimary
            ChipStyle.FILLED_ALPHA -> AppTheme.colors.primary.copy(alpha = 0.1f)
        },
        animationSpec = tween(),
    )
    val borderColor by animateColorAsState(
        when (style) {
            ChipStyle.FILLED -> Color.Transparent
            ChipStyle.OUTLINE -> AppTheme.colors.outline
            ChipStyle.INVERSE_FILLED -> Color.Transparent
            ChipStyle.FILLED_ALPHA -> AppTheme.colors.primary.copy(alpha = 0.1f)
        },
        animationSpec = tween(),
    )

    Row(
        modifier =
        modifier
            .height(height)
            .clip(shape)
            .background(color = containerColor, shape = shape)
            .border(1.dp, borderColor, shape)
            .clickable { onClick() }
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        startIconRes?.let {
            Icon(
                modifier = Modifier.sizeIn(maxWidth = iconSize, maxHeight = iconSize),
                painter = painterResource(it),
                contentDescription = null,
                tint = iconColor,
            )
            Spacer(modifier = Modifier.width(AppTheme.spacing.defaultSpacing))
        }

        Text(
            text = text,
            style = textStyle,
            color = textColor,
        )

        endIconRes?.let {
            Spacer(modifier = Modifier.width(AppTheme.spacing.defaultSpacing))
            Icon(
                modifier = Modifier.sizeIn(maxWidth = iconSize, maxHeight = iconSize),
                painter = painterResource(it),
                contentDescription = null,
                tint = iconColor,
            )
        }
    }
}

@Composable
@Preview
internal fun ChipPreview() {
    PreviewHelper {
        val selectedIndex = remember { mutableStateOf(0) }
        Chip(
            "Filled",
            onClick = {
                selectedIndex.value = 0
            },
            style = if (selectedIndex.value == 0) ChipStyle.FILLED else ChipStyle.OUTLINE,
        )

        Chip(
            "Filled Alpha",
            onClick = {
                selectedIndex.value = 1
            },
            style = if (selectedIndex.value == 1) ChipStyle.FILLED_ALPHA else ChipStyle.OUTLINE,
        )

        Chip(
            "Outlined",
            onClick = {
                selectedIndex.value = 2
            },
            style = if (selectedIndex.value == 2) ChipStyle.FILLED else ChipStyle.OUTLINE,
        )

        Chip(
            "Inverse Filled",
            onClick = {
                selectedIndex.value = 3
            },
            style = if (selectedIndex.value == 3) ChipStyle.INVERSE_FILLED else ChipStyle.OUTLINE,
        )
    }
}
