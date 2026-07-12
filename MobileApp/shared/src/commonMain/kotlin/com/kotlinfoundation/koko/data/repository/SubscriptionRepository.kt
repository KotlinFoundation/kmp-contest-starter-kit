package com.kotlinfoundation.koko.data.repository

import com.kotlinfoundation.koko.data.BackgroundExecutor
import com.kotlinfoundation.koko.data.source.preferences.UserPreferences
import com.kotlinfoundation.koko.domain.model.Subscription
import com.kotlinfoundation.koko.subscription.api.BillingPeriod
import com.kotlinfoundation.koko.subscription.api.PurchasePackage
import com.kotlinfoundation.koko.subscription.api.PurchasePackageId
import com.kotlinfoundation.koko.subscription.api.SubscriptionProvider
import com.kotlinfoundation.koko.subscription.api.SubscriptionProviderUser
import com.kotlinfoundation.koko.subscription.api.hasAccess
import com.kotlinfoundation.koko.util.ApplicationScope
import com.kotlinfoundation.koko.util.Constants
import com.kotlinfoundation.koko.util.Constants.PAYWALL_PREMIUM_ACCESS
import com.kotlinfoundation.koko.util.analytics.Analytics
import com.kotlinfoundation.koko.util.logging.AppLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

/**
 * Owns subscription/billing state, wrapping the chosen [SubscriptionProvider] (Adapty or
 * RevenueCat) and exposing it as provider-agnostic domain [Subscription]s. `null` means free.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionRepository(
    private val applicationScope: ApplicationScope,
    private val subscriptionProvider: SubscriptionProvider,
    private val userPreferences: UserPreferences,
    private val analytics: Analytics,
    private val backgroundExecutor: BackgroundExecutor = BackgroundExecutor.IO,
) {

    // Emits the current premium subscription. Emits twice on change: a fast value first, then
    // one enriched with extra details (title/price/period) which needs additional provider calls.
    val currentSubscriptionFlow: Flow<Subscription?> =
        subscriptionProvider.currentSubscriptionProviderUserFlow
            .flatMapLatest { subscriptionProviderUser ->
                flow {
                    if (subscriptionProviderUser == null) {
                        emit(null)
                    } else {
                        // Initial fast emit value without extra details
                        emit(subscriptionProviderUser.asPremiumSubscription(retrieveExtraDetails = false))
                        emit(subscriptionProviderUser.asPremiumSubscription(retrieveExtraDetails = true))
                    }
                }
            }
            .onStart {
                emit(
                    subscriptionProvider.getUser().getOrNull()
                        ?.asPremiumSubscription(retrieveExtraDetails = false),
                )
            }
            .catch { error ->
                AppLogger.e("Error occurred while getting current subscription", error)
            }
            .flowOn(backgroundExecutor.scope)
            .shareIn(applicationScope, SharingStarted.Lazily, 1)

    suspend fun login(userId: String) {
        subscriptionProvider.login(userId).onFailure {
            AppLogger.e("Error occurred while logging in for subscription provider", it)
        }
    }

    suspend fun logOut() {
        subscriptionProvider.logout().onFailure {
            AppLogger.e("Error occurred while logging out for subscription provider", it)
        }
    }

    /**
     * True when the active provider is the built-in mock (no real subscription key configured).
     * The paywall shows a "Demo" banner in this case.
     */
    val isMockProvider: Boolean get() = subscriptionProvider.isMockProvider

    /** Demo-only: cancels the simulated subscription. No-op for real providers (cancel via store). */
    suspend fun cancelMockSubscription() = subscriptionProvider.cancelMockSubscription()

    suspend fun hasPremiumAccess(): Boolean = hasAccess(PAYWALL_PREMIUM_ACCESS)

    suspend fun hasAccess(key: String): Boolean = subscriptionProvider.hasAccess(key = key)

    fun onPaywallDismissed() = applicationScope.launch {
        val nbTimesPaywallIsDismissed =
            userPreferences.getInt(UserPreferences.KEY_NB_PAYWALL_DISMISSED, 0) ?: 0
        val newNbTimesPaywallIsDismissed = nbTimesPaywallIsDismissed + 1
        userPreferences.putInt(
            UserPreferences.KEY_NB_PAYWALL_DISMISSED,
            newNbTimesPaywallIsDismissed,
        )
        analytics.logEvent(Analytics.EVENT_PAYWALL_DISMISSED)
        subscriptionProvider.setCustomAttributes(
            mapOf(Analytics.PARAM_NB_PAYWALL_DISMISSED to newNbTimesPaywallIsDismissed),
        )
    }

    suspend fun getPackageList(placementId: String? = null): Result<List<PurchasePackage>> = subscriptionProvider.getPurchasePackages(placementId = placementId)
        .map { purchasePackages ->
            purchasePackages.sortedBy { it.price.amount }
        }

    suspend fun purchase(purchasePackageId: PurchasePackageId): Result<SubscriptionProviderUser> = subscriptionProvider.purchase(purchasePackageId)

    suspend fun restorePurchase(): Result<SubscriptionProviderUser> = subscriptionProvider.restorePurchase()

    suspend fun SubscriptionProviderUser.asPremiumSubscription(retrieveExtraDetails: Boolean = true): Subscription? {
        if (retrieveExtraDetails) {
            val availablePlacements = listOfNotNull(
                Constants.PAYWALL_PLACEMENT_DEFAULT,
                Constants.PAYWALL_PLACEMENT_ONBOARDING,
                // Add more placements if needed
            )

            val grantedAccessWithDetails = subscriptionProvider
                .getGrantedAccessesWithDetails(placements = availablePlacements)
                .getOrNull()?.find { it.id in PREMIUM_ACCESS_ID_LIST }

            return grantedAccessWithDetails?.let {
                Subscription(
                    accessId = grantedAccessWithDetails.id,
                    expirationDateInMillis = grantedAccessWithDetails.expirationDateMillis,
                    willRenew = grantedAccessWithDetails.willRenew,
                    name = grantedAccessWithDetails.details?.title,
                    formattedPrice = grantedAccessWithDetails.details?.price?.localizedString,
                    durationType = grantedAccessWithDetails.details?.period?.asDomainDurationType(),
                )
            }
        } else {
            val grantedAccess = grantedAccesses.values.firstOrNull()
            return grantedAccess?.let {
                Subscription(
                    accessId = grantedAccess.id,
                    expirationDateInMillis = grantedAccess.expirationDateMillis,
                    willRenew = grantedAccess.willRenew,
                )
            }
        }
    }

    companion object {
        val PREMIUM_ACCESS_ID_LIST = listOf(PAYWALL_PREMIUM_ACCESS)
    }
}

/**
 * Crosses the SDK → domain boundary. Domain owns its own enum so callers can
 * be tested without pulling in the subscription-api artifact.
 */
private fun BillingPeriod.asDomainDurationType(): Subscription.DurationType = when (this) {
    BillingPeriod.DAILY -> Subscription.DurationType.DAILY
    BillingPeriod.WEEKLY -> Subscription.DurationType.WEEKLY
    BillingPeriod.MONTHLY -> Subscription.DurationType.MONTHLY
    BillingPeriod.TWO_MONTHS -> Subscription.DurationType.TWO_MONTHS
    BillingPeriod.THREE_MONTHS -> Subscription.DurationType.THREE_MONTHS
    BillingPeriod.SIX_MONTHS -> Subscription.DurationType.SIX_MONTHS
    BillingPeriod.YEARLY -> Subscription.DurationType.YEARLY
    BillingPeriod.LIFETIME -> Subscription.DurationType.LIFETIME
}
