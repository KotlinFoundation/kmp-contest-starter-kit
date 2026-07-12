package com.kotlinfoundation.koko.presentation.components.premium

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.kotlinfoundation.koko.designsystem.components.premium.PremiumFeatureUiState
import com.kotlinfoundation.koko.designsystem.components.premium.SuccessfulPurchaseContent
import com.kotlinfoundation.koko.util.Constants.subscriptionUrl
import com.kotlinfoundation.koko.util.inappreview.rememberInAppReviewTrigger
import com.kotlinfoundation.koko.util.logging.AppLogger
import com.kotlinfoundation.koko.util.logging.logSuccessfulPurchase

@Composable
fun SuccessfulPurchaseView(
    features: List<PremiumFeatureUiState> = emptyList(),
    expirationDate: String? = null,
    isLifetime: Boolean = false,
    isRecurring: Boolean = true,
    modifier: Modifier = Modifier,
    onContinue: () -> Unit = {},
) {
    val inAppReviewTrigger = rememberInAppReviewTrigger()
    LaunchedEffect(Unit) {
        AppLogger.logSuccessfulPurchase()
        inAppReviewTrigger.triggerAfterSuccessfulPurchase()
    }

    SuccessfulPurchaseContent(
        subscriptionUrl = subscriptionUrl,
        features = features,
        expirationDate = expirationDate,
        isLifetime = isLifetime,
        isRecurring = isRecurring,
        modifier = modifier,
        onContinue = onContinue,
    )
}
