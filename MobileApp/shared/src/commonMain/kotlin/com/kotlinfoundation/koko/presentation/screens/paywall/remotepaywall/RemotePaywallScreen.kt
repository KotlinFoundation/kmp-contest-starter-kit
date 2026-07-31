package com.kotlinfoundation.koko.presentation.screens.paywall.remotepaywall

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kotlinfoundation.koko.designsystem.components.LoadingProgress
import com.kotlinfoundation.koko.designsystem.components.LoadingProgressMode
import com.kotlinfoundation.koko.designsystem.components.modals.AppDialog
import com.kotlinfoundation.koko.designsystem.components.modals.DialogType
import com.kotlinfoundation.koko.presentation.components.premium.PremiumFeatureFactory
import com.kotlinfoundation.koko.presentation.components.premium.SuccessfulPurchaseView
import com.kotlinfoundation.koko.presentation.screens.paywall.PaywallViewModel
import com.kotlinfoundation.koko.subscription.api.SubscriptionProviderUi
import com.kotlinfoundation.koko.util.extensions.asFormattedDate
import org.koin.compose.koinInject

@Composable
fun RemotePaywallScreen(
    onDismiss: () -> Unit,
    onSignInRequired: () -> Unit,
    viewModel: PaywallViewModel,
) {
    val subscriptionProviderUi = koinInject<SubscriptionProviderUi>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isPaywallVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isPaywallVisible = true
    }

    LaunchedEffect(uiState.isDismissRequired) {
        if (uiState.isDismissRequired) {
            onDismiss()
        }
    }

    LaunchedEffect(uiState.signInActionRequired) {
        if (uiState.signInActionRequired) {
            onSignInRequired()
            viewModel.onSignInActionHandled()
        }
    }

    if (uiState.errorMessage?.value.isNullOrEmpty().not()) {
        AppDialog(
            type = DialogType.ERROR,
            text = uiState.errorMessage?.value,
            onConfirm = {
                viewModel.onMessageShown()
                onDismiss()
            },
        )
    }

    uiState.successfulSubscription?.let { subscription ->
        SuccessfulPurchaseView(
            modifier = Modifier.fillMaxSize(),
            features = PremiumFeatureFactory.ofSubscription(subscription),
            isRecurring = subscription.willRenew,
            isLifetime = subscription.isLifetime,
            expirationDate = subscription.expirationDateInMillis?.asFormattedDate(),
            onContinue = { onDismiss() },
        )
    }

    if (isPaywallVisible) {
        key(uiState.currentPlacementId) {
            subscriptionProviderUi.RemotePaywall(
                listener = viewModel.remotePaywallPurchaseEventsListener,
                placementId = uiState.currentPlacementId,
            )
        }
    }

    if (uiState.isLoading || !isPaywallVisible) LoadingProgress(mode = LoadingProgressMode.FULLSCREEN)
}
