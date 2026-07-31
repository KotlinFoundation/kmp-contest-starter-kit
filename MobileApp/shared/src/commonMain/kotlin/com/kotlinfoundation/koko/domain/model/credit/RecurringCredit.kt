package com.kotlinfoundation.koko.domain.model.credit

import kotlin.time.Duration

/** UI-facing snapshot of one recurring credit source (e.g. "5 weekly credits") for display: how many are [used]/[remaining] this cycle and when it renews. */
data class RecurringCredit(
    val id: String,
    val total: Int, // total credits for this cycle
    val remaining: Int,
    val renewableDuration: Duration,
    val nextRenewalTime: Long? = null, // epoch millis
) {
    val used: Int
        get() = (total - remaining).coerceAtLeast(0)

    val progress: Float
        get() = if (total > 0) used.toFloat() / total else 0f
}
