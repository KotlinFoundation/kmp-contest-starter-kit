package com.kotlinfoundation.koko.domain.model.credit

import kotlin.time.Duration

/**
 * Example usage of [creditSystemConfig]:
 *
 * ```
 * val appCreditSystemConfig = creditSystemConfig {
 *
 *     // -----------------------------------------
 *     // One-time bonuses
 *     // -----------------------------------------
 *
 *     // Give 1 credit to new users (only once)
 *     oneTimeBonus(
 *         id = "welcome_bonus",
 *         amount = 1
 *     )
 *
 *
 *     // -----------------------------------------
 *     // Recurring credits
 *     // -----------------------------------------
 *
 *     // Give 2 free credits weekly for non-premium users
 *     recurringWeekly(
 *         id = "free_plan_weekly",
 *         amount = 2,
 *         condition = {
 *             !subscriptionRepository.hasPremiumAccess()
 *         }
 *     )
 *
 *     // Premium users → 10 weekly credits
 *     recurringWeekly(
 *         id = "premium_plan_weekly",
 *         amount = 10,
 *         condition = {
 *              val sub = subscriptionRepository.getCurrentPremiumSubscription()
 *              sub != null && sub.durationType == Subscription.DurationType.WEEKLY
 *         }
 *     )
 *
 *     // Premium users → 1 credit daily
 *     recurringDaily(
 *         id = "premium_plan_daily",
 *         amount = 1,
 *         condition = {
 *             subscriptionRepository.hasPremiumAccess() //Check duration as well if needed
 *         }
 *     )
 *
 *
 *     // -----------------------------------------
 *     // Custom interval (optional feature)
 *     // Runs every 'every' duration.
 *     // -----------------------------------------
 *
 *     recurringCustomDuration(
 *         id = "special_3_day_bonus",
 *         amount = 2,
 *         every = 3.days,
 *         condition = {
 *             subscriptionRepository.hasPremiumAccess()
 *         }
 *     )
 * }
 * ```
 *
 * Notes:
 * - One-time bonuses are granted only once per user.
 * - Recurring rules store last execution timestamps locally and auto-trigger
 *   when `refreshAndGetBalance()`  is called.
 * - The `condition` lambda determines whether a rule applies (e.g. premium vs free users).
 * - Credits are stored locally; consider server sync if you detect anomalies or need
 *   cross-device consistency.
 */

fun creditSystemConfig(block: CreditSystemBuilder.() -> Unit): CreditSystemConfig = CreditSystemBuilder().apply(block).build()

data class CreditSourceConfig(
    val id: String, // unique key, e.g. "free_weekly", "pro_subscription", "welcome_bonus"
    val amount: Int, // number of credits granted
    val renewableType: CreditRenewableType,
    val condition: (suspend () -> Boolean) = { true }, // This is useful for one time packs, like referral_bonus, or premium ones
) {
    init {
        require(amount >= 0) { "amount must be >= 0" }
        // Allow zero duration only for one-time bonuses
        if (renewableType !is CreditRenewableType.None) {
            require(renewableType.duration.isPositive()) { "Renewable Duration must be positive" }
        }
    }
}

/**
 * Check [creditSystemConfig] dsl function
 */
data class CreditSystemConfig(val sources: List<CreditSourceConfig>)

class CreditSystemBuilder {
    private val sources = mutableListOf<CreditSourceConfig>()

    // --- One-time Bonus ---
    fun oneTimeBonus(
        id: String,
        amount: Int,
        condition: (suspend () -> Boolean) = { true },
    ) {
        sources += CreditSourceConfig(
            id = id,
            amount = amount,
            renewableType = CreditRenewableType.None,
            condition = condition,
        )
    }

    // --- Weekly / Daily / Monthly / Yearly ---
    fun recurringDaily(id: String, amount: Int, condition: (suspend () -> Boolean) = { true }) {
        sources += CreditSourceConfig(
            id = id,
            amount = amount,
            renewableType = CreditRenewableType.Daily,
            condition = condition,
        )
    }

    fun recurringWeekly(id: String, amount: Int, condition: (suspend () -> Boolean) = { true }) {
        sources += CreditSourceConfig(
            id = id,
            amount = amount,
            renewableType = CreditRenewableType.Weekly,
            condition = condition,
        )
    }

    fun recurringMonthly(id: String, amount: Int, condition: (suspend () -> Boolean) = { true }) {
        sources += CreditSourceConfig(
            id = id,
            amount = amount,
            renewableType = CreditRenewableType.Monthly,
            condition = condition,
        )
    }

    fun recurringYearly(id: String, amount: Int, condition: (suspend () -> Boolean) = { true }) {
        sources += CreditSourceConfig(
            id = id,
            amount = amount,
            renewableType = CreditRenewableType.Yearly,
            condition = condition,
        )
    }

    // --- Custom interval ---
    fun recurringCustomDuration(
        id: String,
        amount: Int,
        every: Duration,
        condition: (suspend () -> Boolean) = { true },
    ) {
        sources += CreditSourceConfig(
            id = id,
            amount = amount,
            renewableType = CreditRenewableType.Custom(duration = every),
            condition = condition,
        )
    }

    fun build(): CreditSystemConfig {
        val duplicateIds = sources.groupingBy { it.id }.eachCount().filter { it.value > 1 }.keys
        require(duplicateIds.isEmpty()) { "Duplicate credit source ids found: ${duplicateIds.joinToString()}" }
        return CreditSystemConfig(sources)
    }
}
