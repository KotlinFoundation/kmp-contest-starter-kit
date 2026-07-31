package com.kotlinfoundation.koko.subscription.revenuecat

import com.kotlinfoundation.koko.subscription.api.BillingPeriod
import com.kotlinfoundation.koko.subscription.api.GrantedAccess
import com.kotlinfoundation.koko.subscription.api.IntroductoryOffer
import com.kotlinfoundation.koko.subscription.api.Price
import com.kotlinfoundation.koko.subscription.api.PurchasePackage
import com.kotlinfoundation.koko.subscription.api.PurchasePackageId
import com.kotlinfoundation.koko.subscription.api.SubscriptionProvider
import com.kotlinfoundation.koko.subscription.api.SubscriptionProviderUser
import com.kotlinfoundation.koko.subscription.api.runCatchingSuspend
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.PurchasesDelegate
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitLogIn
import com.revenuecat.purchases.kmp.ktx.awaitLogOut
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.OfferPaymentMode
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PackageType
import com.revenuecat.purchases.kmp.models.Period
import com.revenuecat.purchases.kmp.models.PeriodUnit
import com.revenuecat.purchases.kmp.models.PricingPhase
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.models.StoreTransaction
import com.revenuecat.purchases.kmp.models.freePhase
import com.revenuecat.purchases.kmp.models.introPhase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal class RevenueCatSubscriptionProvider : SubscriptionProvider {
    private val packagesCache = mutableMapOf<PurchasePackageId, Package>()

    override val currentSubscriptionProviderUserFlow: Flow<SubscriptionProviderUser?> =
        callbackFlow {
            val delegate =
                object : PurchasesDelegate {
                    override fun onCustomerInfoUpdated(customerInfo: CustomerInfo) {
                        trySend(customerInfo.asSubscriptionProviderUser())
                    }

                    override fun onPurchasePromoProduct(
                        product: StoreProduct,
                        startPurchase: (
                            onError: (error: PurchasesError, userCancelled: Boolean) -> Unit,
                            onSuccess: (storeTransaction: StoreTransaction, customerInfo: CustomerInfo) -> Unit,
                        ) -> Unit,
                    ) {
                    }
                }
            Purchases.sharedInstance.delegate = delegate
            awaitClose {
                Purchases.sharedInstance.delegate = null
            }
        }

    override suspend fun initialize(apiKey: String): Result<Unit> {
        Purchases.configure(PurchasesConfiguration(apiKey = apiKey))
        return Result.success(Unit)
    }

    override suspend fun setLogEnabled(enabled: Boolean) {
        val revenueCatLogLevel =
            when (enabled) {
                true -> LogLevel.VERBOSE
                false -> LogLevel.ERROR
            }
        Purchases.logLevel = revenueCatLogLevel
    }

    override suspend fun login(userId: String): Result<Unit> = runCatchingSuspend {
        Purchases.sharedInstance.awaitLogIn(userId)
        Unit
    }

    override suspend fun logout(): Result<Unit> = runCatchingSuspend {
        Purchases.sharedInstance.awaitLogOut()
        Unit
    }

    override suspend fun getUser(): Result<SubscriptionProviderUser> = runCatchingSuspend {
        Purchases.sharedInstance.awaitCustomerInfo().asSubscriptionProviderUser()
    }

    override suspend fun setCustomAttributes(attributes: Map<String, Any?>) {
        val mappedAttributes =
            attributes.mapValues { (key, value) ->
                when (value) {
                    null -> null
                    is String -> value
                    else -> value.toString()
                }
            }
        Purchases.sharedInstance.setAttributes(mappedAttributes)
    }

    override suspend fun restorePurchase(): Result<SubscriptionProviderUser> = runCatchingSuspend {
        val customerInfo = Purchases.sharedInstance.awaitRestore()
        val hasSuccessfulRestore = customerInfo.entitlements.all.any { it.value.isActive }
        if (!hasSuccessfulRestore) throw Exception("Restore failed. No active subscription found")
        customerInfo.asSubscriptionProviderUser()
    }

    override suspend fun purchase(purchasePackageId: PurchasePackageId): Result<SubscriptionProviderUser> = runCatchingSuspend {
        val packageToBuy =
            packagesCache[purchasePackageId]
                ?: throw Exception("Package is not found in RevenueCat cache. Make sure, you called getPurchasePackages first")
        val successfulPurchaseResult = Purchases.sharedInstance.awaitPurchase(packageToBuy)
        successfulPurchaseResult.customerInfo.asSubscriptionProviderUser()
    }

    override suspend fun getPurchasePackages(placementId: String?): Result<List<PurchasePackage>> = runCatchingSuspend {
        val offerings = Purchases.sharedInstance.awaitOfferings()
        val currentOffering =
            if (placementId == null) {
                offerings.current
            } else {
                offerings.get(placementId) ?: offerings.current
            }

        val packages =
            currentOffering?.availablePackages?.takeUnless { it.isEmpty() } ?: emptyList()

        packages.forEach { rcPackage ->
            packagesCache[PurchasePackageId(rcPackage.identifier)] = rcPackage
        }
        packages.map { it.asPurchasePackage() }
    }

    override suspend fun getGrantedAccessesWithDetails(placements: List<String>): Result<List<GrantedAccess>> = runCatchingSuspend {
        val purchasesInstance = Purchases.sharedInstance

        val activeEntitlements =
            purchasesInstance
                .awaitCustomerInfo()
                .entitlements.active.values

        val grantedAccessWithDetails =
            activeEntitlements.map { entitlement ->
                val allOfferings = purchasesInstance.awaitOfferings().all.map { it.value }
                val allPackages = allOfferings.flatMap { it.availablePackages }

                val entitlementPackage =
                    allPackages.first {
                        val productPlanIdentifier = entitlement.productPlanIdentifier
                        it.storeProduct.id == entitlement.productIdentifier ||
                            (
                                productPlanIdentifier != null &&
                                    it.storeProduct.id.contains(
                                        productPlanIdentifier,
                                    )
                                )
                    }

                GrantedAccess(
                    id = entitlement.identifier,
                    productIdentifier = entitlement.productIdentifier,
                    expirationDateMillis = entitlement.expirationDateMillis,
                    willRenew = entitlement.willRenew,
                    isLifetime = entitlement.expirationDateMillis == null && entitlement.willRenew,
                    details =
                    GrantedAccess.Details(
                        title = entitlementPackage.asPurchasePackage().title.substringBefore("("),
                        price = entitlementPackage.storeProduct.price.asGrantedAccessPrice(),
                        period = entitlementPackage.packageType.asBillingPeriod(),
                    ),
                )
            }
        return Result.success(grantedAccessWithDetails)
    }

    private fun Package.asPurchasePackage(): PurchasePackage {
        // Prefer the free phase (most common). If none, fall back to a paid intro phase.
        val freePhase = storeProduct.defaultOption?.freePhase
        val introPhase = storeProduct.defaultOption?.introPhase
        val intro = freePhase?.asIntroductoryOffer(IntroductoryOffer.Mode.FREE_TRIAL)
            ?: introPhase?.asPaidIntroductoryOffer()
        return PurchasePackage(
            id = PurchasePackageId(this.identifier),
            title = storeProduct.title,
            description = "${storeProduct.localizedDescription}",
            price = this.storeProduct.price.asGrantedAccessPrice(),
            period = packageType.asBillingPeriod(),
            introductoryOffer = intro,
        )
    }

    private fun PricingPhase.asIntroductoryOffer(mode: IntroductoryOffer.Mode): IntroductoryOffer? {
        val periodDays = billingPeriod.asApproxDays() ?: return null
        val periods = billingCycleCount?.takeIf { it > 0 } ?: 1
        return IntroductoryOffer(
            price = price.asGrantedAccessPrice(),
            durationDays = periodDays * periods,
            mode = mode,
            periods = periods,
            periodUnit = billingPeriod.periodUnit(),
        )
    }

    private fun PricingPhase.asPaidIntroductoryOffer(): IntroductoryOffer? {
        // SINGLE_PAYMENT → user pays the intro price once for the full intro span.
        // DISCOUNTED_RECURRING_PAYMENT → user pays the intro price each period for N periods.
        val mode = when (offerPaymentMode) {
            OfferPaymentMode.SINGLE_PAYMENT -> IntroductoryOffer.Mode.PAY_UP_FRONT
            OfferPaymentMode.DISCOUNTED_RECURRING_PAYMENT -> IntroductoryOffer.Mode.PAY_AS_YOU_GO
            OfferPaymentMode.FREE_TRIAL, null -> return null
        }
        return asIntroductoryOffer(mode)
    }

    private fun Period.asApproxDays(): Int? = when (unit) {
        PeriodUnit.DAY -> value
        PeriodUnit.WEEK -> value * 7
        PeriodUnit.MONTH -> value * 30
        PeriodUnit.YEAR -> value * 365
        PeriodUnit.UNKNOWN -> null
    }

    private fun Period.periodUnit(): com.kotlinfoundation.koko.subscription.api.PeriodUnit? = when (unit) {
        PeriodUnit.DAY -> com.kotlinfoundation.koko.subscription.api.PeriodUnit.DAY
        PeriodUnit.WEEK -> com.kotlinfoundation.koko.subscription.api.PeriodUnit.WEEK
        PeriodUnit.MONTH -> com.kotlinfoundation.koko.subscription.api.PeriodUnit.MONTH
        PeriodUnit.YEAR -> com.kotlinfoundation.koko.subscription.api.PeriodUnit.YEAR
        PeriodUnit.UNKNOWN -> null
    }

    private fun PackageType.asBillingPeriod(): BillingPeriod? = when (this) {
        PackageType.WEEKLY -> BillingPeriod.WEEKLY
        PackageType.MONTHLY -> BillingPeriod.MONTHLY
        PackageType.TWO_MONTH -> BillingPeriod.TWO_MONTHS
        PackageType.THREE_MONTH -> BillingPeriod.THREE_MONTHS
        PackageType.SIX_MONTH -> BillingPeriod.SIX_MONTHS
        PackageType.ANNUAL -> BillingPeriod.YEARLY
        PackageType.LIFETIME -> BillingPeriod.LIFETIME
        PackageType.UNKNOWN, PackageType.CUSTOM -> null
    }

    private fun com.revenuecat.purchases.kmp.models.Price.asGrantedAccessPrice(): Price = Price(
        amount = this.amountMicros / 1000000f,
        currencyCodeOrSymbol = currencyCode,
        localizedString = formatted,
    )
}
