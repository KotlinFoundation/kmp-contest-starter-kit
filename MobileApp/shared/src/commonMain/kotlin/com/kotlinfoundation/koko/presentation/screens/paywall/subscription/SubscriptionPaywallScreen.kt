
package com.kotlinfoundation.koko.presentation.screens.paywall.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.components.AppButton
import com.kotlinfoundation.koko.designsystem.components.ButtonStyle
import com.kotlinfoundation.koko.designsystem.components.ScreenWithToolbar
import com.kotlinfoundation.koko.designsystem.components.premium.PremiumFeatureUiState
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_check
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_close
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_crown
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.generated.resources.Res
import com.kotlinfoundation.koko.generated.resources.btn_skip
import com.kotlinfoundation.koko.generated.resources.paywall_btn_restore
import com.kotlinfoundation.koko.generated.resources.paywall_sub_subtitle
import com.kotlinfoundation.koko.generated.resources.paywall_sub_title
import com.kotlinfoundation.koko.presentation.components.premium.PremiumFeatureFactory
import com.kotlinfoundation.koko.presentation.screens.paywall.PaywallPackageUiState
import com.kotlinfoundation.koko.presentation.screens.paywall.PaywallPreviewData
import com.kotlinfoundation.koko.presentation.screens.paywall.PaywallUiEvent
import com.kotlinfoundation.koko.presentation.screens.paywall.PaywallUiState
import com.kotlinfoundation.koko.presentation.screens.paywall.creditpack.FooterLinksRow
import com.kotlinfoundation.koko.subscription.api.PurchasePackageId
import com.kotlinfoundation.koko.util.StoreScreenshot
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SubscriptionPaywallScreen(
    uiState: PaywallUiState,
    onUiEvent: (PaywallUiEvent) -> Unit,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    ScreenWithToolbar(
        modifier = modifier.fillMaxSize().background(AppTheme.colors.background),
        title = "",
        includeBottomInsets = true,
        navigationIcon = UiRes.drawable.ic_close,
        onNavigationIconClick = onDismiss,
        toolbarExtraContent = {
            RestoreActionButton(onClick = { onUiEvent(PaywallUiEvent.OnClickRestore) })
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
            ) {
                Hero()
                ValueRowsSection()
                if (uiState.packages.isNotEmpty()) {
                    PricingSelector(
                        packages = uiState.packages,
                        onSelect = { id -> onUiEvent(PaywallUiEvent.OnSelectPackage(id)) },
                    )
                }
                Spacer(modifier = Modifier.height(AppTheme.spacing.defaultSpacing))
            }
            StickyFooter(
                buyButtonEnabled = uiState.buyButtonEnabled,
                ctaText = uiState.ctaText.value,
                aboveCtaText = uiState.aboveCtaText.value,
                belowCtaText = uiState.belowCtaText?.value,
                onBuy = { onUiEvent(PaywallUiEvent.OnClickBuy) },
            )
        }
    }
}

@Composable
private fun RestoreAction(onClick: () -> Unit) {
    Text(
        text = stringResource(Res.string.paywall_btn_restore),
        style = AppTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = AppTheme.colors.text.secondary,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Composable
private fun RestoreActionButton(onClick: () -> Unit) {
    TextButton(
        // This 12 dp extra padding comes from Material Design so we remove that
        modifier = Modifier.offset(x = 12.dp),
        onClick = { onClick() },
    ) {
        Text(
            text = stringResource(Res.string.paywall_btn_restore),
            style = AppTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = AppTheme.colors.text.secondary,
        )
    }
}

// ── Hero ─────────────────────────────────────────────────────────────────────

@Composable
private fun Hero() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.cardContentSpacing),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(UiRes.drawable.ic_crown),
                contentDescription = null,
                tint = AppTheme.colors.primary,
                modifier = Modifier.size(44.dp),
            )
        }
        Text(
            text = stringResource(Res.string.paywall_sub_title),
            style = AppTheme.typography.h3,
            color = AppTheme.colors.text.primary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(Res.string.paywall_sub_subtitle),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.text.secondary,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Value rows ───────────────────────────────────────────────────────────────

@Composable
private fun ValueRowsSection() {
    val features: List<PremiumFeatureUiState> = remember { PremiumFeatureFactory.defaultPremiumFeatures }
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing)) {
        features.forEach { feature -> ValueRow(text = feature.text.value) }
    }
}

@Composable
private fun ValueRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(UiRes.drawable.ic_check),
                contentDescription = null,
                tint = AppTheme.colors.primary,
                modifier = Modifier.size(13.dp),
            )
        }
        Text(
            text = text,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.text.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
    }
}

// ── Pricing selector ─────────────────────────────────────────────────────────

@Composable
private fun PricingSelector(
    packages: List<PaywallPackageUiState>,
    onSelect: (PurchasePackageId) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing)) {
        packages.forEach { pkg ->
            PricingCard(
                pkg = pkg,
                onClick = { onSelect(pkg.id) },
            )
        }
    }
}

@Composable
private fun PricingCard(
    pkg: PaywallPackageUiState,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    val containerColor =
        if (pkg.isSelected) AppTheme.colors.primary.copy(alpha = 0.08f) else AppTheme.colors.surfaceContainer
    val borderColor = when {
        pkg.isSelected || pkg.isRecommended -> AppTheme.colors.primary
        else -> AppTheme.colors.outline
    }
    val borderWidth = if (pkg.isSelected) 2.dp else 1.dp

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(containerColor)
                .border(width = borderWidth, color = borderColor, shape = shape)
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = if (pkg.isRecommended) 18.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SelectedDot(isSelected = pkg.isSelected)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pkg.title.value,
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.text.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = pkg.subtitle.value,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
            }
            Text(
                text = pkg.priceText.value,
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.text.primary,
                fontWeight = FontWeight.Bold,
            )
        }
        if (pkg.savingsBadge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                    .background(AppTheme.colors.primary)
                    .padding(horizontal = 14.dp, vertical = 5.dp),
            ) {
                Text(
                    text = pkg.savingsBadge.value,
                    style = AppTheme.typography.bodyExtraSmall,
                    color = AppTheme.colors.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun SelectedDot(isSelected: Boolean) {
    val ringColor = if (isSelected) AppTheme.colors.primary else AppTheme.colors.outline
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (isSelected) AppTheme.colors.primary else AppTheme.colors.surfaceContainer)
            .border(width = 2.dp, color = ringColor, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Icon(
                painter = painterResource(UiRes.drawable.ic_check),
                contentDescription = null,
                tint = AppTheme.colors.onPrimary,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

// ── Sticky footer ────────────────────────────────────────────────────────────

@Composable
private fun StickyFooter(
    buyButtonEnabled: Boolean,
    ctaText: String,
    aboveCtaText: String,
    belowCtaText: String?,
    onBuy: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = aboveCtaText,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.text.primary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing.groupedVerticalElementSpacing))
        AppButton(
            text = ctaText,
            style = ButtonStyle.PRIMARY,
            enabled = buyButtonEnabled,
            onClick = onBuy,
            modifier = Modifier.fillMaxWidth(),
        )
        if (!belowCtaText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(AppTheme.spacing.groupedVerticalElementSpacingSmall))
            Text(
                text = belowCtaText,
                style = AppTheme.typography.bodyExtraSmall,
                color = AppTheme.colors.text.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.height(AppTheme.spacing.defaultSpacing))
        FooterLinksRow()
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview
@StoreScreenshot(locale = "en", tag = "paywall_review_screenshot_subscription")
@Composable
private fun SubscriptionPaywallStoreScreenshot_NoTrial_iPhone_en() {
    AppTheme {
        SubscriptionPaywallScreen(
            uiState = PaywallPreviewData.subscriptionState(trialAvailable = false),
            onUiEvent = {},
        )
    }
}

@Preview
@Composable
private fun SubscriptionPaywallStoreScreenshot_Trial_iPhone_en() {
    AppTheme {
        SubscriptionPaywallScreen(
            uiState = PaywallPreviewData.subscriptionState(trialAvailable = true),
            onUiEvent = {},
        )
    }
}

@Preview
@Composable
private fun SubscriptionPaywallStoreScreenshot_PaidIntro_iPhone_en() {
    AppTheme {
        SubscriptionPaywallScreen(uiState = PaywallPreviewData.paidIntroSubscriptionState(), onUiEvent = {})
    }
}
