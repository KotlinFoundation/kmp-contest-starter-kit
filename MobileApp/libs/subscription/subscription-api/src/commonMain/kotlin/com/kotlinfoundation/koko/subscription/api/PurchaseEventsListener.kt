package com.kotlinfoundation.koko.subscription.api

interface PurchaseEventsListener {
    fun onPurchaseSuccess(
        info: SubscriptionProviderUser,
        productIds: List<String>,
    )

    fun onRestoreSuccess(info: SubscriptionProviderUser)

    fun onPurchaseFailure(error: PurchaseError)

    fun onRestoreFailure(error: PurchaseError)

    fun onDismiss() {}

    fun onLoadingStateChanged(isLoading: Boolean) {}

    fun onUnknownError(error: Exception) {}
}
