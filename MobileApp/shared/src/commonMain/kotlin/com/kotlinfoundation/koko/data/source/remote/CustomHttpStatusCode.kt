package com.kotlinfoundation.koko.data.source.remote

// App-specific (non-standard) API status codes the backend returns to signal billing gates.
// Mapped to PurchaseRequiredException / CreditRequiredException at the data boundary.
object CustomHttpStatusCode {
    const val PURCHASE_REQUIRED = 1002
    const val CREDIT_REQUIRED = 1003
}
