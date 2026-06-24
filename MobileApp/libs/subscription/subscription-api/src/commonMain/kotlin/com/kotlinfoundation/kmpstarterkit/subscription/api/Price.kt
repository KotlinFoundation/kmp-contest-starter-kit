package com.kotlinfoundation.kmpstarterkit.subscription.api

data class Price(
    val amount: Float,
    val currencyCodeOrSymbol: String? = null,
    val localizedString: String? = null,
)
