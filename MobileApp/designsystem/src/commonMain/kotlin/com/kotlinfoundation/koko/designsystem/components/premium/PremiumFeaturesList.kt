package com.kotlinfoundation.koko.designsystem.components.premium

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_check
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_close
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper
import com.kotlinfoundation.koko.designsystem.util.UiText
import org.jetbrains.compose.resources.vectorResource

data class PremiumFeatureUiState(
    val text: UiText,
    val isIncluded: Boolean = true,
)

@Composable
fun PremiumFeaturesList(
    features: List<PremiumFeatureUiState> = emptyList(),
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.verticalListItemSpacingSmall),
    ) {
        features.forEach { feature ->
            val icon =
                if (feature.isIncluded) UiRes.drawable.ic_check else UiRes.drawable.ic_close
            val iconTint =
                if (feature.isIncluded) AppTheme.colors.text.primary else AppTheme.colors.status.error
            val textColor =
                if (feature.isIncluded) {
                    AppTheme.colors.text.primary
                } else {
                    AppTheme.colors.text.primary.copy(
                        alpha = 0.5f,
                    )
                }
            val textDecoration =
                if (feature.isIncluded) TextDecoration.None else TextDecoration.LineThrough
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inputIconTextSpacing),
            ) {
                Icon(
                    imageVector = vectorResource(icon),
                    tint = iconTint,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )

                Text(
                    text = feature.text.value,
                    style = AppTheme.typography.bodyExtraLarge,
                    textDecoration = textDecoration,
                    color = textColor,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
@Preview
internal fun PremiumFeaturesListPreview() {
    PreviewHelper {
        PremiumFeaturesList(
            features =
            listOf(
                PremiumFeatureUiState(text = UiText.of("Feature 1"), isIncluded = true),
                PremiumFeatureUiState(text = UiText.of("Feature 2"), isIncluded = true),
                PremiumFeatureUiState(text = UiText.of("Feature 3"), isIncluded = true),
                PremiumFeatureUiState(text = UiText.of("Feature 4"), isIncluded = false),
                PremiumFeatureUiState(text = UiText.of("Feature 5"), isIncluded = false),
            ),
        )
    }
}
