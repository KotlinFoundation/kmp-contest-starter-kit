@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.kotlinfoundation.koko.presentation.screens.creditbalance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kotlinfoundation.koko.designsystem.components.*
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_back
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_coin_credits
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_crown
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_sparkles
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.domain.model.credit.CreditTransaction
import com.kotlinfoundation.koko.generated.resources.*
import com.kotlinfoundation.koko.presentation.components.credit.CreditTransactionsList
import com.kotlinfoundation.koko.presentation.components.credit.RecurringCreditsStatusBox
import com.kotlinfoundation.koko.util.Constants
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreditBalanceScreen(
    modifier: Modifier = Modifier,
    viewModel: CreditBalanceViewModel,
    onPurchaseRequired: (placementId: String?) -> Unit,
    onClickBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isPremiumRequired) {
        if (uiState.isPremiumRequired) {
            onPurchaseRequired(null)
            viewModel.onPremiumRequiredHandled()
        }
    }

    LaunchedEffect(uiState.isMoreCreditRequired) {
        if (uiState.isMoreCreditRequired) {
            onPurchaseRequired(Constants.PAYWALL_PLACEMENT_CREDITS_PACK)
            viewModel.onMoreCreditRequiredHandled()
        }
    }

    ScreenWithToolbar(
        modifier = modifier.fillMaxSize().background(AppTheme.colors.background),
        title = stringResource(Res.string.title_screen_credits),
        navigationIcon = UiRes.drawable.ic_back,
        includeBottomInsets = true,
        isScrollableContent = true,
        onNavigationIconClick = onClickBack,
    ) {
        CreditBalanceScreen(
            modifier = Modifier.fillMaxSize(),
            uiState = uiState,
            onUiEvent = viewModel::onUiEvent,
        )
    }
}

@Composable
private fun CreditBalanceScreen(
    modifier: Modifier = Modifier,
    uiState: CreditBalanceUiState,
    onUiEvent: (CreditBalanceUiEvent) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
    ) {
        var isTransactionListExpanded by rememberSaveable { mutableStateOf(false) }

        CreditBalanceHeroSection(
            creditBalance = uiState.creditBalance,
            isPremiumUser = uiState.isPremiumUser,
        )

        CreditActionsSection(
            isPremiumUser = uiState.isPremiumUser,
            onUpgradeToPremium = { onUiEvent(CreditBalanceUiEvent.UpgradeToPremium) },
            onBuyCreditPack = { onUiEvent(CreditBalanceUiEvent.BuyCreditPack) },
        )

        RecurringCreditsStatusBox(recurringCredits = uiState.recurringCredits)

        CreditCostInfoSection()

        TransactionHistorySection(
            transactions = uiState.lastTransactions,
            isExpanded = isTransactionListExpanded,
            onToggleExpanded = { isTransactionListExpanded = !isTransactionListExpanded },
        )
    }
}

@Composable
private fun CreditBalanceHeroSection(
    creditBalance: Int,
    isPremiumUser: Boolean,
) {
    AppCardContainer(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing),
        ) {
            IconCircle(icon = UiRes.drawable.ic_coin_credits, size = 56.dp, iconSize = 28.dp)

            Text(
                text = stringResource(Res.string.credit_available),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
                textAlign = TextAlign.Center,
            )

            Text(
                text = creditBalance.toString(),
                style = AppTheme.typography.h1,
                fontWeight = FontWeight.Bold,
                color = if (creditBalance > 0) AppTheme.colors.text.primary else AppTheme.colors.status.error,
                textAlign = TextAlign.Center,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (isPremiumUser) {
                    Icon(
                        painter = painterResource(UiRes.drawable.ic_crown),
                        contentDescription = null,
                        tint = AppTheme.colors.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Text(
                    text = stringResource(
                        if (isPremiumUser) Res.string.credit_status_premium else Res.string.credit_status_free,
                    ),
                    style = AppTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPremiumUser) AppTheme.colors.primary else AppTheme.colors.text.secondary,
                )
            }

            Text(
                text = stringResource(motivationalMessageRes(creditBalance)),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = AppTheme.spacing.defaultSpacing),
            )
        }
    }
}

@Composable
private fun CreditActionsSection(
    isPremiumUser: Boolean,
    onUpgradeToPremium: () -> Unit,
    onBuyCreditPack: () -> Unit,
) {
    if (isPremiumUser) {
        // Premium users only need the top-up action.
        CreditActionCard(
            modifier = Modifier.fillMaxWidth(),
            icon = UiRes.drawable.ic_coin_credits,
            title = stringResource(Res.string.credit_top_up_title),
            subtitle = stringResource(Res.string.credit_top_up_subtitle),
            onClick = onBuyCreditPack,
        )
    } else {
        // Upgrade and Top Up share the exact same card style — neither looks "selected".
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing),
        ) {
            CreditActionCard(
                modifier = Modifier.weight(1f),
                icon = UiRes.drawable.ic_crown,
                title = stringResource(Res.string.credit_upgrade_title),
                subtitle = stringResource(Res.string.credit_upgrade_subtitle),
                onClick = onUpgradeToPremium,
            )
            CreditActionCard(
                modifier = Modifier.weight(1f),
                icon = UiRes.drawable.ic_coin_credits,
                title = stringResource(Res.string.credit_top_up_title),
                subtitle = stringResource(Res.string.credit_top_up_subtitle),
                onClick = onBuyCreditPack,
            )
        }
    }
}

@Composable
private fun CreditActionCard(
    icon: DrawableResource,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCardContainer(modifier = modifier, onClick = onClick) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacingSmall),
        ) {
            IconCircle(icon = icon, size = 40.dp, iconSize = 20.dp)
            Text(
                text = title,
                style = AppTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.text.primary,
            )
            Text(
                text = subtitle,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
        }
    }
}

@Composable
private fun CreditCostInfoSection() {
    AppCardContainer(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
        ) {
            IconCircle(icon = UiRes.drawable.ic_sparkles, size = 40.dp, iconSize = 20.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.credit_cost_title),
                    style = AppTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.text.primary,
                )
                Text(
                    text = stringResource(Res.string.credit_cost_subtitle),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
            }
        }
    }
}

@Composable
private fun TransactionHistorySection(
    transactions: List<CreditTransaction>,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
) {
    AppCardContainer(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.credit_transaction_history),
                    style = AppTheme.typography.h5,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.text.primary,
                )

                AppButton(
                    text = stringResource(if (isExpanded) Res.string.btn_hide else Res.string.btn_show),
                    style = ButtonStyle.TEXT,
                    size = ButtonSize.SMALL,
                    // This 12 dp extra padding comes from Material Design so we remove that
                    modifier = Modifier.offset(x = 12.dp),
                    onClick = onToggleExpanded,
                )
            }

            AnimatedVisibility(isExpanded) {
                CreditTransactionsList(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    transactions = transactions,
                )
            }
        }
    }
}

@Composable
private fun IconCircle(
    icon: DrawableResource,
    size: Dp,
    iconSize: Dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(AppTheme.colors.primary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = AppTheme.colors.primary,
            modifier = Modifier.size(iconSize),
        )
    }
}

private fun motivationalMessageRes(credits: Int): StringResource = when {
    credits == 0 -> Res.string.credit_motivational_zero
    credits == 1 -> Res.string.credit_motivational_one
    credits <= 5 -> Res.string.credit_motivational_low
    else -> Res.string.credit_motivational_plenty
}

@Preview
@Composable
private fun CreditBalanceScreenPreview() {
    AppTheme {
        CreditBalanceScreen(uiState = CreditBalanceUiState(), onUiEvent = {})
    }
}
