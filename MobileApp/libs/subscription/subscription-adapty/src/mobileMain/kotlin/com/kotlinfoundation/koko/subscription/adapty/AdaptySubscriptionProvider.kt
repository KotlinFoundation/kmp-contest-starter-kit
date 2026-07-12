package com.kotlinfoundation.koko.subscription.adapty

import com.adapty.kmp.Adapty
import com.adapty.kmp.OnProfileUpdatedListener
import com.adapty.kmp.models.AdaptyConfig
import com.adapty.kmp.models.AdaptyLogLevel
import com.adapty.kmp.models.AdaptyPaywallFetchPolicy
import com.adapty.kmp.models.AdaptyPaywallProduct
import com.adapty.kmp.models.AdaptyPeriodUnit
import com.adapty.kmp.models.AdaptyPrice
import com.adapty.kmp.models.AdaptyProfileParameters
import com.adapty.kmp.models.AdaptyPurchaseResult
import com.adapty.kmp.models.AdaptyResult
import com.adapty.kmp.models.AdaptySubscriptionOfferPaymentMode
import com.adapty.kmp.models.AdaptySubscriptionOfferPhase
import com.adapty.kmp.models.AdaptySubscriptionPeriod
import com.adapty.kmp.models.exceptionOrNull
import com.adapty.kmp.models.fold
import com.adapty.kmp.models.getOrNull
import com.kotlinfoundation.koko.subscription.api.BillingPeriod
import com.kotlinfoundation.koko.subscription.api.GrantedAccess
import com.kotlinfoundation.koko.subscription.api.IntroductoryOffer
import com.kotlinfoundation.koko.subscription.api.Price
import com.kotlinfoundation.koko.subscription.api.PurchasePackage
import com.kotlinfoundation.koko.subscription.api.PurchasePackageId
import com.kotlinfoundation.koko.subscription.api.SubscriptionProvider
import com.kotlinfoundation.koko.subscription.api.SubscriptionProviderUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.time.Duration.Companion.minutes

internal class AdaptySubscriptionProvider : SubscriptionProvider {
    private val paywallProductsCache = mutableMapOf<PurchasePackageId, AdaptyPaywallProduct>()

    override val currentSubscriptionProviderUserFlow: Flow<SubscriptionProviderUser?> =
        callbackFlow {
            val listener =
                OnProfileUpdatedListener { adaptyProfile ->
                    trySend(adaptyProfile.asSubscriptionProviderUser())
                }
            Adapty.setOnProfileUpdatedListener(listener)
            awaitClose {
                Adapty.setOnProfileUpdatedListener(null)
            }
        }

    override suspend fun initialize(apiKey: String): Result<Unit> {
        val adaptyResult =
            Adapty.activate(
                configuration =
                AdaptyConfig
                    .Builder(apiKey = apiKey)
                    .withActivateUI(true)
                    .build(),
            )
        return adaptyResult.asResult(onSuccess = {})
    }

    override suspend fun setLogEnabled(enabled: Boolean) {
        val adaptyLogLevel =
            when (enabled) {
                true -> AdaptyLogLevel.VERBOSE
                false -> AdaptyLogLevel.ERROR
            }
        Adapty.setLogLevel(adaptyLogLevel)
    }

    override suspend fun login(userId: String): Result<Unit> = Adapty.identify(userId).asResult {}

    override suspend fun logout(): Result<Unit> = Adapty.logout().asResult {}

    override suspend fun setCustomAttributes(attributes: Map<String, Any?>) {
        val builder = AdaptyProfileParameters.Builder()
        attributes.forEach { (key, value) ->
            when (value) {
                null -> builder.withRemovedCustomAttribute(key)
                is String -> builder.withCustomAttribute(key, value)
                is Double -> builder.withCustomAttribute(key, value)
                else -> builder.withCustomAttribute(key, value.toString())
            }
        }
        Adapty.updateProfile(builder.build())
    }

    override suspend fun getUser(): Result<SubscriptionProviderUser> = Adapty.getProfile().asResult { it.asSubscriptionProviderUser() }

    override suspend fun purchase(purchasePackageId: PurchasePackageId): Result<SubscriptionProviderUser> {
        val packageToBuy = paywallProductsCache[purchasePackageId]

        if (packageToBuy ==
            null
        ) {
            return Result.failure(
                Exception("Package is not found in Adapty paywall cache. Make sure, you called getPurchasePackages first"),
            )
        }
        val purchaseResult = Adapty.makePurchase(packageToBuy)

        return when (purchaseResult) {
            is AdaptyResult.Error -> Result.failure(purchaseResult.error)

            is AdaptyResult.Success<AdaptyPurchaseResult> -> {
                when (val successfulPurchaseResult = purchaseResult.value) {
                    is AdaptyPurchaseResult.Success -> {
                        Result.success(successfulPurchaseResult.profile.asSubscriptionProviderUser())
                    }

                    AdaptyPurchaseResult.Pending -> {
                        Result.failure(Exception("Purchase is pending"))
                    }

                    AdaptyPurchaseResult.UserCanceled -> {
                        Result.failure(Exception("Purchase was canceled"))
                    }
                }
            }
        }
    }

    override suspend fun restorePurchase(): Result<SubscriptionProviderUser> = Adapty.restorePurchases().fold(
        onSuccess = { adaptyProfile ->
            val isActiveAccessLevelsEmpty =
                adaptyProfile.accessLevels.filter { it.value.isActive }.isEmpty()
            if (isActiveAccessLevelsEmpty) {
                Result.failure(Exception("Restore failed. No active subscription found"))
            } else {
                Result.success(adaptyProfile.asSubscriptionProviderUser())
            }
        },
        onError = { error -> Result.failure(error) },
    )

    override suspend fun getPurchasePackages(placementId: String?): Result<List<PurchasePackage>> {
        val currentPlacementId = placementId ?: ADAPTY_DEFAULT_PLACEMENT_ID
        val paywallResult =
            Adapty.getPaywall(
                placementId = currentPlacementId,
                fetchPolicy = AdaptyPaywallFetchPolicy.ReturnCacheDataIfNotExpiredElseLoad(5.minutes.inWholeMilliseconds),
            )

        val paywall = paywallResult.getOrNull()

        if (paywall == null) {
            return Result.failure(
                paywallResult.exceptionOrNull()
                    ?: Exception("Paywall is not found for placementId: $currentPlacementId"),
            )
        }

        val paywallProductsResult = Adapty.getPaywallProducts(paywall)
        val paywallProducts = paywallProductsResult.getOrNull()

        if (paywallProducts == null) {
            return Result.failure(
                paywallProductsResult.exceptionOrNull()
                    ?: Exception("Paywall products are not found for paywall: ${paywall.name}"),
            )
        }

        paywallProducts.forEach { paywallProduct ->
            paywallProductsCache[PurchasePackageId(paywallProduct.vendorProductId)] = paywallProduct
        }

        val purchasePackages =
            paywallProducts.map { paywallProduct ->
                paywallProduct.asPurchasePackage()
            }

        return Result.success(purchasePackages)
    }

    override suspend fun getGrantedAccessesWithDetails(placements: List<String>): Result<List<GrantedAccess>> {
        val adaptyProfile = Adapty.getProfile().getOrNull()
        val activeAccessLevels =
            adaptyProfile?.accessLevels?.filter { it.value.isActive }?.values ?: emptyList()

        val availablePaywallPlacements = placements.takeIf { it.isNotEmpty() } ?: listOf(ADAPTY_DEFAULT_PLACEMENT_ID)

        val grantedAccessWithDetails =
            activeAccessLevels.map { accessLevel ->

                val allPaywalls =
                    availablePaywallPlacements.mapNotNull {
                        Adapty
                            .getPaywall(
                                placementId = it,
                                fetchPolicy = AdaptyPaywallFetchPolicy.ReturnCacheDataIfNotExpiredElseLoad(5.minutes.inWholeMilliseconds),
                            ).getOrNull()
                    }
                val allPaywallProducts =
                    allPaywalls.flatMap {
                        Adapty.getPaywallProducts(it).getOrNull() ?: emptyList()
                    }

                val paywallProduct =
                    allPaywallProducts.first {
                        val basePlanIdOfProduct = it.subscription?.basePlanId
                        it.vendorProductId == accessLevel.vendorProductId ||
                            (
                                basePlanIdOfProduct != null &&
                                    accessLevel.vendorProductId.contains(
                                        basePlanIdOfProduct,
                                    )
                                )
                    }

                GrantedAccess(
                    id = accessLevel.id,
                    productIdentifier = accessLevel.vendorProductId,
                    expirationDateMillis = accessLevel.expiresAt.asTimeInMilliseconds(),
                    willRenew = accessLevel.willRenew,
                    isLifetime = accessLevel.isLifetime,
                    details =
                    GrantedAccess.Details(
                        title = paywallProduct.asPurchasePackage().title.substringBefore("("),
                        price = paywallProduct.price.asGrantedAccessPrice(),
                        period = paywallProduct.subscription?.period?.asBillingPeriod()
                            ?: BillingPeriod.LIFETIME.takeIf { accessLevel.isLifetime },
                    ),
                )
            }

        return Result.success(grantedAccessWithDetails)
    }

    private fun AdaptyPaywallProduct.asPurchasePackage(): PurchasePackage {
        // Pick the most relevant intro phase. Prefer a free trial if present, else fall
        // back to a paid intro (pay-as-you-go or pay-up-front).
        val phases = subscription?.offer?.phases.orEmpty()
        val phase = phases.firstOrNull { it.paymentMode == AdaptySubscriptionOfferPaymentMode.FREE_TRIAL }
            ?: phases.firstOrNull { it.paymentMode == AdaptySubscriptionOfferPaymentMode.PAY_AS_YOU_GO }
            ?: phases.firstOrNull { it.paymentMode == AdaptySubscriptionOfferPaymentMode.PAY_UP_FRONT }
        val intro = phase?.asIntroductoryOffer()
        return PurchasePackage(
            id = PurchasePackageId(this.vendorProductId),
            title = this.localizedTitle,
            description = localizedDescription,
            price = this.price.asGrantedAccessPrice(),
            period = subscription?.period?.asBillingPeriod(),
            introductoryOffer = intro,
        )
    }

    private fun AdaptySubscriptionOfferPhase.asIntroductoryOffer(): IntroductoryOffer? {
        val unitDays = when (subscriptionPeriod.unit) {
            AdaptyPeriodUnit.DAY -> 1
            AdaptyPeriodUnit.WEEK -> 7
            AdaptyPeriodUnit.MONTH -> 30
            AdaptyPeriodUnit.YEAR -> 365
            AdaptyPeriodUnit.UNKNOWN -> return null
        }
        val mode = when (paymentMode) {
            AdaptySubscriptionOfferPaymentMode.FREE_TRIAL -> IntroductoryOffer.Mode.FREE_TRIAL
            AdaptySubscriptionOfferPaymentMode.PAY_AS_YOU_GO -> IntroductoryOffer.Mode.PAY_AS_YOU_GO
            AdaptySubscriptionOfferPaymentMode.PAY_UP_FRONT -> IntroductoryOffer.Mode.PAY_UP_FRONT
            else -> return null
        }
        val periodDays = unitDays * subscriptionPeriod.numberOfUnits
        return IntroductoryOffer(
            price = price.asGrantedAccessPrice(),
            durationDays = periodDays * numberOfPeriods,
            mode = mode,
            periods = numberOfPeriods,
            periodUnit = when (subscriptionPeriod.unit) {
                AdaptyPeriodUnit.DAY -> com.kotlinfoundation.koko.subscription.api.PeriodUnit.DAY
                AdaptyPeriodUnit.WEEK -> com.kotlinfoundation.koko.subscription.api.PeriodUnit.WEEK
                AdaptyPeriodUnit.MONTH -> com.kotlinfoundation.koko.subscription.api.PeriodUnit.MONTH
                AdaptyPeriodUnit.YEAR -> com.kotlinfoundation.koko.subscription.api.PeriodUnit.YEAR
                AdaptyPeriodUnit.UNKNOWN -> null
            },
        )
    }

    private fun AdaptySubscriptionPeriod.asBillingPeriod(): BillingPeriod? = when (unit) {
        AdaptyPeriodUnit.DAY -> if (numberOfUnits == 1) BillingPeriod.DAILY else null

        AdaptyPeriodUnit.WEEK -> BillingPeriod.WEEKLY

        AdaptyPeriodUnit.MONTH -> when (numberOfUnits) {
            1 -> BillingPeriod.MONTHLY
            2 -> BillingPeriod.TWO_MONTHS
            3 -> BillingPeriod.THREE_MONTHS
            6 -> BillingPeriod.SIX_MONTHS
            12 -> BillingPeriod.YEARLY
            else -> null
        }

        AdaptyPeriodUnit.YEAR -> BillingPeriod.YEARLY

        AdaptyPeriodUnit.UNKNOWN -> null
    }

    private fun AdaptyPrice.asGrantedAccessPrice(): Price = Price(
        amount = amount.toFloat(),
        currencyCodeOrSymbol = currencyCode ?: currencySymbol,
        localizedString = localizedString,
    )

    private inline fun <T, R> AdaptyResult<T>.asResult(onSuccess: (T) -> R): Result<R> = this.fold(
        onSuccess = { resultData -> Result.success(onSuccess(resultData)) },
        onError = { error -> Result.failure(error) },
    )
}
