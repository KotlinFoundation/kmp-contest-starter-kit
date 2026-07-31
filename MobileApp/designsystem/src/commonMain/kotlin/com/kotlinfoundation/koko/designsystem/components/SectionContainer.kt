package com.kotlinfoundation.koko.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper

@Composable
fun SectionContainer(
    title: String? = null,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    onClickSubtitle: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        if (title != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionTitle(text = title)
                subtitle?.let {
                    Text(
                        modifier =
                        Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                onClickSubtitle()
                            },
                        text = it,
                        color = AppTheme.colors.primary,
                        style = AppTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppTheme.spacing.sectionHeaderSpacing))
        }
        content()
    }
}

@Composable
fun AnimatedSectionContainer(
    isVisible: Boolean,
    title: String? = null,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    onClickSubtitle: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter =
        slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        ) + fadeIn(animationSpec = tween(700, delayMillis = 400)),
    ) {
        SectionContainer(
            title = title,
            subtitle = subtitle,
            onClickSubtitle = onClickSubtitle,
            content = content,
        )
    }
}

@Composable
@Preview
internal fun SectionContainerPreview() {
    PreviewHelper {
        SectionContainer(title = "Section Title", subtitle = "Section Subtitle") {
            Text(text = "Section Content")
        }
    }
}
