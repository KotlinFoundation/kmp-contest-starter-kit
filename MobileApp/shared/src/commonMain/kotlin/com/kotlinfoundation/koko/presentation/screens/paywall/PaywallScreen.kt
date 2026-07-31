package com.kotlinfoundation.koko.presentation.screens.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kotlinfoundation.koko.designsystem.components.LoadingProgress
import com.kotlinfoundation.koko.designsystem.components.LoadingProgressMode
import com.kotlinfoundation.koko.designsystem.components.modals.AppDialog
import com.kotlinfoundation.koko.designsystem.components.modals.DialogType
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.generated.resources.Res
import com.kotlinfoundation.koko.generated.resources.paywall_demo_banner
import com.kotlinfoundation.koko.generated.resources.paywall_demo_title
import com.kotlinfoundation.koko.presentation.components.premium.PremiumFeatureFactory
import com.kotlinfoundation.koko.presentation.components.premium.SuccessfulPurchaseView
import com.kotlinfoundation.koko.presentation.screens.paywall.creditpack.CreditPackPaywallScreen
import com.kotlinfoundation.koko.presentation.screens.paywall.subscription.SubscriptionPaywallScreen
import com.kotlinfoundation.koko.util.extensions.asFormattedDate
import org.jetbrains.compose.resources.stringResource

@Composable
fun PaywallScreen(
    modifier: Modifier = Modifier,
    viewModel: PaywallViewModel,
    onDismiss: () -> Unit,
    onSignInRequired: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.signInActionRequired) {
        if (uiState.signInActionRequired) {
            onSignInRequired()
            viewModel.onSignInActionHandled()
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
            onContinue = { onDismiss() },
        )
        return
    }

    if (uiState.errorMessage?.value.isNullOrEmpty().not()) {
        AppDialog(
            type = DialogType.ERROR,
            text = uiState.errorMessage?.value,
            onConfirm = { viewModel.onMessageShown() },
        )
    }

    PaywallScreen(
        modifier = modifier.fillMaxSize().background(AppTheme.colors.background),
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
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
    var showDemoDialog by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isMock) {
        if (uiState.isMock) showDemoDialog = true
    }
    if (showDemoDialog) {
        AppDialog(
            type = DialogType.ERROR,
            title = stringResource(Res.string.paywall_demo_title),
            text = stringResource(Res.string.paywall_demo_banner),
            onConfirm = { showDemoDialog = false },
            onDismiss = { showDemoDialog = false },
        )
    }

    Column(modifier = modifier) {
        when {
            uiState.isLoading -> Box(modifier = Modifier.weight(1f)) {
                LoadingProgress(mode = LoadingProgressMode.FULLSCREEN)
            }

            uiState.mode == PaywallMode.CREDIT_PACK -> CreditPackPaywallScreen(
                modifier = Modifier.weight(1f),
                uiState = uiState,
                onUiEvent = onUiEvent,
                onDismiss = onDismiss,
            )

            else -> SubscriptionPaywallScreen(
                modifier = Modifier.weight(1f),
                uiState = uiState,
                onUiEvent = onUiEvent,
                onDismiss = onDismiss,
            )
        }
    }
}

@Preview
@Composable
private fun PaywallScreenPreview() {
    AppTheme {
        PaywallScreen(
            uiState = PaywallPreviewData.subscriptionState(),
            onUiEvent = {},
            onDismiss = {},
        )
    }
}
