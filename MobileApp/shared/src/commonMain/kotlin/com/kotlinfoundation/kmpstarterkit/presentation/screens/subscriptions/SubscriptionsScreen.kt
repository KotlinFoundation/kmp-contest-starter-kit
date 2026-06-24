package com.kotlinfoundation.kmpstarterkit.presentation.screens.subscriptions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kotlinfoundation.kmpstarterkit.designsystem.components.LoadingProgress
import com.kotlinfoundation.kmpstarterkit.designsystem.components.LoadingProgressMode
import com.kotlinfoundation.kmpstarterkit.designsystem.components.ScreenWithToolbar
import com.kotlinfoundation.kmpstarterkit.designsystem.components.premium.CurrentSubscriptionPlanAndFeatures
import com.kotlinfoundation.kmpstarterkit.designsystem.components.premium.ManageSubscriptionText
import com.kotlinfoundation.kmpstarterkit.designsystem.components.premium.UpgradePremiumBanner
import com.kotlinfoundation.kmpstarterkit.designsystem.components.premium.UpgradePremiumBannerStyle
import com.kotlinfoundation.kmpstarterkit.designsystem.generated.resources.UiRes
import com.kotlinfoundation.kmpstarterkit.designsystem.generated.resources.ic_back
import com.kotlinfoundation.kmpstarterkit.designsystem.theme.AppTheme
import com.kotlinfoundation.kmpstarterkit.domain.model.Subscription
import com.kotlinfoundation.kmpstarterkit.generated.resources.Res
import com.kotlinfoundation.kmpstarterkit.generated.resources.paywall_unit_day
import com.kotlinfoundation.kmpstarterkit.generated.resources.paywall_unit_lifetime
import com.kotlinfoundation.kmpstarterkit.generated.resources.paywall_unit_month
import com.kotlinfoundation.kmpstarterkit.generated.resources.paywall_unit_month_count
import com.kotlinfoundation.kmpstarterkit.generated.resources.paywall_unit_week
import com.kotlinfoundation.kmpstarterkit.generated.resources.paywall_unit_year
import com.kotlinfoundation.kmpstarterkit.generated.resources.subscriptions
import com.kotlinfoundation.kmpstarterkit.generated.resources.subscriptions_plan_free
import com.kotlinfoundation.kmpstarterkit.presentation.components.premium.PremiumFeatureFactory
import com.kotlinfoundation.kmpstarterkit.util.Constants.subscriptionUrl
import com.kotlinfoundation.kmpstarterkit.util.extensions.asFormattedDate
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SubscriptionsScreen(
    modifier: Modifier = Modifier,
    uiStateHolder: SubscriptionsUiStateHolder,
    onNavigatePaywall: () -> Unit,
    onClickBack: () -> Unit,
) {
    val uiState by uiStateHolder.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        LoadingProgress(mode = LoadingProgressMode.FULLSCREEN)
    } else {
        ScreenWithToolbar(
            modifier = modifier.fillMaxSize().background(AppTheme.colors.background),
            title = stringResource(Res.string.subscriptions),
            navigationIcon = UiRes.drawable.ic_back,
            includeBottomInsets = true,
            isScrollableContent = true,
            onNavigationIconClick = onClickBack,
        ) {
            SubscriptionsScreen(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                onUiEvent = uiStateHolder::onUiEvent,
                onClickUpgradePremium = { onNavigatePaywall() },
            )
        }
    }
}

@Composable
private fun SubscriptionsScreen(
    modifier: Modifier = Modifier,
    uiState: SubscriptionsUiState,
    onUiEvent: (SubscriptionsUiEvent) -> Unit,
    onClickUpgradePremium: () -> Unit = {},
) {
    val topPadding =
        if (uiState.showUpgradePremiumBanner) {
            AppTheme.spacing.defaultSpacing
        } else {
            0.dp
        }
    Column(
        modifier = modifier.padding(top = topPadding),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
    ) {
        if (uiState.showUpgradePremiumBanner) {
            UpgradePremiumBanner(
                style = UpgradePremiumBannerStyle.LARGE,
                onClick = { onClickUpgradePremium() },
            )
        }

        CurrentSubscriptionPlanAndFeatures(
            name = uiState.currentPlan?.name ?: stringResource(Res.string.subscriptions_plan_free),
            features = PremiumFeatureFactory.ofSubscription(uiState.currentPlan),
            price = uiState.currentPlan?.formattedPrice,
            duration = when (uiState.currentPlan?.durationType) {
                Subscription.DurationType.DAILY -> pluralStringResource(Res.plurals.paywall_unit_day, 1)
                Subscription.DurationType.WEEKLY -> pluralStringResource(Res.plurals.paywall_unit_week, 1)
                Subscription.DurationType.MONTHLY -> pluralStringResource(Res.plurals.paywall_unit_month, 1)
                Subscription.DurationType.TWO_MONTHS -> pluralStringResource(Res.plurals.paywall_unit_month_count, 2, 2)
                Subscription.DurationType.THREE_MONTHS -> pluralStringResource(Res.plurals.paywall_unit_month_count, 3, 3)
                Subscription.DurationType.SIX_MONTHS -> pluralStringResource(Res.plurals.paywall_unit_month_count, 6, 6)
                Subscription.DurationType.YEARLY -> pluralStringResource(Res.plurals.paywall_unit_year, 1)
                Subscription.DurationType.LIFETIME -> stringResource(Res.string.paywall_unit_lifetime)
                else -> null
            },
        )

        if (uiState.currentPlan != null) {
            ManageSubscriptionText(
                isLifetime = uiState.currentPlan.isLifetime,
                isRecurring = uiState.currentPlan.willRenew,
                expirationDate = uiState.currentPlan.expirationDateInMillis?.asFormattedDate(),
                subscriptionUrl = subscriptionUrl,
            )
        }
    }
}
