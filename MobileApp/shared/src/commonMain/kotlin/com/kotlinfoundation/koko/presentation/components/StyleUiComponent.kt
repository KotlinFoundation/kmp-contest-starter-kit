package com.kotlinfoundation.koko.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.components.AsyncImageWithShimmer
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_check
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import org.jetbrains.compose.resources.painterResource

data class StyleUiComponentUiState(
    val text: String,
    val imageSrc: String,
    val isSelected: Boolean = false,
)

@Composable
fun StyleUiComponent(
    uiState: StyleUiComponentUiState,
    onClick: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val isSelected = uiState.isSelected
    val imageSize = 80.dp

    val borderColor by animateColorAsState(
        if (isSelected) AppTheme.colors.primary else AppTheme.colors.primary.copy(alpha = 0.1f),
    )
    val imageBackgroundColor by animateColorAsState(
        if (isSelected) Color.Black.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.1f),
    )

    Column(
        modifier = Modifier
            .clickable(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 64.dp),
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
    ) {
        Box(
            modifier = Modifier
                .size(imageSize)
                .clip(CircleShape)
                .border(3.dp, borderColor, CircleShape),
        ) {
            AsyncImageWithShimmer(
                model = uiState.imageSrc,
                contentDescription = null,
                modifier = Modifier
                    .size(imageSize)
                    .clip(CircleShape),
            )

            Box(modifier = Modifier.matchParentSize().background(imageBackgroundColor))
            if (isSelected) {
                Icon(
                    painter = painterResource(UiRes.drawable.ic_check),
                    tint = Color.White,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp).align(Alignment.Center),
                )
            }
        }
        Text(
            modifier = Modifier.widthIn(max = 100.dp),
            text = uiState.text,
            maxLines = 2,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            color = if (isSelected) AppTheme.colors.primary else AppTheme.colors.text.primary,
            style = AppTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}
