package com.kotlinfoundation.koko.presentation.screens.paywall

import com.kotlinfoundation.koko.designsystem.util.UiText
import com.kotlinfoundation.koko.generated.resources.Res
import com.kotlinfoundation.koko.generated.resources.paywall_badge_best_value
import com.kotlinfoundation.koko.generated.resources.paywall_badge_save_percent
import com.kotlinfoundation.koko.generated.resources.paywall_cp_credits_count
import com.kotlinfoundation.koko.generated.resources.paywall_cp_cta_buy
import com.kotlinfoundation.koko.generated.resources.paywall_cp_reassurance
import com.kotlinfoundation.koko.generated.resources.paywall_cp_unit_price
import com.kotlinfoundation.koko.generated.resources.paywall_sub_cta_continue
import com.kotlinfoundation.koko.generated.resources.paywall_sub_cta_try_for
import com.kotlinfoundation.koko.generated.resources.paywall_sub_disclosure_intro_pay_as_you_go
import com.kotlinfoundation.koko.generated.resources.paywall_sub_disclosure_intro_upfront
import com.kotlinfoundation.koko.generated.resources.paywall_sub_disclosure_template
import com.kotlinfoundation.koko.generated.resources.paywall_sub_disclosure_then
import com.kotlinfoundation.koko.generated.resources.paywall_sub_disclosure_then_standard
import com.kotlinfoundation.koko.generated.resources.paywall_sub_disclosure_trial
import com.kotlinfoundation.koko.generated.resources.paywall_sub_disclosure_trial_generic
import com.kotlinfoundation.koko.generated.resources.paywall_sub_plan_daily
import com.kotlinfoundation.koko.generated.resources.paywall_sub_plan_lifetime
import com.kotlinfoundation.koko.generated.resources.paywall_sub_plan_monthly
import com.kotlinfoundation.koko.generated.resources.paywall_sub_plan_six_months
import com.kotlinfoundation.koko.generated.resources.paywall_sub_plan_three_months
import com.kotlinfoundation.koko.generated.resources.paywall_sub_plan_two_months
import com.kotlinfoundation.koko.generated.resources.paywall_sub_plan_unknown
import com.kotlinfoundation.koko.generated.resources.paywall_sub_plan_weekly
import com.kotlinfoundation.koko.generated.resources.paywall_sub_plan_yearly
import com.kotlinfoundation.koko.generated.resources.paywall_sub_reassurance_cancel
import com.kotlinfoundation.koko.generated.resources.paywall_sub_reassurance_intro
import com.kotlinfoundation.koko.generated.resources.paywall_sub_reassurance_intro_generic
import com.kotlinfoundation.koko.generated.resources.paywall_sub_reassurance_trial
import com.kotlinfoundation.koko.generated.resources.paywall_sub_subtitle_auto_renews
import com.kotlinfoundation.koko.generated.resources.paywall_sub_subtitle_one_time
import com.kotlinfoundation.koko.generated.resources.paywall_sub_subtitle_per_week
import com.kotlinfoundation.koko.generated.resources.paywall_sub_subtitle_premium_access
import com.kotlinfoundation.koko.generated.resources.paywall_unit_day
import com.kotlinfoundation.koko.generated.resources.paywall_unit_day_count
import com.kotlinfoundation.koko.generated.resources.paywall_unit_month
import com.kotlinfoundation.koko.generated.resources.paywall_unit_month_count
import com.kotlinfoundation.koko.generated.resources.paywall_unit_period
import com.kotlinfoundation.koko.generated.resources.paywall_unit_week
import com.kotlinfoundation.koko.generated.resources.paywall_unit_week_count
import com.kotlinfoundation.koko.generated.resources.paywall_unit_year
import com.kotlinfoundation.koko.generated.resources.paywall_unit_year_count
import com.kotlinfoundation.koko.subscription.api.BillingPeriod
import com.kotlinfoundation.koko.subscription.api.IntroductoryOffer
import com.kotlinfoundation.koko.subscription.api.PeriodUnit
import com.kotlinfoundation.koko.subscription.api.Price
import com.kotlinfoundation.koko.subscription.api.PurchasePackage
import com.kotlinfoundation.koko.subscription.api.PurchasePackageId
import com.kotlinfoundation.koko.subscription.api.hasFreeTrial
import com.kotlinfoundation.koko.subscription.api.isRecurring
import com.kotlinfoundation.koko.subscription.api.trialDays
import com.kotlinfoundation.koko.util.extensions.parseCreditAmountFromProductId
import org.jetbrains.compose.resources.PluralStringResource

/**
 * Pure mapper from raw [PurchasePackage]s to the parts of [PaywallUiState] that
 * depend on packages + selection: the per-card [PaywallPackageUiState] list and
 * the footer copy (CTA, reassurance, disclosure).
 *
 */
class PaywallUiStateMapper {

    /**
     * Slice of [PaywallUiState] that the mapper owns. Whatever isn't returned
     * here (loading, errors, success state, etc.) is managed by the view model.
     */
    data class MappedPaywall(
        val packages: List<PaywallPackageUiState>,
        val ctaText: UiText,
        val aboveCtaText: UiText,
        val belowCtaText: UiText?,
    )

    /**
     * Builds the package cards + footer copy for the given selection. Re-invoke
     * whenever the package list or [selectedId] changes so the footer (CTA copy,
     * trial vs. paid intro reassurance, compliance disclosure) reflects the
     * currently-selected plan.
     */
    fun map(
        rawPackages: List<PurchasePackage>,
        selectedId: PurchasePackageId?,
        mode: PaywallMode,
    ): MappedPaywall {
        val selected = rawPackages.firstOrNull { it.id == selectedId }
        val packages = when (mode) {
            PaywallMode.CREDIT_PACK -> buildCreditPackageStates(rawPackages, selectedId)
            PaywallMode.SUBSCRIPTION -> buildSubscriptionPackageStates(rawPackages, selectedId)
        }
        return MappedPaywall(
            packages = packages,
            ctaText = ctaText(mode, selected),
            aboveCtaText = aboveCtaText(mode, selected),
            belowCtaText = belowCtaText(mode, selected),
        )
    }

    /**
     * Picks the package that should be selected by default — the same one that
     * carries the BEST VALUE / SAVE N% chip — so the user lands on the
     * recommended plan instead of the cheapest. Falls back to the first
     * package when no recommendation can be derived.
     */
    fun pickDefaultSelection(
        rawPackages: List<PurchasePackage>,
        mode: PaywallMode,
    ): PurchasePackageId? {
        if (rawPackages.isEmpty()) return null
        val recommendedId = when (mode) {
            PaywallMode.CREDIT_PACK -> bestValueCreditPackId(rawPackages)
            PaywallMode.SUBSCRIPTION -> recommendedSubscriptionId(rawPackages)
        }
        return recommendedId ?: rawPackages.firstOrNull()?.id
    }

    /**
     * The most expensive package — typically the longest-period / lifetime plan —
     * is the one we recommend. Only meaningful when there's more than one plan
     * to compare against (a single-plan paywall has no "best" choice).
     */
    private fun recommendedSubscriptionId(packages: List<PurchasePackage>): PurchasePackageId? {
        val byPrice = packages.sortedBy { it.price.amount }
        return if (byPrice.size > 1) byPrice.lastOrNull()?.id else null
    }

    /**
     * Best credit pack = lowest price-per-credit, parsed from the product id
     * suffix (e.g. `credit_pack_200`). Packs without a parseable credit count
     * are skipped so a malformed id can't win.
     */
    private fun bestValueCreditPackId(packages: List<PurchasePackage>): PurchasePackageId? = packages
        .mapNotNull { pkg ->
            val credits = pkg.id.value.parseCreditAmountFromProductId() ?: return@mapNotNull null
            if (credits <= 0 || pkg.price.amount <= 0f) return@mapNotNull null
            pkg.id to (pkg.price.amount / credits)
        }
        .minByOrNull { it.second }
        ?.first

    // ── Subscription package card formatting ────────────────────────────────

    /**
     * Builds one [PaywallPackageUiState] per subscription option, with the
     * recommended plan flagged via [PaywallPackageUiState.isRecommended] and
     * tagged with a SAVE % chip (or BEST VALUE fallback when the % can't be
     * computed).
     */
    private fun buildSubscriptionPackageStates(
        packages: List<PurchasePackage>,
        selectedId: PurchasePackageId?,
    ): List<PaywallPackageUiState> {
        if (packages.isEmpty()) return emptyList()
        val byPrice = packages.sortedBy { it.price.amount }
        val recommendedId = recommendedSubscriptionId(packages)
        // Anchor for the SAVE % calculation: cheapest recurring plan per-day rate.
        val cheapestRecurring = byPrice.firstOrNull { it.period.isRecurring }
        return byPrice.map { pkg ->
            val isRecommended = pkg.id == recommendedId
            val savingsPercent = if (isRecommended) computeSavingsPercent(pkg, cheapestRecurring) else null
            PaywallPackageUiState(
                purchasePackage = pkg,
                title = subscriptionTitle(pkg.period),
                subtitle = subscriptionSubtitle(pkg),
                priceText = priceWithPeriodSuffix(pkg.price.localizedString.orEmpty(), pkg.period),
                isSelected = pkg.id == selectedId,
                isRecommended = isRecommended,
                savingsBadge = when {
                    !isRecommended -> null
                    savingsPercent != null -> UiText.of(Res.string.paywall_badge_save_percent, savingsPercent)
                    else -> UiText.of(Res.string.paywall_badge_best_value)
                },
            )
        }
    }

    /** Card title for a subscription plan — "1 Week" / "1 Month" / "1 Year" / "Lifetime". */
    private fun subscriptionTitle(period: BillingPeriod?): UiText = UiText.of(
        when (period) {
            BillingPeriod.DAILY -> Res.string.paywall_sub_plan_daily
            BillingPeriod.WEEKLY -> Res.string.paywall_sub_plan_weekly
            BillingPeriod.MONTHLY -> Res.string.paywall_sub_plan_monthly
            BillingPeriod.TWO_MONTHS -> Res.string.paywall_sub_plan_two_months
            BillingPeriod.THREE_MONTHS -> Res.string.paywall_sub_plan_three_months
            BillingPeriod.SIX_MONTHS -> Res.string.paywall_sub_plan_six_months
            BillingPeriod.YEARLY -> Res.string.paywall_sub_plan_yearly
            BillingPeriod.LIFETIME -> Res.string.paywall_sub_plan_lifetime
            null -> Res.string.paywall_sub_plan_unknown
        },
    )

    /**
     * Per-week price ("$0.96 per week") for recurring plans — easiest way for
     * a user to compare a yearly plan against the weekly anchor. Falls back to
     * the per-period qualifier ("Auto-renews" / "One-time payment" / "Premium
     * access") when we can't compute a per-week amount.
     */
    private fun subscriptionSubtitle(pkg: PurchasePackage): UiText {
        val approxDays = pkg.period?.approximateDays
        val symbol = pkg.price.currencyCodeOrSymbol.orEmpty()
        if (approxDays != null && approxDays > 0 && pkg.price.amount > 0f) {
            val perWeek = pkg.price.amount / (approxDays / 7f)
            return UiText.of(Res.string.paywall_sub_subtitle_per_week, "$symbol${formatTwoDecimals(perWeek)}")
        }
        return UiText.of(
            when (pkg.period) {
                BillingPeriod.LIFETIME -> Res.string.paywall_sub_subtitle_one_time
                null -> Res.string.paywall_sub_subtitle_premium_access
                else -> Res.string.paywall_sub_subtitle_auto_renews
            },
        )
    }

    /**
     * Combines a localized price string ("$4.99") with the localized period unit
     * to produce the display price ("$4.99/week", "$9.99/3 months"). For
     * lifetime / unknown periods returns the bare price.
     */
    private fun priceWithPeriodSuffix(priceText: String, period: BillingPeriod?): UiText {
        val (pluralRes, count) = periodBareWord(period) ?: return UiText.of(priceText)
        return if (count == 1) {
            UiText.of("$priceText/") + UiText.of(pluralRes, 1)
        } else {
            UiText.of("$priceText/") + UiText.of(periodCountPlural(period)!!, count, count)
        }
    }

    /**
     * Maps a [BillingPeriod] to its base unit + the multiplier inside that unit
     * (e.g. THREE_MONTHS → month × 3). Returns the bare-word plural resource so
     * callers can choose between "month" (count == 1) and "months" (count > 1).
     */
    private fun periodBareWord(period: BillingPeriod?): Pair<PluralStringResource, Int>? = when (period) {
        BillingPeriod.DAILY -> Res.plurals.paywall_unit_day to 1
        BillingPeriod.WEEKLY -> Res.plurals.paywall_unit_week to 1
        BillingPeriod.MONTHLY -> Res.plurals.paywall_unit_month to 1
        BillingPeriod.TWO_MONTHS -> Res.plurals.paywall_unit_month to 2
        BillingPeriod.THREE_MONTHS -> Res.plurals.paywall_unit_month to 3
        BillingPeriod.SIX_MONTHS -> Res.plurals.paywall_unit_month to 6
        BillingPeriod.YEARLY -> Res.plurals.paywall_unit_year to 1
        BillingPeriod.LIFETIME, null -> null
    }

    /** Count-form plural resource for [period] — emits "2 months", "3 months", etc. */
    private fun periodCountPlural(period: BillingPeriod?): PluralStringResource? = when (period) {
        BillingPeriod.DAILY -> Res.plurals.paywall_unit_day_count

        BillingPeriod.WEEKLY -> Res.plurals.paywall_unit_week_count

        BillingPeriod.MONTHLY, BillingPeriod.TWO_MONTHS, BillingPeriod.THREE_MONTHS,
        BillingPeriod.SIX_MONTHS,
        -> Res.plurals.paywall_unit_month_count

        BillingPeriod.YEARLY -> Res.plurals.paywall_unit_year_count

        BillingPeriod.LIFETIME, null -> null
    }

    /**
     * Computes the "SAVE N%" headline by comparing the per-day price of [recommended]
     * against [cheapestRecurring]. Returns null when the comparison is degenerate
     * (same plan, missing period, non-positive prices, or the recommended plan
     * is somehow more expensive). The 5% floor avoids advertising a tiny saving
     * that looks like a typo.
     */
    private fun computeSavingsPercent(
        recommended: PurchasePackage,
        cheapestRecurring: PurchasePackage?,
    ): Int? {
        if (cheapestRecurring == null || cheapestRecurring.id == recommended.id) return null
        val recDays = recommended.period?.approximateDays ?: return null
        val cheapDays = cheapestRecurring.period?.approximateDays ?: return null
        if (recDays <= 0 || cheapDays <= 0) return null
        val recPerDay = recommended.price.amount / recDays
        val cheapPerDay = cheapestRecurring.price.amount / cheapDays
        if (cheapPerDay <= 0f || recPerDay >= cheapPerDay) return null
        val percent = ((cheapPerDay - recPerDay) / cheapPerDay * 100).toInt()
        return percent.takeIf { it >= 5 }
    }

    // ── Credit-pack package row formatting ──────────────────────────────────

    /**
     * Builds one [PaywallPackageUiState] per credit pack. Title shows the credit
     * count parsed from the product id; subtitle shows the per-credit unit price
     * for anchoring; the cheapest-per-credit pack gets the BEST VALUE chip.
     */
    private fun buildCreditPackageStates(
        packages: List<PurchasePackage>,
        selectedId: PurchasePackageId?,
    ): List<PaywallPackageUiState> {
        if (packages.isEmpty()) return emptyList()
        val sorted = packages.sortedBy { it.price.amount }
        val bestValueId = bestValueCreditPackId(sorted)

        return sorted.map { pkg ->
            val credits = pkg.id.value.parseCreditAmountFromProductId()
            val symbol = pkg.price.currencyCodeOrSymbol.orEmpty()
            val title: UiText = if (credits != null) {
                UiText.of(Res.string.paywall_cp_credits_count, credits)
            } else {
                UiText.of(pkg.title)
            }
            val subtitle: UiText = if (credits != null && credits > 0 && pkg.price.amount > 0f) {
                val perCredit = pkg.price.amount / credits
                UiText.of(Res.string.paywall_cp_unit_price, "$symbol${formatTwoDecimals(perCredit)}")
            } else {
                UiText.empty()
            }
            val isBest = pkg.id == bestValueId
            PaywallPackageUiState(
                purchasePackage = pkg,
                title = title,
                subtitle = subtitle,
                priceText = UiText.of(pkg.price.localizedString.orEmpty()),
                isSelected = pkg.id == selectedId,
                isRecommended = isBest,
                savingsBadge = if (isBest) UiText.of(Res.string.paywall_badge_best_value) else null,
            )
        }
    }

    // ── Footer (CTA / reassurance / disclosure) ─────────────────────────────

    /**
     * Button copy. Credit pack → "Buy credits". Subscription with a free trial →
     * "Try for $0.00" (currency mirrored from the selected price). Everything
     * else → neutral "Continue" — intro details are surfaced via the
     * reassurance + disclosure lines so the CTA stays low-friction.
     */
    private fun ctaText(mode: PaywallMode, selected: PurchasePackage?): UiText {
        if (mode == PaywallMode.CREDIT_PACK) return UiText.of(Res.string.paywall_cp_cta_buy)
        if (selected != null && selected.hasFreeTrial) {
            return UiText.of(Res.string.paywall_sub_cta_try_for, formatZeroPrice(selected.price))
        }
        return UiText.of(Res.string.paywall_sub_cta_continue)
    }

    /**
     * Single-line conversion lever shown above the CTA. The variant depends on
     * the selected package's intro phase: free trial → "No payment required
     * now"; paid intro with ≥5% discount → "Save N% on your first <period>";
     * otherwise → "Cancel anytime" (or the generic intro line as a fallback).
     */
    private fun aboveCtaText(mode: PaywallMode, selected: PurchasePackage?): UiText {
        if (mode == PaywallMode.CREDIT_PACK) return UiText.of(Res.string.paywall_cp_reassurance)
        val intro = selected?.introductoryOffer
        if (selected?.hasFreeTrial == true) return UiText.of(Res.string.paywall_sub_reassurance_trial)
        if (intro != null && intro.mode != IntroductoryOffer.Mode.FREE_TRIAL) {
            val percent = computeIntroDiscountPercent(selected)
            if (percent != null && percent >= 5) {
                return UiText.ofComposed(
                    Res.string.paywall_sub_reassurance_intro,
                    UiText.of(percent.toString()),
                    durationText(intro.periodUnit, intro.periods),
                )
            }
            return UiText.of(Res.string.paywall_sub_reassurance_intro_generic)
        }
        return UiText.of(Res.string.paywall_sub_reassurance_cancel)
    }

    /**
     * Below-CTA legal disclosure required by the App Store and Play Store when
     * an intro phase exists ("$0.99/month for first 3 months, then $9.99/month.
     * Cancel anytime."). Returns null for credit packs and for plans that have
     * no intro phase — no template fires in those cases.
     */
    private fun belowCtaText(mode: PaywallMode, selected: PurchasePackage?): UiText? {
        if (mode == PaywallMode.CREDIT_PACK || selected == null) return null
        val intro = selected.introductoryOffer
        val isFreeTrial = selected.hasFreeTrial
        val isPaidIntro = intro != null && intro.mode != IntroductoryOffer.Mode.FREE_TRIAL
        if (!isFreeTrial && !isPaidIntro) return null

        val priceText = selected.price.localizedString.orEmpty()
        val afterPart: UiText = if (priceText.isNotBlank()) {
            UiText.ofComposed(
                Res.string.paywall_sub_disclosure_then,
                priceWithPeriodSuffix(priceText, selected.period),
            )
        } else {
            UiText.of(Res.string.paywall_sub_disclosure_then_standard)
        }
        val introPart: UiText = when {
            intro == null || isFreeTrial -> {
                val duration = trialDuration(selected, intro)
                if (duration == null) {
                    UiText.of(Res.string.paywall_sub_disclosure_trial_generic)
                } else {
                    UiText.ofComposed(Res.string.paywall_sub_disclosure_trial, duration)
                }
            }

            intro.mode == IntroductoryOffer.Mode.PAY_UP_FRONT -> {
                UiText.ofComposed(
                    Res.string.paywall_sub_disclosure_intro_upfront,
                    UiText.of(intro.price.localizedString.orEmpty()),
                    durationText(intro.periodUnit, intro.periods),
                )
            }

            intro.mode == IntroductoryOffer.Mode.PAY_AS_YOU_GO -> {
                // Template "%1$s/%2$s for first %3$s" — %2$s is the per-period
                // rate suffix (always singular: "$X/month", never "$X/months"),
                // %3$s is the total duration ("3 months").
                UiText.ofComposed(
                    Res.string.paywall_sub_disclosure_intro_pay_as_you_go,
                    UiText.of(intro.price.localizedString.orEmpty()),
                    unitWord(intro.periodUnit),
                    durationText(intro.periodUnit, intro.periods),
                )
            }

            else -> UiText.of(Res.string.paywall_sub_reassurance_intro_generic)
        }
        return UiText.ofComposed(
            Res.string.paywall_sub_disclosure_template,
            introPart,
            afterPart,
            UiText.of(Res.string.paywall_sub_reassurance_cancel),
        )
    }

    /**
     * Bare-word noun for [unit] — "month" / "months" / "day" / "days" — with
     * the [count] controlling which grammatical form the plural resource emits.
     * Defaults to singular since per-rate suffixes ("$X/month") are the most
     * common use case; pass a larger count for "your first N months" style copy.
     */
    private fun unitWord(unit: PeriodUnit?, count: Int = 1): UiText {
        val res = unit?.bareWordPlural() ?: return UiText.of(Res.string.paywall_unit_period)
        return UiText.of(res, count)
    }

    /**
     * Localized duration like "7 days", "1 month", "3 months". For count = 1 we
     * emit the bare unit word ("month") to read more naturally than "1 month".
     */
    private fun durationText(unit: PeriodUnit?, count: Int): UiText {
        if (unit == null || unit == PeriodUnit.UNKNOWN) return UiText.of(Res.string.paywall_unit_period)
        return if (count <= 1) {
            UiText.of(unit.bareWordPlural(), 1)
        } else {
            UiText.of(unit.countPlural(), count, count)
        }
    }

    /**
     * Build the trial duration display ("7 days", "1 month", "1 year") from the
     * intro offer's [PeriodUnit] when available; falls back to days via
     * `durationDays` for legacy providers that don't surface the unit.
     */
    private fun trialDuration(pkg: PurchasePackage, intro: IntroductoryOffer?): UiText? {
        val unit = intro?.periodUnit
        val periods = intro?.periods ?: 1
        if (unit != null && unit != PeriodUnit.UNKNOWN && periods > 0) {
            return durationText(unit, periods)
        }
        val days = pkg.trialDays ?: intro?.durationDays
        if (days == null || days <= 0) return null
        return UiText.of(Res.plurals.paywall_unit_day_count, days, days)
    }

    /**
     * Discount of the intro phase against the full price (0-100). Free trials
     * are always 100% off; degenerate cases (missing prices, intro ≥ regular)
     * return null so the reassurance line falls back to the generic copy.
     */
    private fun computeIntroDiscountPercent(selectedPackage: PurchasePackage?): Int? {
        val pkg = selectedPackage ?: return null
        val intro = pkg.introductoryOffer ?: return null
        if (intro.mode == IntroductoryOffer.Mode.FREE_TRIAL) return 100
        val regular = pkg.price.amount
        val introPrice = intro.price.amount
        if (regular <= 0f || introPrice < 0f || introPrice >= regular) return null
        return ((regular - introPrice) / regular * 100).toInt()
    }

    /**
     * Build a "zero in the user's currency" string from the selected package's price.
     * Mirrors the symbol position (prefix vs suffix) and decimal precision (JPY has
     * none) of the localized price string the SDK returned.
     */
    private fun formatZeroPrice(price: Price?): String {
        if (price == null) return "0"
        val symbol = price.currencyCodeOrSymbol.orEmpty()
        val localized = price.localizedString.orEmpty()
        val hasDecimal = localized.contains('.') || localized.contains(',')
        val zero = if (hasDecimal) "0.00" else "0"
        if (symbol.isEmpty()) return zero
        val isPrefix = localized.trimStart().startsWith(symbol)
        return if (isPrefix) "$symbol$zero" else "$zero $symbol"
    }

    /** Bare-word plural resource — emits "day" / "days" with no number. */
    private fun PeriodUnit.bareWordPlural(): PluralStringResource = when (this) {
        PeriodUnit.DAY -> Res.plurals.paywall_unit_day
        PeriodUnit.WEEK -> Res.plurals.paywall_unit_week
        PeriodUnit.MONTH -> Res.plurals.paywall_unit_month
        PeriodUnit.YEAR -> Res.plurals.paywall_unit_year
        PeriodUnit.UNKNOWN -> Res.plurals.paywall_unit_day
    }

    /** Count-form plural resource — emits "1 day" / "7 days". */
    private fun PeriodUnit.countPlural(): PluralStringResource = when (this) {
        PeriodUnit.DAY -> Res.plurals.paywall_unit_day_count
        PeriodUnit.WEEK -> Res.plurals.paywall_unit_week_count
        PeriodUnit.MONTH -> Res.plurals.paywall_unit_month_count
        PeriodUnit.YEAR -> Res.plurals.paywall_unit_year_count
        PeriodUnit.UNKNOWN -> Res.plurals.paywall_unit_day_count
    }
}

/**
 * Locale-agnostic 2-decimal formatter — avoids pulling in `kotlinx.format` for
 * a single use case. Always emits a `.` separator; acceptable here because the
 * value is sandwiched inside an already-localized price string.
 */
internal fun formatTwoDecimals(value: Float): String {
    val rounded = (value * 100).toInt()
    val whole = rounded / 100
    val frac = (rounded % 100).toString().padStart(2, '0')
    return "$whole.$frac"
}
