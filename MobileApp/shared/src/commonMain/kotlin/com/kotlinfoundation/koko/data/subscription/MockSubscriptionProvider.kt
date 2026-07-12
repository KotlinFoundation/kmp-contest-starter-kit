package com.kotlinfoundation.koko.data.subscription

import com.kotlinfoundation.koko.common.BuildConfig
import com.kotlinfoundation.koko.data.source.preferences.UserPreferences
import com.kotlinfoundation.koko.subscription.api.BillingPeriod
import com.kotlinfoundation.koko.subscription.api.GrantedAccess
import com.kotlinfoundation.koko.subscription.api.IntroductoryOffer
import com.kotlinfoundation.koko.subscription.api.Price
import com.kotlinfoundation.koko.subscription.api.PurchasePackage
import com.kotlinfoundation.koko.subscription.api.PurchasePackageId
import com.kotlinfoundation.koko.subscription.api.SubscriptionProvider
import com.kotlinfoundation.koko.subscription.api.SubscriptionProviderUser
import com.kotlinfoundation.koko.util.Constants
import com.kotlinfoundation.koko.util.extensions.isCreditPackProductId
import com.kotlinfoundation.koko.util.isAndroid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Placeholder value the build substitutes for an unset key (see `shared/build.gradle.kts`). */
private const val PLACEHOLDER_KEY = "testValue"

/**
 * True when the active-platform subscription SDK key is still a placeholder, i.e. no real
 * Adapty/RevenueCat account is wired yet. While true the app runs [MockSubscriptionProvider] so the
 * whole paywall → purchase → unlock flow is explorable with zero keys. Auto-off once a real key is set.
 */
internal fun isSubscriptionMockActive(): Boolean {
    val key =
        if (isAndroid) {
            BuildConfig.SUBSCRIPTION_PROVIDER_ANDROID_API_KEY
        } else {
            BuildConfig.SUBSCRIPTION_PROVIDER_IOS_API_KEY
        }
    return key.isBlank() || key == PLACEHOLDER_KEY
}

/**
 * A fake in-app-purchase backend used when no real subscription key is configured. It returns real
 * paywall packages and a "purchase" simulates success — unlocking Premium (subscriptions) or letting
 * the paywall grant credits (credit packs). The unlock is persisted so it survives restart.
 *
 * Client-only: no receipt, no server verification. The paywall shows a "Demo" banner while this is
 * active (see `PaywallUiState.isMock`). Never used once a real key is set.
 */
internal class MockSubscriptionProvider(
    private val userPreferences: UserPreferences,
) : SubscriptionProvider {

    private val userFlow = MutableStateFlow<SubscriptionProviderUser?>(null)

    override val currentSubscriptionProviderUserFlow: Flow<SubscriptionProviderUser?> = userFlow

    override suspend fun initialize(apiKey: String): Result<Unit> {
        userFlow.value = buildUser(premium = isPremiumPersisted())
        return Result.success(Unit)
    }

    override suspend fun setLogEnabled(enabled: Boolean) = Unit

    override suspend fun login(userId: String): Result<Unit> = Result.success(Unit)

    override suspend fun logout(): Result<Unit> = Result.success(Unit)

    override suspend fun setCustomAttributes(attributes: Map<String, Any?>) = Unit

    override suspend fun getUser(): Result<SubscriptionProviderUser> = Result.success(buildUser(premium = isPremiumPersisted()))

    override suspend fun purchase(purchasePackageId: PurchasePackageId): Result<SubscriptionProviderUser> {
        if (purchasePackageId.value.isCreditPackProductId()) {
            // Credit packs don't grant Premium — the paywall reads the product id and adds credits.
            return Result.success(
                SubscriptionProviderUser(
                    grantedAccesses = premiumMap(isPremiumPersisted()),
                    activeSubscriptionIds = setOf(purchasePackageId.value),
                ),
            )
        }
        setPremiumPersisted(true)
        return Result.success(buildUser(premium = true).also { userFlow.value = it })
    }

    override suspend fun restorePurchase(): Result<SubscriptionProviderUser> = Result.success(buildUser(premium = isPremiumPersisted()))

    override suspend fun getPurchasePackages(placementId: String?): Result<List<PurchasePackage>> {
        val packages =
            if (placementId == Constants.PAYWALL_PLACEMENT_CREDITS_PACK) {
                creditPackPackages()
            } else {
                subscriptionPackages()
            }
        return Result.success(packages)
    }

    override suspend fun getGrantedAccessesWithDetails(placements: List<String>): Result<List<GrantedAccess>> = Result.success(if (isPremiumPersisted()) listOf(premiumGrantedAccess()) else emptyList())

    // ── helpers ─────────────────────────────────────────────────────────────────

    private suspend fun isPremiumPersisted(): Boolean = userPreferences.getBoolean(KEY_MOCK_PREMIUM_PURCHASED, defaultValue = false)

    private suspend fun setPremiumPersisted(value: Boolean) = userPreferences.putBoolean(KEY_MOCK_PREMIUM_PURCHASED, value)

    private fun buildUser(premium: Boolean) = SubscriptionProviderUser(
        grantedAccesses = premiumMap(premium),
        activeSubscriptionIds = if (premium) setOf(MOCK_SUBSCRIPTION_ID) else emptySet(),
    )

    private fun premiumMap(premium: Boolean): Map<String, GrantedAccess> = if (premium) mapOf(Constants.PAYWALL_PREMIUM_ACCESS to premiumGrantedAccess()) else emptyMap()

    private fun premiumGrantedAccess() = GrantedAccess(
        id = Constants.PAYWALL_PREMIUM_ACCESS,
        expirationDateMillis = null,
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
            id = PurchasePackageId("${Constants.CREDIT_PACK_PRODUCT_ID_PREFIX}$credits"),
            price = Price(price, "$", "$$price"),
            title = "$credits credits",
        )
    }

    companion object {
        private const val KEY_MOCK_PREMIUM_PURCHASED = "KEY_MOCK_PREMIUM_PURCHASED"
        private const val MOCK_SUBSCRIPTION_ID = "mock_premium_monthly"
    }
}
