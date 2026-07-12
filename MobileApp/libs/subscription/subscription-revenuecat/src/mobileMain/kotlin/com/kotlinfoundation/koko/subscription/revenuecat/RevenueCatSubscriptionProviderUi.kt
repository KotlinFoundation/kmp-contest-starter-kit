package com.kotlinfoundation.koko.subscription.revenuecat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kotlinfoundation.koko.subscription.api.PurchaseError
import com.kotlinfoundation.koko.subscription.api.PurchaseEventsListener
import com.kotlinfoundation.koko.subscription.api.SubscriptionProviderUi
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.StoreTransaction
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions
import kotlinx.coroutines.ensureActive

class RevenueCatSubscriptionProviderUi : SubscriptionProviderUi {
    @Composable
    override fun RemotePaywall(
        placementId: String?,
        listener: PurchaseEventsListener,
    ) {
        var currentOffering: Offering? by remember { mutableStateOf(null) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(isLoading) {
            listener.onLoadingStateChanged(isLoading)
        }

        val revenueCatPurchaseListener =
            remember {
                object : PaywallListener {
                    override fun onPurchaseCompleted(
                        customerInfo: CustomerInfo,
                        storeTransaction: StoreTransaction,
                    ) {
                        listener.onPurchaseSuccess(
                            info = customerInfo.asSubscriptionProviderUser(),
                            productIds = storeTransaction.productIds,
                        )
                    }

                    override fun onPurchaseError(error: PurchasesError) {
                        listener.onPurchaseFailure(PurchaseError(message = error.message))
                    }

                    override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                        val hasSuccessfulRestore =
                            customerInfo.entitlements.all.any { it.value.isActive }
                        if (!hasSuccessfulRestore) {
                            listener.onRestoreFailure(PurchaseError("Restore failed. No active subscription found"))
                        } else {
                            listener.onRestoreSuccess(customerInfo.asSubscriptionProviderUser())
                        }
                    }

                    override fun onRestoreError(error: PurchasesError) {
                        listener.onRestoreFailure(PurchaseError(message = error.message))
                    }
                }
            }

        LaunchedEffect(Unit) {
            isLoading = true
            if (placementId != null) {
                try {
                    val offerings = Purchases.sharedInstance.awaitOfferings()
                    currentOffering = offerings[placementId]
                    isLoading = false
                } catch (error: Exception) {
                    coroutineContext.ensureActive()
                    isLoading = false
                    listener.onUnknownError(error)
                }
            } else {
                isLoading = false
            }
        }

        val paywallOptions =
            remember(currentOffering) {
                PaywallOptions(dismissRequest = { listener.onDismiss() }) {
                    this.shouldDisplayDismissButton = true
                    this.offering = currentOffering
                    this.listener = revenueCatPurchaseListener
                }
            }

        if (!isLoading) {
            Paywall(options = paywallOptions)
        }
    }
}
