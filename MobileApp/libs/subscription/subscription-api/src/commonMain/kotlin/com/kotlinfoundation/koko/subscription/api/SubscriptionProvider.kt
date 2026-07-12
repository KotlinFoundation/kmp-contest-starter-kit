package com.kotlinfoundation.koko.subscription.api

import kotlinx.coroutines.flow.Flow

interface SubscriptionProvider {
    companion object {
        fun get(factory: SubscriptionProviderFactory): SubscriptionProvider = factory.createProvider()
    }

    val currentSubscriptionProviderUserFlow: Flow<SubscriptionProviderUser?>

    suspend fun initialize(apiKey: String): Result<Unit>

    suspend fun setLogEnabled(enabled: Boolean)

    suspend fun login(userId: String): Result<Unit>

    suspend fun logout(): Result<Unit>

    suspend fun setCustomAttributes(attributes: Map<String, Any?>)

    suspend fun getUser(): Result<SubscriptionProviderUser>

    suspend fun purchase(purchasePackageId: PurchasePackageId): Result<SubscriptionProviderUser>

    suspend fun restorePurchase(): Result<SubscriptionProviderUser>

    suspend fun getPurchasePackages(placementId: String? = null): Result<List<PurchasePackage>>

    suspend fun getGrantedAccessesWithDetails(placements: List<String> = emptyList()): Result<List<GrantedAccess>>
}

suspend fun SubscriptionProvider.hasAccess(key: String): Boolean = getUser().getOrNull()?.grantedAccesses[key] != null
