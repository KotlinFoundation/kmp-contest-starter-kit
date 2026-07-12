package com.kotlinfoundation.koko.presentation.screens.paywall

import com.kotlinfoundation.koko.designsystem.util.UiText
import com.kotlinfoundation.koko.generated.resources.Res
import com.kotlinfoundation.koko.generated.resources.paywall_badge_best_value
import com.kotlinfoundation.koko.generated.resources.paywall_badge_save_percent
import com.kotlinfoundation.koko.generated.resources.paywall_cp_cta_buy
import com.kotlinfoundation.koko.generated.resources.paywall_sub_cta_continue
import com.kotlinfoundation.koko.generated.resources.paywall_sub_cta_try_for
import com.kotlinfoundation.koko.generated.resources.paywall_sub_reassurance_cancel
import com.kotlinfoundation.koko.generated.resources.paywall_sub_reassurance_trial
import com.kotlinfoundation.koko.subscription.api.BillingPeriod
import com.kotlinfoundation.koko.subscription.api.IntroductoryOffer
import com.kotlinfoundation.koko.subscription.api.Price
import com.kotlinfoundation.koko.subscription.api.PurchasePackage
import com.kotlinfoundation.koko.subscription.api.PurchasePackageId
import org.jetbrains.compose.resources.StringResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * [PaywallUiStateMapper] is pure and stateless, so it's tested without Koin or
 * coroutines. String resources aren't resolved here (that needs a Compose
 * environment) — assertions check which resource template was picked via
 * [UiText.Resource.id] / [UiText.ComposedResource.id].
 */
class PaywallUiStateMapperTest {

    private val mapper = PaywallUiStateMapper()

    // ── pickDefaultSelection ─────────────────────────────────────────────────

    @Test
    fun `default selection is null for empty package list`() {
        assertNull(mapper.pickDefaultSelection(emptyList(), PaywallMode.SUBSCRIPTION))
        assertNull(mapper.pickDefaultSelection(emptyList(), PaywallMode.CREDIT_PACK))
    }

    @Test
    fun `default subscription selection is the most expensive plan`() {
        val packages = listOf(
            subscription("sub_weekly", 6.99f, BillingPeriod.WEEKLY),
            subscription("sub_yearly", 49.99f, BillingPeriod.YEARLY),
            subscription("sub_monthly", 9.99f, BillingPeriod.MONTHLY),
        )
        val selected = mapper.pickDefaultSelection(packages, PaywallMode.SUBSCRIPTION)
        assertEquals(PurchasePackageId("sub_yearly"), selected)
    }

    @Test
    fun `single subscription plan falls back to itself`() {
        val packages = listOf(subscription("sub_only", 9.99f, BillingPeriod.MONTHLY))
        assertEquals(
            PurchasePackageId("sub_only"),
            mapper.pickDefaultSelection(packages, PaywallMode.SUBSCRIPTION),
        )
    }

    @Test
    fun `default credit pack selection is the lowest price per credit`() {
        val packages = listOf(
            creditPack(10, 4.99f), // $0.499 / credit
            creditPack(80, 19.99f), // $0.249 / credit  ← best value
            creditPack(30, 9.99f), // $0.333 / credit
        )
        assertEquals(
            PurchasePackageId("credit_pack_80"),
            mapper.pickDefaultSelection(packages, PaywallMode.CREDIT_PACK),
        )
    }

    @Test
    fun `credit packs with malformed product ids fall back to first package`() {
        val packages = listOf(
            PurchasePackage(
                id = PurchasePackageId("not_a_credit_pack"),
                price = Price(amount = 4.99f, localizedString = "$4.99"),
                title = "Mystery pack",
            ),
        )
        assertEquals(
            PurchasePackageId("not_a_credit_pack"),
            mapper.pickDefaultSelection(packages, PaywallMode.CREDIT_PACK),
        )
    }

    // ── map: subscription cards ──────────────────────────────────────────────

    @Test
    fun `subscription cards are sorted by price with recommended plan badged`() {
        val packages = listOf(
            subscription("sub_yearly", 49.99f, BillingPeriod.YEARLY),
            subscription("sub_weekly", 6.99f, BillingPeriod.WEEKLY),
        )
        val mapped = mapper.map(packages, selectedId = PurchasePackageId("sub_weekly"), mode = PaywallMode.SUBSCRIPTION)

        assertEquals(listOf("sub_weekly", "sub_yearly"), mapped.packages.map { it.purchasePackage.id.value })

        val weekly = mapped.packages[0]
        val yearly = mapped.packages[1]
        assertTrue(weekly.isSelected)
        assertTrue(!weekly.isRecommended && weekly.savingsBadge == null)
        assertTrue(yearly.isRecommended)
        assertNotNull(yearly.savingsBadge)
    }

    @Test
    fun `recommended plan shows save percent badge when cheaper per day than the anchor`() {
        // Weekly $6.99 → ~$1.00/day. Yearly $49.99 → ~$0.14/day → ~86% saving.
        val packages = listOf(
            subscription("sub_weekly", 6.99f, BillingPeriod.WEEKLY),
            subscription("sub_yearly", 49.99f, BillingPeriod.YEARLY),
        )
        val mapped = mapper.map(packages, selectedId = null, mode = PaywallMode.SUBSCRIPTION)
        val badge = mapped.packages.last().savingsBadge

        assertResource(Res.string.paywall_badge_save_percent, badge)
    }

    @Test
    fun `recommended plan falls back to best value badge when savings can't be computed`() {
        // LIFETIME has no period → per-day comparison is impossible.
        val packages = listOf(
            subscription("sub_weekly", 6.99f, BillingPeriod.WEEKLY),
            subscription("sub_lifetime", 99.99f, BillingPeriod.LIFETIME),
        )
        val mapped = mapper.map(packages, selectedId = null, mode = PaywallMode.SUBSCRIPTION)
        val badge = mapped.packages.last().savingsBadge

        assertResource(Res.string.paywall_badge_best_value, badge)
    }

    // ── map: footer copy ─────────────────────────────────────────────────────

    @Test
    fun `plan without intro offer gets continue CTA and cancel-anytime reassurance`() {
        val packages = listOf(subscription("sub_monthly", 9.99f, BillingPeriod.MONTHLY))
        val mapped = mapper.map(packages, selectedId = PurchasePackageId("sub_monthly"), mode = PaywallMode.SUBSCRIPTION)

        assertResource(Res.string.paywall_sub_cta_continue, mapped.ctaText)
        assertResource(Res.string.paywall_sub_reassurance_cancel, mapped.aboveCtaText)
        assertNull(mapped.belowCtaText)
    }

    @Test
    fun `free trial plan gets try-for CTA plus trial reassurance and a disclosure line`() {
        val trial = subscription("sub_yearly", 49.99f, BillingPeriod.YEARLY).copy(
            introductoryOffer = IntroductoryOffer(
                price = Price(amount = 0f, localizedString = "$0.00"),
                durationDays = 7,
                mode = IntroductoryOffer.Mode.FREE_TRIAL,
            ),
        )
        val mapped = mapper.map(listOf(trial), selectedId = trial.id, mode = PaywallMode.SUBSCRIPTION)

        assertResource(Res.string.paywall_sub_cta_try_for, mapped.ctaText)
        assertResource(Res.string.paywall_sub_reassurance_trial, mapped.aboveCtaText)
        assertNotNull(mapped.belowCtaText)
    }

    // ── map: credit packs ────────────────────────────────────────────────────

    @Test
    fun `credit pack mapping badges the best value pack and uses the buy CTA`() {
        val packages = listOf(
            creditPack(10, 4.99f),
            creditPack(30, 9.99f),
            creditPack(80, 19.99f),
        )
        val mapped = mapper.map(packages, selectedId = PurchasePackageId("credit_pack_30"), mode = PaywallMode.CREDIT_PACK)

        assertResource(Res.string.paywall_cp_cta_buy, mapped.ctaText)
        assertNull(mapped.belowCtaText)

        val best = mapped.packages.single { it.isRecommended }
        assertEquals("credit_pack_80", best.purchasePackage.id.value)
        assertResource(Res.string.paywall_badge_best_value, best.savingsBadge)

        val selected = mapped.packages.single { it.isSelected }
        assertEquals("credit_pack_30", selected.purchasePackage.id.value)
    }

    // ── formatTwoDecimals ────────────────────────────────────────────────────

    @Test
    fun `formatTwoDecimals always emits two fraction digits`() {
        assertEquals("0.49", formatTwoDecimals(0.499f))
        assertEquals("1.00", formatTwoDecimals(1.0f))
        assertEquals("12.05", formatTwoDecimals(12.055f))
    }

    // ── fixtures ─────────────────────────────────────────────────────────────

    private fun subscription(id: String, amount: Float, period: BillingPeriod) = PurchasePackage(
        id = PurchasePackageId(id),
        price = Price(amount = amount, currencyCodeOrSymbol = "$", localizedString = "$$amount"),
        title = id,
        period = period,
    )

    private fun creditPack(credits: Int, amount: Float) = PurchasePackage(
        id = PurchasePackageId("credit_pack_$credits"),
        price = Price(amount = amount, currencyCodeOrSymbol = "$", localizedString = "$$amount"),
        title = "$credits credits",
    )

    /** Asserts that [actual] is a [UiText.Resource] built from the [expected] template. */
    private fun assertResource(expected: StringResource, actual: UiText?) {
        assertNotNull(actual, "expected UiText for resource $expected, got null")
        assertTrue(actual is UiText.Resource, "expected UiText.Resource, got ${actual::class.simpleName}")
        assertEquals(expected, actual.id)
    }
}
