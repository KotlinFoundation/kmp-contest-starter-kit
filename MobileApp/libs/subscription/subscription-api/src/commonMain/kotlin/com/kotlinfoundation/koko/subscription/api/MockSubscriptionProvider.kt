package com.kotlinfoundation.koko.subscription.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A fake in-app-purchase backend used when no real subscription key is configured. It returns demo
 * paywall packages and a "purchase" simulates success — unlocking Premium (subscriptions) or letting
 * the paywall grant credits (credit packs). The paywall shows a "Demo" banner while this is active
 * ([isMockProvider] is `true`).
 *
 * Dependency-free: persistence + the app's ids are injected, so this stays in the pure contract
 * module. [readPremiumPurchased] / [writePremiumPurchased] let the app back the unlock with real
 * storage; the app's DI wires them to `UserPreferences`.
 *
 * Client-only fake: no receipt, no server verification. Never used once a real key is set.
 */
class MockSubscriptionProvider(
    private val readPremiumPurchased: suspend () -> Boolean,
    private val writePremiumPurchased: suspend (Boolean) -> Unit,
    private val premiumAccessId: String = DEFAULT_PREMIUM_ACCESS_ID,
    private val creditPackPrefix: String = DEFAULT_CREDIT_PACK_PREFIX,
    private val creditPackPlacementId: String = DEFAULT_CREDIT_PACK_PLACEMENT_ID,
    private val currentTimeMillis: () -> Long = { 0L },
) : SubscriptionProvider {

    private val userFlow = MutableStateFlow<SubscriptionProviderUser?>(null)

    override val isMockProvider: Boolean get() = true

    override val currentSubscriptionProviderUserFlow: Flow<SubscriptionProviderUser?> = userFlow

    override suspend fun initialize(apiKey: String): Result<Unit> {
        userFlow.value = buildUser(premium = readPremiumPurchased())
        return Result.success(Unit)
    }

    override suspend fun setLogEnabled(enabled: Boolean) = Unit

    override suspend fun login(userId: String): Result<Unit> = Result.success(Unit)

    override suspend fun logout(): Result<Unit> = Result.success(Unit)

    override suspend fun setCustomAttributes(attributes: Map<String, Any?>) = Unit

    override suspend fun getUser(): Result<SubscriptionProviderUser> = Result.success(buildUser(premium = readPremiumPurchased()))

    override suspend fun purchase(purchasePackageId: PurchasePackageId): Result<SubscriptionProviderUser> {
        if (purchasePackageId.value.startsWith(creditPackPrefix)) {
            // Credit packs don't grant Premium — the paywall reads the product id and adds credits.
            return Result.success(
                SubscriptionProviderUser(
                    grantedAccesses = premiumMap(readPremiumPurchased()),
                    activeSubscriptionIds = setOf(purchasePackageId.value),
                ),
            )
        }
        writePremiumPurchased(true)
        return Result.success(buildUser(premium = true).also { userFlow.value = it })
    }

    override suspend fun restorePurchase(): Result<SubscriptionProviderUser> = Result.success(buildUser(premium = readPremiumPurchased()))

    override suspend fun cancelMockSubscription() {
        writePremiumPurchased(false)
        userFlow.value = buildUser(premium = false)
    }

    override suspend fun getPurchasePackages(placementId: String?): Result<List<PurchasePackage>> {
        val packages =
            if (placementId == creditPackPlacementId) creditPackPackages() else subscriptionPackages()
        return Result.success(packages)
    }

    override suspend fun getGrantedAccessesWithDetails(placements: List<String>): Result<List<GrantedAccess>> = Result.success(if (readPremiumPurchased()) listOf(premiumGrantedAccess()) else emptyList())

    // ── helpers ─────────────────────────────────────────────────────────────────

    private fun buildUser(premium: Boolean) = SubscriptionProviderUser(
        grantedAccesses = premiumMap(premium),
        activeSubscriptionIds = if (premium) setOf(MOCK_SUBSCRIPTION_ID) else emptySet(),
    )

    private fun premiumMap(premium: Boolean): Map<String, GrantedAccess> = if (premium) mapOf(premiumAccessId to premiumGrantedAccess()) else emptyMap()

    private fun premiumGrantedAccess() = GrantedAccess(
        id = premiumAccessId,
        // A month out so the Subscriptions screen shows the renewal/manage text (with the cancel link).
        expirationDateMillis = currentTimeMillis() + THIRTY_DAYS_MILLIS,
        willRenew = true,
        productIdentifier = MOCK_SUBSCRIPTION_ID,
        details =
        GrantedAccess.Details(
            title = "Premium (Demo)",
            price = Price(amount = 9.99f, currencyCodeOrSymbol = "$", localizedString = "$9.99"),
            period = BillingPeriod.MONTHLY,
        ),
    )

    private fun subscriptionPackages(): List<PurchasePackage> = listOf(
        PurchasePackage(
            id = PurchasePackageId(MOCK_SUBSCRIPTION_ID),
            price = Price(9.99f, "$", "$9.99"),
            title = "Monthly",
            period = BillingPeriod.MONTHLY,
            introductoryOffer =
            IntroductoryOffer(
                price = Price(0f, "$", "$0.00"),
                durationDays = 3,
                mode = IntroductoryOffer.Mode.FREE_TRIAL,
            ),
        ),
        PurchasePackage(
            id = PurchasePackageId("mock_premium_annual"),
            price = Price(59.99f, "$", "$59.99"),
            title = "Annual",
            period = BillingPeriod.YEARLY,
        ),
    )

    private fun creditPackPackages(): List<PurchasePackage> = listOf(10 to 4.99f, 30 to 9.99f, 80 to 19.99f).map { (credits, price) ->
        PurchasePackage(
            id = PurchasePackageId("$creditPackPrefix$credits"),
            price = Price(price, "$", "$$price"),
            title = "$credits credits",
        )
    }

    companion object {
        const val DEFAULT_PREMIUM_ACCESS_ID = "Premium"
        const val DEFAULT_CREDIT_PACK_PREFIX = "credit_pack_"
        const val DEFAULT_CREDIT_PACK_PLACEMENT_ID = "credits_pack"

        /** Suggested `UserPreferences` key for persisting the simulated Premium unlock. */
        const val KEY_MOCK_PREMIUM_PURCHASED = "KEY_MOCK_PREMIUM_PURCHASED"

        /** The value the build substitutes for an unset subscription key (see shared/build.gradle.kts). */
        const val PLACEHOLDER_KEY = "testValue"

        private const val MOCK_SUBSCRIPTION_ID = "mock_premium_monthly"
        private const val THIRTY_DAYS_MILLIS = 30L * 24 * 60 * 60 * 1000
    }
}
