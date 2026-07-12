package com.kotlinfoundation.koko.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper

@Composable
fun AppCardContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = AppTheme.colors.surfaceContainer,
    onClick: (() -> Unit)? = null,
    contentPaddingValues: PaddingValues = PaddingValues(AppTheme.spacing.cardContentSpacing),
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
        modifier
            .clip(shape = shape)
            .background(backgroundColor)
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
            }.padding(contentPaddingValues),
    ) {
        content()
    }
}

@Composable
fun IconTitleDescriptionCard(
    title: String,
    description: String = "",
    icon: IconSource? = null,
    iconTint: Color = AppTheme.colors.primary,
    iconContentDescription: String? = null,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    IconTitleDescriptionCard(
        title = title,
        description = description,
        horizontalAlignment = horizontalAlignment,
        iconContent =
        icon?.let {
            {
                IconWrapper(
                    icon = it,
                    modifier = Modifier.size(48.dp),
                    tint = iconTint,
                    contentDescription = iconContentDescription,
                )
            }
        },
        modifier = modifier,
        onClick = onClick,
    )
}

@Composable
fun IconTitleDescriptionCard(
    title: String,
    description: String = "",
    iconContent: @Composable (() -> Unit)? = null,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, AppTheme.colors.primary),
        onClick = onClick,
        colors =
        CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(AppTheme.spacing.cardContentSpacing),
            horizontalAlignment = horizontalAlignment,
            verticalArrangement =
            Arrangement.spacedBy(
                AppTheme.spacing.groupedVerticalElementSpacingSmall,
                Alignment.CenterVertically,
            ),
        ) {
            iconContent?.invoke()
            Spacer(modifier = Modifier.height(AppTheme.spacing.defaultSpacing))
            Text(
                text = title,
                style = AppTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = AppTheme.colors.text.primary,
            )
            Text(
                text = description,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
@Preview
internal fun AppCardContainerPreview() {
    PreviewHelper {
        AppCardContainer {
            Text("Content of the card container")
        }
        IconTitleDescriptionCard(
            title = "Title",
            description = "Description",
            icon = IconSource.Vector(Icons.Default.Check),
        )
    }
}
