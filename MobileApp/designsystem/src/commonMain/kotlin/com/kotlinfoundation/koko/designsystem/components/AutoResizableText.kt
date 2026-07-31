package com.kotlinfoundation.koko.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper

@Composable
fun AutoResizableText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = AppTheme.typography.h1,
    color: Color = AppTheme.colors.text.primary,
    textAlign: TextAlign = TextAlign.Center,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    var resizeTextStyle by remember { mutableStateOf(style) }
    var shouldDrawText by remember { mutableStateOf(false) }
    Text(
        modifier =
        modifier.drawWithContent {
            if (shouldDrawText) drawContent()
        },
        text = text,
        style = resizeTextStyle,
        color = color,
        softWrap = false,
        textAlign = textAlign,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth) {
                resizeTextStyle = resizeTextStyle.copy(fontSize = resizeTextStyle.fontSize * 0.9)
            } else {
                shouldDrawText = true
            }
            onTextLayout?.invoke(textLayoutResult)
        },
    )
}

@Composable
@Preview
internal fun AutoResizableTextPreview() {
    PreviewHelper {
        var text by remember { mutableStateOf("This text will double and resize when you click") }
        AutoResizableText(
            text = text,
            modifier =
            Modifier.clickable {
                text += text
            },
            style = AppTheme.typography.h1,
            color = AppTheme.colors.text.primary,
            textAlign = TextAlign.Center,
        )
    }
}
