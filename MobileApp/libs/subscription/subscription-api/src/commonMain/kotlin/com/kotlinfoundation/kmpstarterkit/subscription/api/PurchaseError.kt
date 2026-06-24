package com.kotlinfoundation.kmpstarterkit.subscription.api

data class PurchaseError(
    val message: String,
    val code: String? = null,
)
