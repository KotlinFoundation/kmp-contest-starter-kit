package com.kotlinfoundation.koko.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_crown
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun UserInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    startIconSize: Dp = 20.dp,
    endIconSize: Dp = 20.dp,
    shape: Shape = RoundedCornerShape(10.dp),
    enabled: Boolean = true,
    readOnly: Boolean = false,
    startIcon: DrawableResource? = null,
    endIcon: DrawableResource? = null,
    onClickEndIcon: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    helperText: String? = null,
    maxChars: Int? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val backgroundColor by animateColorAsState(
        when {
            readOnly -> AppTheme.colors.textInput.readOnlyBackground
            enabled && isFocused && !readOnly -> AppTheme.colors.alternative
            !enabled -> AppTheme.colors.textInput.disabledBackground
            else -> AppTheme.colors.textInput.background
        },
        animationSpec = tween(),
    )

    val borderColor by animateColorAsState(
        when {
            isFocused && !readOnly -> AppTheme.colors.primary
            else -> Color.Transparent
        },
        animationSpec = tween(),
    )

    val textIconColor by animateColorAsState(
        when (enabled) {
            true -> AppTheme.colors.textInput.textIcon
            false -> AppTheme.colors.textInput.disabledTextIcon
        },
        animationSpec = tween(),
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
    ) {
        Box(
            modifier =
            Modifier
                .clip(shape)
                .border(2.dp, borderColor, shape)
                .background(
                    color = backgroundColor,
                    shape = shape,
                ).padding(vertical = 18.dp, horizontal = 20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                startIcon?.let {
                    Icon(
                        modifier = Modifier.size(startIconSize),
                        tint = textIconColor,
                        painter = painterResource(it),
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(AppTheme.spacing.inputIconTextSpacing))
                }

                BasicTextField(
                    modifier = Modifier.weight(1f),
                    singleLine = singleLine,
                    maxLines = maxLines,
                    minLines = minLines,
                    readOnly = readOnly,
                    enabled = enabled,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    textStyle =
                    AppTheme.typography.bodyExtraLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = textIconColor,
                    ),
                    value = value,
                    cursorBrush = SolidColor(AppTheme.colors.text.primary),
                    interactionSource = interactionSource,
                    onValueChange = { text ->
                        if (maxChars == null) {
                            onValueChange(text)
                        } else {
                            val truncated =
                                if (text.length > maxChars) text.take(maxChars) else text
                            onValueChange(truncated)
                        }
                    },
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = label,
                                style = AppTheme.typography.bodyExtraLarge,
                                fontWeight = FontWeight.Normal,
                                color = AppTheme.colors.textInput.placeholder,
                            )
                        }
                        innerTextField()
                    },
                )
                endIcon?.let {
                    Spacer(modifier = Modifier.width(AppTheme.spacing.inputIconTextSpacing))
                    Icon(
                        modifier =
                        Modifier
                            .size(endIconSize)
                            .clickable {
                                onClickEndIcon()
                            },
                        tint = textIconColor,
                        painter = painterResource(it),
                        contentDescription = null,
                    )
                }
            }
        }

        // Helper row: tip + char counter

        if (maxChars != null || helperText != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = helperText ?: "",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
                // Only show the counter when there's a real limit (avoids rendering "0/null").
                if (maxChars != null) {
                    Text(
                        text = "${value.length}/$maxChars",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                }
            }
        }
    }
}

@Composable
@Preview
internal fun UserInputPreview() {
    PreviewHelper {
        var text by remember { mutableStateOf("") }
        UserInput(
            value = text,
            onValueChange = {
                text = it
            },
            label = "PlaceHolder",
            startIcon = null,
        )

        UserInput(
            value = text,
            onValueChange = {
                text = it
            },
            endIcon = UiRes.drawable.ic_crown,
            label = "",
        )

        UserInput(
            value = "Disabled Text",
            onValueChange = {},
            enabled = false,
            startIcon = UiRes.drawable.ic_crown,
            label = "Disabled",
        )

        UserInput(
            value = "ReadOnly Text",
            onValueChange = {},
            startIcon = UiRes.drawable.ic_crown,
            readOnly = true,
            label = "ReadyOnlyText",
        )
    }
}
