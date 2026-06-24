package com.kotlinfoundation.kmpstarterkit.presentation.components.premium

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.kotlinfoundation.kmpstarterkit.designsystem.components.premium.PremiumFeatureUiState
import com.kotlinfoundation.kmpstarterkit.designsystem.components.premium.SuccessfulPurchaseContent
import com.kotlinfoundation.kmpstarterkit.util.Constants.subscriptionUrl
import com.kotlinfoundation.kmpstarterkit.util.inappreview.rememberInAppReviewTrigger
import com.kotlinfoundation.kmpstarterkit.util.logging.AppLogger
import com.kotlinfoundation.kmpstarterkit.util.logging.logSuccessfulPurchase

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
