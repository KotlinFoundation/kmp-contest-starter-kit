package com.kotlinfoundation.koko.subscription.api

data class GrantedAccess(
    val id: String,
    val expirationDateMillis: Long?,
    val willRenew: Boolean,
    val isLifetime: Boolean = false,
    val productIdentifier: String,
    val details: Details? = null,
) {
    data class Details(
        val title: String,
        val price: Price,
        /** Billing cycle of the purchased plan. `null` for malformed / opaque
         *  custom-period subscriptions where the provider didn't surface a unit. */
        val period: BillingPeriod?,
    )
}
