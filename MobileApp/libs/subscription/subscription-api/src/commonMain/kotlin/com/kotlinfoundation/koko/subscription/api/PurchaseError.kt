package com.kotlinfoundation.koko.subscription.api

data class PurchaseError(
    val message: String,
    val code: String? = null,
)
