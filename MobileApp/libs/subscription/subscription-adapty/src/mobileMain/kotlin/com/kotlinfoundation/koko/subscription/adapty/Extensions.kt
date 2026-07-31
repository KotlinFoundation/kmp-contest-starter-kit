package com.kotlinfoundation.koko.subscription.adapty

import com.adapty.kmp.models.AdaptyProfile
import com.kotlinfoundation.koko.subscription.api.GrantedAccess
import com.kotlinfoundation.koko.subscription.api.SubscriptionProviderUser
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

internal const val ADAPTY_DEFAULT_PLACEMENT_ID = "default"

internal fun AdaptyProfile.asSubscriptionProviderUser(): SubscriptionProviderUser {
    val grantedAccesses =
        accessLevels.filter { it.value.isActive }.mapValues { (id, access) ->
            GrantedAccess(
                id = id,
                expirationDateMillis = access.expiresAt.asTimeInMilliseconds(),
                willRenew = access.willRenew,
                productIdentifier = access.vendorProductId,
                isLifetime = access.isLifetime,
            )
        }
    return SubscriptionProviderUser(
        grantedAccesses = grantedAccesses,
        activeSubscriptionIds = subscriptions.keys,
    )
}

@OptIn(ExperimentalTime::class)
internal fun String?.asTimeInMilliseconds(): Long? = runCatching {
    if (this == null) return null

    val cleaned =
        this
            .replace(Regex("\\.(\\d{3})\\d*"), ".$1") // keep only 3 digits of millis
            .replace(Regex("([+-]\\d{2})(\\d{2})$"), "$1:$2") // convert +0000 to +00:00

    val instant = Instant.parse(cleaned)
    instant.toEpochMilliseconds()
}.getOrNull()
