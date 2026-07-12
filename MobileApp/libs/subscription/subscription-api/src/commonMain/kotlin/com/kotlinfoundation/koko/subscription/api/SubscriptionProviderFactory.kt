package com.kotlinfoundation.koko.subscription.api

interface SubscriptionProviderFactory {
    companion object {}

    fun createProvider(): SubscriptionProvider

    fun createProviderUi(): SubscriptionProviderUi
}
