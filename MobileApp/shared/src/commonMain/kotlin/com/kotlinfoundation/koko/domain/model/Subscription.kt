package com.kotlinfoundation.koko.domain.model

/**
 * The user's active premium subscription (provider-agnostic). A `null` Subscription means the
 * user is on the free tier (see [isFree]).
 */
data class Subscription(
    val accessId: String? = null,
    val name: String? = null,
    val formattedPrice: String? = null,
    val durationType: DurationType? = null,
    val expirationDateInMillis: Long?,
    val willRenew: Boolean,
    val benefits: List<String> = emptyList(),
) {
    enum class DurationType {
        DAILY,
        WEEKLY,
        MONTHLY,
        TWO_MONTHS,
        THREE_MONTHS,
        SIX_MONTHS,
        YEARLY,
        LIFETIME,
        UNKNOWN,
    }

    // Non-expiring plan: either an explicit LIFETIME duration or a zero/absent expiry date.
    val isLifetime: Boolean get() = durationType == DurationType.LIFETIME || expirationDateInMillis == 0L
}

/** No subscription object == free tier. */
val Subscription?.isFree: Boolean get() = this == null
