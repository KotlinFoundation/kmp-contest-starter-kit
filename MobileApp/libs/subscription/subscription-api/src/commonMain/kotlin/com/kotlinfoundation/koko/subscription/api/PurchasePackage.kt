package com.kotlinfoundation.koko.subscription.api

import kotlin.jvm.JvmInline

@JvmInline
value class PurchasePackageId(
    val value: String,
)

data class PurchasePackage(
    val id: PurchasePackageId,
    val price: Price,
    val title: String,
    val description: String = "",
    val period: BillingPeriod? = null,
    val introductoryOffer: IntroductoryOffer? = null,
)

val PurchasePackage.hasFreeTrial get(): Boolean = introductoryOffer?.mode == IntroductoryOffer.Mode.FREE_TRIAL
val PurchasePackage.trialDays get(): Int? = introductoryOffer.takeIf { hasFreeTrial }?.durationDays

/**
 * An introductory pricing phase that precedes the full-price subscription period.
 *
 * @property price Price paid during the intro phase (zero for free trials).
 * @property durationDays Total intro duration in days. For [Mode.PAY_AS_YOU_GO] this is the
 *   sum of all intro periods (e.g. 3 months at intro price = ~90 days).
 * @property mode Whether the user pays nothing ([Mode.FREE_TRIAL]), pays the intro price
 *   once for the whole [durationDays] ([Mode.PAY_UP_FRONT]), or pays the intro price every
 *   billing period for the duration ([Mode.PAY_AS_YOU_GO]).
 * @property periods Number of billing periods the intro lasts. 1 for [Mode.PAY_UP_FRONT]
 *   and [Mode.FREE_TRIAL]; can be more than 1 for [Mode.PAY_AS_YOU_GO].
 */
data class IntroductoryOffer(
    val price: Price,
    val durationDays: Int,
    val mode: Mode,
    val periods: Int = 1,
    val periodUnit: PeriodUnit? = null,
) {
    enum class Mode { FREE_TRIAL, PAY_AS_YOU_GO, PAY_UP_FRONT }
}
