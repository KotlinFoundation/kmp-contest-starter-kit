package com.kotlinfoundation.koko.designsystem.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper

@Composable
fun ScreenTitle(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
) {
    Text(
        text = text,
        style = AppTheme.typography.h3,
        color = AppTheme.colors.text.primary,
        textAlign = textAlign,
        modifier = modifier,
    )
}

@Composable
fun DialogOrBottomSheetTitle(
    text: String,
    color: Color = AppTheme.colors.text.primary,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
) {
    Text(
        text = text,
        style = AppTheme.typography.h4,
        color = color,
        textAlign = textAlign,
        modifier = modifier,
    )
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
) {
    Text(
        modifier = modifier,
        text = text,
        style = AppTheme.typography.h5,
        color = AppTheme.colors.text.primary,
        textAlign = textAlign,
    )
}

@Composable
fun SmallTitle(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
) {
    Text(
        modifier = modifier,
        text = text,
        style = AppTheme.typography.h6,
        color = AppTheme.colors.text.primary,
        textAlign = textAlign,
    )
}

@Preview
@Composable
fun TitlesPreview() {
    PreviewHelper {
        ScreenTitle("Screen Title")
        DialogOrBottomSheetTitle("Dialog or Bottom Sheet Title")
        SectionTitle("Section Title")
        SmallTitle("Small Title")
    }
}
