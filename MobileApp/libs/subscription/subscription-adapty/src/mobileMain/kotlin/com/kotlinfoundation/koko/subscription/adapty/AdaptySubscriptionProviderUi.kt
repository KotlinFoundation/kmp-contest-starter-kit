package com.kotlinfoundation.koko.subscription.adapty

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.adapty.kmp.Adapty
import com.adapty.kmp.models.AdaptyPaywall
import com.adapty.kmp.models.AdaptyPaywallFetchPolicy
import com.adapty.kmp.models.AdaptyPurchaseResult
import com.adapty.kmp.models.AdaptyUIAction
import com.adapty.kmp.models.exceptionOrNull
import com.adapty.kmp.models.getOrNull
import com.adapty.kmp.ui.AdaptyUIPaywallPlatformView
import com.kotlinfoundation.koko.subscription.api.PurchaseError
import com.kotlinfoundation.koko.subscription.api.PurchaseEventsListener
import com.kotlinfoundation.koko.subscription.api.SubscriptionProviderUi
import kotlin.time.Duration.Companion.minutes

class AdaptySubscriptionProviderUi : SubscriptionProviderUi {
    @Composable
    override fun RemotePaywall(
        placementId: String?,
        listener: PurchaseEventsListener,
    ) {
        var adaptyPaywall: AdaptyPaywall? by remember { mutableStateOf(null) }

        LaunchedEffect(Unit) {
            listener.onLoadingStateChanged(true)
            val currentPlacementId = placementId ?: ADAPTY_DEFAULT_PLACEMENT_ID
            val paywallResult =
                Adapty.getPaywall(
                    placementId = currentPlacementId,
                    fetchPolicy = AdaptyPaywallFetchPolicy.ReturnCacheDataIfNotExpiredElseLoad(5.minutes.inWholeMilliseconds),
                )
            adaptyPaywall = paywallResult.getOrNull()
            if (adaptyPaywall == null) {
                listener.onUnknownError(
                    paywallResult.exceptionOrNull()
                        ?: Exception("Paywall is not found for placementId: $currentPlacementId"),
                )
            }
            listener.onLoadingStateChanged(false)
        }

        adaptyPaywall?.let { paywall ->
            AdaptyUIPaywallPlatformView(
                modifier = Modifier.fillMaxSize(),
                paywall = paywall,
                onDidPerformAction = { view, action ->
                    if (action is AdaptyUIAction.CloseAction) {
                        listener.onDismiss()
                    }
                },
                onDidFinishPurchase = { view, product, result ->
                    when (result) {
                        is AdaptyPurchaseResult.Success -> {
                            listener.onPurchaseSuccess(
                                info = result.profile.asSubscriptionProviderUser(),
                                productIds = listOf(product.vendorProductId),
                            )
                        }

                        else -> {}
                    }
                },
                onDidFailPurchase = { view, product, error ->
                    listener.onPurchaseFailure(PurchaseError(error.message))
                },
                onDidFinishRestore = { view, profile ->
                    val isActiveAccessLevelsEmpty =
                        profile.accessLevels.filter { it.value.isActive }.isEmpty()
                    if (isActiveAccessLevelsEmpty) {
                        listener.onRestoreFailure(PurchaseError("Restore failed. No active subscription found"))
                    } else {
                        listener.onRestoreSuccess(profile.asSubscriptionProviderUser())
                    }
                },
                onDidFailRestore = { view, error ->
                    listener.onRestoreFailure(PurchaseError(message = error.message))
                },
                onDidFailRendering = { view, error ->
                    listener.onUnknownError(error)
                },
                onDidFailLoadingProducts = { view, error ->
                    listener.onUnknownError(error)
                },
            )
        }
    }
}
