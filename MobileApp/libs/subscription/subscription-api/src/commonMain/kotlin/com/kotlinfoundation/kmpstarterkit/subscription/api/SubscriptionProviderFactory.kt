package com.kotlinfoundation.kmpstarterkit.subscription.api

interface SubscriptionProviderFactory {
    companion object {}

    fun createProvider(): SubscriptionProvider

    fun createProviderUi(): SubscriptionProviderUi
}
