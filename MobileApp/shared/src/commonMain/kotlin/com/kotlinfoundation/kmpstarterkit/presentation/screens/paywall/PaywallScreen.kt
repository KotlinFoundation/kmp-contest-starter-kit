package com.kotlinfoundation.kmpstarterkit.presentation.screens.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kotlinfoundation.kmpstarterkit.designsystem.components.LoadingProgress
import com.kotlinfoundation.kmpstarterkit.designsystem.components.LoadingProgressMode
import com.kotlinfoundation.kmpstarterkit.designsystem.components.modals.AppDialog
import com.kotlinfoundation.kmpstarterkit.designsystem.components.modals.DialogType
import com.kotlinfoundation.kmpstarterkit.designsystem.theme.AppTheme
import com.kotlinfoundation.kmpstarterkit.presentation.components.premium.PremiumFeatureFactory
import com.kotlinfoundation.kmpstarterkit.presentation.components.premium.SuccessfulPurchaseView
import com.kotlinfoundation.kmpstarterkit.presentation.screens.paywall.creditpack.CreditPackPaywallScreen
import com.kotlinfoundation.kmpstarterkit.presentation.screens.paywall.subscription.SubscriptionPaywallScreen
import com.kotlinfoundation.kmpstarterkit.util.extensions.asFormattedDate

@Composable
fun PaywallScreen(
    modifier: Modifier = Modifier,
    uiStateHolder: PaywallUiStateHolder,
    onDismiss: () -> Unit,
    onSignInRequired: () -> Unit,
) {
    val uiState by uiStateHolder.uiState.collectAsStateWithLifecycle()

    if (uiState.signInActionRequired) {
        LaunchedEffect(uiState.signInActionRequired) {
            onSignInRequired()
            uiStateHolder.onSignInActionHandled()
        }
    }

    LaunchedEffect(uiState.isDismissRequired) {
        if (uiState.isDismissRequired) {
            onDismiss()
        }
    }

    uiState.successfulSubscription?.let { subscription ->
        SuccessfulPurchaseView(
            modifier = modifier.fillMaxSize().background(AppTheme.colors.background),
            features = PremiumFeatureFactory.ofSubscription(subscription),
            isRecurring = subscription.willRenew,
            isLifetime = subscription.isLifetime,
            expirationDate = subscription.expirationDateInMillis?.asFormattedDate(),
            onContinue = {
                onDismiss()
                uiStateHolder.onSuccessfulPurchaseHandled()
            },
        )
        return
    }

    if (uiState.errorMessage?.value.isNullOrEmpty().not()) {
        AppDialog(
            type = DialogType.ERROR,
            text = uiState.errorMessage?.value,
            onConfirm = { uiStateHolder.onMessageShown() },
        )
    }

    PaywallScreen(
        modifier = modifier.fillMaxSize().background(AppTheme.colors.background),
        uiState = uiState,
        onUiEvent = uiStateHolder::onUiEvent,
        onDismiss = onDismiss,
    )
}

@Composable
fun PaywallScreen(
    modifier: Modifier = Modifier,
    uiState: PaywallUiState,
    onUiEvent: (PaywallUiEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    when {
        uiState.isLoading -> LoadingProgress(mode = LoadingProgressMode.FULLSCREEN)

        uiState.mode == PaywallMode.CREDIT_PACK -> CreditPackPaywallScreen(
            modifier = modifier,
            uiState = uiState,
            onUiEvent = onUiEvent,
            onDismiss = onDismiss,
        )

        else -> SubscriptionPaywallScreen(
            modifier = modifier,
            uiState = uiState,
            onUiEvent = onUiEvent,
            onDismiss = onDismiss,
        )
    }
}
