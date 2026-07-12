package com.kotlinfoundation.koko.data.subscription

import com.kotlinfoundation.koko.data.source.preferences.FakeUserPreferences
import com.kotlinfoundation.koko.subscription.api.MockSubscriptionProvider
import com.kotlinfoundation.koko.subscription.api.PurchasePackageId
import com.kotlinfoundation.koko.subscription.api.hasAccess
import com.kotlinfoundation.koko.util.Constants
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MockSubscriptionProviderTest {

    private val premiumKey = "mock_premium"

    private fun provider(preferences: FakeUserPreferences = FakeUserPreferences()) = MockSubscriptionProvider(
        readPremiumPurchased = { preferences.getBoolean(premiumKey, false) },
        writePremiumPurchased = { preferences.putBoolean(premiumKey, it) },
        premiumAccessId = Constants.PAYWALL_PREMIUM_ACCESS,
        creditPackPrefix = Constants.CREDIT_PACK_PRODUCT_ID_PREFIX,
        creditPackPlacementId = Constants.PAYWALL_PLACEMENT_CREDITS_PACK,
    )

    @Test
    fun subscriptionPlacementReturnsPackages() = runTest {
        val packages = provider().getPurchasePackages(placementId = null).getOrThrow()
        assertTrue(packages.isNotEmpty())
    }

    @Test
    fun creditPackPlacementReturnsCreditPackages() = runTest {
        val packages = provider().getPurchasePackages(Constants.PAYWALL_PLACEMENT_CREDITS_PACK).getOrThrow()
        assertTrue(packages.isNotEmpty())
        assertTrue(packages.all { it.id.value.startsWith(Constants.CREDIT_PACK_PRODUCT_ID_PREFIX) })
    }

    @Test
    fun subscriptionPurchaseGrantsPremium() = runTest {
        val provider = provider()
        provider.initialize(apiKey = "testValue")
        assertFalse(provider.hasAccess(Constants.PAYWALL_PREMIUM_ACCESS))

        val subscriptionId = provider.getPurchasePackages(placementId = null).getOrThrow().first().id
        provider.purchase(subscriptionId).getOrThrow()

        assertTrue(provider.hasAccess(Constants.PAYWALL_PREMIUM_ACCESS))
    }

    @Test
    fun creditPackPurchaseDoesNotGrantPremium() = runTest {
        val provider = provider()
        val creditPackId = PurchasePackageId("${Constants.CREDIT_PACK_PRODUCT_ID_PREFIX}30")

        val user = provider.purchase(creditPackId).getOrThrow()

        assertTrue(creditPackId.value in user.activeSubscriptionIds)
        assertFalse(provider.hasAccess(Constants.PAYWALL_PREMIUM_ACCESS))
    }

    @Test
    fun premiumUnlockPersistsAcrossInstances() = runTest {
        val preferences = FakeUserPreferences()
        val subscriptionId = provider(preferences)
            .getPurchasePackages(placementId = null).getOrThrow().first().id
        provider(preferences).purchase(subscriptionId).getOrThrow()

        // A fresh provider backed by the same preferences still sees the unlock.
        assertTrue(provider(preferences).hasAccess(Constants.PAYWALL_PREMIUM_ACCESS))
    }
}
