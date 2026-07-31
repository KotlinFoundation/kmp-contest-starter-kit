package com.kotlinfoundation.koko.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_logo
import com.kotlinfoundation.koko.designsystem.generated.resources.illustration_empty
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper
import com.kotlinfoundation.koko.designsystem.util.defaultScreenPadding
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun EmptyContentView(
    title: String,
    text: String,
    image: DrawableResource = UiRes.drawable.illustration_empty,
    actionButton: @Composable (() -> Unit)? = null,
    modifier: Modifier =
        Modifier
            .fillMaxSize()
            .defaultScreenPadding(AppTheme.spacing),
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier.size(200.dp),
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing.sectionSpacing))
        SectionTitle(title, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            textAlign = TextAlign.Center,
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodyLarge,
        )
        actionButton?.let {
            Spacer(modifier = Modifier.height(AppTheme.spacing.sectionSpacing))
            Box(modifier = Modifier) { it() }
        }
    }
}

@Composable
@Preview
internal fun EmptyContentPreview() {
    PreviewHelper {
        EmptyContentView(
            title = "No data found",
            text = "No matching data found. Please search again",
            image = UiRes.drawable.illustration_empty,
        )
    }
}
