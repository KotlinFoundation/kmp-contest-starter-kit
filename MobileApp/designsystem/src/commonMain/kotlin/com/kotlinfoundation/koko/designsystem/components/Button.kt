package com.kotlinfoundation.koko.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_crown
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_delete
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

enum class ButtonStyle {
    PRIMARY,
    ALTERNATIVE,
    TEXT,
}

enum class ButtonSize(
    val value: Dp,
) {
    LARGE(65.dp),
    MEDIUM(58.dp),
    SMALL(50.dp),
    EXTRA_SMALL(43.dp),
}

@Composable
fun AppButton(
    text: String = "",
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.PRIMARY,
    size: ButtonSize = ButtonSize.MEDIUM,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    startIcon: DrawableResource? = null,
    endIcon: DrawableResource? = null,
    shape: Shape = CircleShape,
    onClick: () -> Unit,
) {
    val iconSize =
        when (size) {
            ButtonSize.LARGE -> 24.dp
            ButtonSize.MEDIUM -> 20.dp
            ButtonSize.SMALL -> 16.dp
            ButtonSize.EXTRA_SMALL -> 12.dp
        }

    val buttonColors =
        ButtonDefaults.buttonColors(
            containerColor =
            when (style) {
                ButtonStyle.PRIMARY -> AppTheme.colors.primary
                ButtonStyle.ALTERNATIVE -> AppTheme.colors.alternative
                ButtonStyle.TEXT -> Color.Transparent
            },
            contentColor =
            when (style) {
                ButtonStyle.PRIMARY -> AppTheme.colors.onPrimary
                ButtonStyle.ALTERNATIVE -> AppTheme.colors.onAlternative
                ButtonStyle.TEXT -> AppTheme.colors.primary
            },
        )
    if (isLoading) {
        Box(
            modifier = modifier.height(size.value),
            contentAlignment = Alignment.Center,
        ) {
            LoadingProgress(mode = LoadingProgressMode.CIRCULAR)
        }
    } else {
        Button(
            enabled = enabled,
            modifier =
            modifier
                .heightIn(min = size.value)
                .clip(shape),
            shape = shape,
            colors = buttonColors,
            onClick = { onClick() },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                startIcon?.let {
                    Icon(
                        modifier = Modifier.size(iconSize),
                        painter = painterResource(it),
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(AppTheme.spacing.horizontalItemSpacing))
                }
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    style = AppTheme.typography.bodyLarge,
                )

                endIcon?.let {
                    Spacer(modifier = Modifier.width(AppTheme.spacing.horizontalItemSpacing))
                    Icon(
                        modifier = Modifier.size(iconSize),
                        painter = painterResource(it),
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@Composable
fun CircularActionButton(
    icon: DrawableResource,
    iconTint: Color = AppTheme.colors.text.primary,
    text: String? = null,
    isLoading: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.widthIn(max = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
    ) {
        Box(
            modifier =
            Modifier
                .size(60.dp)
                .clip(CircleShape)
                .clickable {
                    if (!isLoading) onClick()
                }.background(AppTheme.colors.outline),
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                LoadingProgress(mode = LoadingProgressMode.CIRCULAR)
            } else {
                Icon(
                    modifier = Modifier.size(28.dp),
                    tint = iconTint,
                    painter = painterResource(icon),
                    contentDescription = null,
                )
            }
        }

        text?.let {
            Text(
                text = text,
                color = AppTheme.colors.text.secondary,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview
@Composable
internal fun AppButtonPreviews() {
    PreviewHelper {
        AppButton("Primary Enabled", style = ButtonStyle.PRIMARY, onClick = {})
        AppButton("Primary Disabled", style = ButtonStyle.PRIMARY, onClick = {}, enabled = false)
        AppButton(
            "PrimaryIconOnLeft",
            style = ButtonStyle.PRIMARY,
            onClick = {},
            startIcon = UiRes.drawable.ic_crown,
        )
        AppButton(
            "PrimaryIconOnRight",
            style = ButtonStyle.PRIMARY,
            onClick = {},
            endIcon = UiRes.drawable.ic_crown,
        )
        AppButton("Alternative Enabled", style = ButtonStyle.ALTERNATIVE, onClick = {})

        CircularActionButton(icon = UiRes.drawable.ic_crown, text = "Action")
        CircularActionButton(
            icon = UiRes.drawable.ic_delete,
            text = "Delete",
            iconTint = AppTheme.colors.status.error,
        )
        CircularActionButton(icon = UiRes.drawable.ic_crown, text = "Action Button Long Text")
        CircularActionButton(icon = UiRes.drawable.ic_crown)
    }
}
