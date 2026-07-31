package com.kotlinfoundation.koko.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper

/**
 * A loud, error-tinted notice strip. Used to mark a screen as running in a demo/simulated mode
 * (e.g. the paywall while the mock subscription provider is active) so it can't be mistaken for real.
 * The [text] is passed in so this stays a generic, resource-free design-system component.
 */
@Composable
fun DemoBanner(text: String, modifier: Modifier = Modifier) {
    AppCardContainer(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        backgroundColor = AppTheme.colors.status.errorContainer,
    ) {
        Text(
            text = text,
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.status.error,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
@Preview
internal fun DemoBannerPreview() {
    PreviewHelper {
        DemoBanner(text = "Demo mode — purchases are simulated.")
    }
}
