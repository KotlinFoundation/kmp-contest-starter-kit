package com.kotlinfoundation.koko.domain.model.credit

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** A single entry in the local credit ledger (one grant or deduction). */
@OptIn(ExperimentalTime::class)
data class CreditTransaction(
    val id: String,
    val type: Type,
    val amount: Int, // Positive for credits granted, negative for credits used
    val description: String? = null,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
) {
    enum class Type {
        PURCHASE,
        RECURRING,
        DEDUCTION,
        BONUS,
        ADMIN_ADJUSTMENT,
    }
}
