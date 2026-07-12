package com.kotlinfoundation.koko.designsystem.components.premium

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.kotlinfoundation.koko.designsystem.components.AppButton
import com.kotlinfoundation.koko.designsystem.components.Divider
import com.kotlinfoundation.koko.designsystem.components.ScreenTitle
import com.kotlinfoundation.koko.designsystem.components.SectionContainer
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.benefits
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_action_successful_purchase
import com.kotlinfoundation.koko.designsystem.generated.resources.confetti
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_premium_feature_crown
import com.kotlinfoundation.koko.designsystem.generated.resources.subtitle_successful_purchase
import com.kotlinfoundation.koko.designsystem.generated.resources.title_successful_purchase
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper
import com.kotlinfoundation.koko.designsystem.util.UiText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SuccessfulPurchaseContent(
    subscriptionUrl: String = "",
    features: List<PremiumFeatureUiState> = emptyList(),
    expirationDate: String? = null,
    isLifetime: Boolean = false,
    isRecurring: Boolean = true,
    modifier: Modifier = Modifier,
    onContinue: () -> Unit = {},
) {
    val statusBarHeight =
        with(LocalDensity.current) {
            WindowInsets.statusBars.getTop(this).toDp()
        }
    Column(
        modifier =
        modifier
            .fillMaxSize()
            .zIndex(4f)
            .background(AppTheme.colors.background)
            .padding(
                start = AppTheme.spacing.outerSpacing,
                end = AppTheme.spacing.outerSpacing,
                bottom = AppTheme.spacing.outerSpacing,
            ),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
    ) {
        Column(
            modifier =
            Modifier
                .weight(1f)
                .offset(y = -statusBarHeight)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
        ) {
            Image(
                modifier = Modifier.fillMaxWidth(),
                painter = painterResource(UiRes.drawable.confetti),
                contentDescription = null,
            )

            SectionContainer {
                Image(
                    modifier = Modifier.size(80.dp),
                    painter = painterResource(UiRes.drawable.ic_premium_feature_crown),
                    contentDescription = null,
                )
            }

            SectionContainer {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing),
                ) {
                    ScreenTitle(text = stringResource(UiRes.string.title_successful_purchase))
                    Text(
                        stringResource(UiRes.string.subtitle_successful_purchase),
                        textAlign = TextAlign.Center,
                        style = AppTheme.typography.bodyExtraLarge,
                        color = AppTheme.colors.text.secondary,
                    )
                }
            }

            Divider(modifier = Modifier.fillMaxWidth())

            if (features.isNotEmpty()) {
                SectionContainer {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(UiRes.string.benefits),
                            style = AppTheme.typography.h4,
                            color = AppTheme.colors.text.primary,
                        )
                        Spacer(modifier = Modifier.height(AppTheme.spacing.verticalListItemSpacingSmall))
                        PremiumFeaturesList(features = features)
                    }
                }
                Divider(modifier = Modifier.fillMaxWidth())
            }

            ManageSubscriptionText(
                expirationDate = expirationDate,
                isLifetime = isLifetime,
                isRecurring = isRecurring,
                subscriptionUrl = subscriptionUrl,
            )
        }

        AppButton(
            text = stringResource(UiRes.string.btn_action_successful_purchase),
            modifier = Modifier.fillMaxWidth(),
            onClick = { onContinue() },
        )
    }
}

@Composable
@Preview
internal fun SuccessfulPurchaseContentPreview() {
    PreviewHelper {
        SuccessfulPurchaseContent(
            subscriptionUrl = "https://play.google.com/store/account/subscriptions",
            features =
            listOf(
                PremiumFeatureUiState(text = UiText.of("Feature 1"), isIncluded = true),
                PremiumFeatureUiState(text = UiText.of("Feature 2"), isIncluded = true),
                PremiumFeatureUiState(text = UiText.of("Feature 3"), isIncluded = true),
                PremiumFeatureUiState(text = UiText.of("Feature 4"), isIncluded = false),
            ),
            modifier = Modifier.height(900.dp),
            expirationDate = "27 September 2024",
            onContinue = {},
        )
    }
}
