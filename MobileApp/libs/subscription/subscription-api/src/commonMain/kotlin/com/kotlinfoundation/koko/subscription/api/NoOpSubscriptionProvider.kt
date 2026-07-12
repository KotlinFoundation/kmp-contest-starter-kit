package com.kotlinfoundation.koko.subscription.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

object NoOpSubscriptionProvider : SubscriptionProvider {
    private var isLogEnabled = true

    override val currentSubscriptionProviderUserFlow: Flow<SubscriptionProviderUser?> = flowOf(null)

    override suspend fun initialize(apiKey: String): Result<Unit> {
        logMessageIfEnabled { "initialization is called" }
        return Result.success(Unit)
    }

    override suspend fun setLogEnabled(enabled: Boolean) {
        this.isLogEnabled = enabled
    }

    override suspend fun login(userId: String): Result<Unit> {
        logMessageIfEnabled { "login is called" }
        return Result.success(Unit)
    }

    override suspend fun logout(): Result<Unit> {
        logMessageIfEnabled { "logout is called" }
        return Result.success(Unit)
    }

    override suspend fun setCustomAttributes(attributes: Map<String, Any?>) {
        logMessageIfEnabled { "setCustomAttributes is called" }
    }

    override suspend fun getUser(): Result<SubscriptionProviderUser> {
        logMessageIfEnabled { "getUser is called" }
        return Result.failure(Exception("NoOpSubscriptionProvider user doesn't exist"))
    }

    override suspend fun restorePurchase(): Result<SubscriptionProviderUser> {
        logMessageIfEnabled { "restorePurchase is called" }
        return Result.failure(Exception("NoOpSubscriptionProvider user doesn't exist"))
    }

    override suspend fun purchase(purchasePackageId: PurchasePackageId): Result<SubscriptionProviderUser> {
        logMessageIfEnabled { "restorePurchase is called" }
        return Result.failure(Exception("NoOpSubscriptionProvider user doesn't exist"))
    }

    override suspend fun getPurchasePackages(placementId: String?): Result<List<PurchasePackage>> {
        logMessageIfEnabled { "getPurchasePackages is called for placementId: $placementId" }
        return Result.success(emptyList())
    }

    override suspend fun getGrantedAccessesWithDetails(placements: List<String>): Result<List<GrantedAccess>> {
        logMessageIfEnabled { "getGrantedAccessesWithDetails is called " }
        return Result.success(emptyList())
    }

    private fun logMessageIfEnabled(message: () -> String) {
        if (!isLogEnabled) return
        println("NoOpSubscriptionProvider: ${message()}")
    }
}
