package com.kotlinfoundation.koko.presentation.screens.paywall.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.components.AppCardContainer
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.generated.resources.Res
import com.kotlinfoundation.koko.generated.resources.paywall_demo_banner
import org.jetbrains.compose.resources.stringResource

/**
 * A loud, always-on notice shown at the top of the paywall while the mock subscription provider is
 * active (no real key set). Makes it impossible to mistake simulated purchases for real ones.
 */
@Composable
internal fun DemoPaywallBanner(modifier: Modifier = Modifier) {
    AppCardContainer(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        backgroundColor = AppTheme.colors.status.warningContainer,
    ) {
        Text(
            text = stringResource(Res.string.paywall_demo_banner),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.text.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
