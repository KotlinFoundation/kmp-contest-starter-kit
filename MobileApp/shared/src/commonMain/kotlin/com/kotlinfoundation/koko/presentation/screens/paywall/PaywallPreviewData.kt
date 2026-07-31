package com.kotlinfoundation.koko.presentation.screens.paywall

import com.kotlinfoundation.koko.designsystem.util.UiText
import com.kotlinfoundation.koko.subscription.api.Price
import com.kotlinfoundation.koko.subscription.api.PurchasePackage
import com.kotlinfoundation.koko.subscription.api.PurchasePackageId
import com.kotlinfoundation.koko.util.Constants

/**
 * Fixture builders for `@Preview` and store-screenshot composables. Kept out of
 * the screen files so previews can stay one-liners and the same data can be
 * reused across multiple preview functions (default state, trial state, etc.).
 *
 * These are *display fixtures only* — they bypass the real
 * [PaywallUiStateMapper] so previews never depend on string resources or
 * billing logic. For tests that exercise the mapper, build real
 * [PurchasePackage] lists and call the mapper directly.
 */
internal object PaywallPreviewData {

    /** A fully-formed subscription pricing card, ready to drop into a preview list. */
    fun subscriptionPackage(
        id: String,
        title: UiText,
        subtitle: UiText,
        priceText: UiText,
        isSelected: Boolean = false,
        isRecommended: Boolean = false,
        savingsBadge: UiText? = null,
    ): PaywallPackageUiState = PaywallPackageUiState(
        purchasePackage = PurchasePackage(
            id = PurchasePackageId(id),
            price = Price(amount = 0f, currencyCodeOrSymbol = "$", localizedString = "$0"),
            title = id,
        ),
        title = title,
        subtitle = subtitle,
        priceText = priceText,
        isSelected = isSelected,
        isRecommended = isRecommended,
        savingsBadge = savingsBadge,
    )

    /** A credit-pack row with a parsable product id (so `parseCreditAmount` works in tests). */
    fun creditPack(
        credits: Int,
        priceText: String,
        perCreditText: String,
        isSelected: Boolean = false,
        isBestValue: Boolean = false,
    ): PaywallPackageUiState = PaywallPackageUiState(
        purchasePackage = PurchasePackage(
            id = PurchasePackageId("${Constants.CREDIT_PACK_PRODUCT_ID_PREFIX}$credits"),
            price = Price(amount = 0f, currencyCodeOrSymbol = "$", localizedString = priceText),
            title = "$credits credits",
        ),
        title = UiText.of("$credits credits"),
        subtitle = UiText.of(perCreditText),
        priceText = UiText.of(priceText),
        isSelected = isSelected,
        isRecommended = isBestValue,
        savingsBadge = if (isBestValue) UiText.of("BEST VALUE") else null,
    )

    /**
     * Three-tier subscription state (weekly / monthly / yearly) with the yearly
     * plan selected. Set [trialAvailable] = true to flip the footer copy into
     * the free-trial variant ("Try for $0.00" / "No payment required now" +
     * compliance disclosure).
     */
    fun subscriptionState(trialAvailable: Boolean = false): PaywallUiState {
        val weekly = subscriptionPackage(
            id = "sub_weekly",
            title = UiText.of("1 Week"),
            subtitle = UiText.of("$6.99 per week"),
            priceText = UiText.of("$6.99/week"),
        )
        val yearly = subscriptionPackage(
            id = "sub_yearly",
            title = UiText.of("1 Year"),
            subtitle = UiText.of("$0.58 per week"),
            priceText = UiText.of("$29.99/year"),
            isSelected = true,
            isRecommended = true,
            savingsBadge = UiText.of(if (trialAvailable) "BEST VALUE" else "SAVE 92%"),
        )
        return PaywallUiState(
            isLoading = false,
            packages = listOf(weekly, yearly),
            buyButtonEnabled = true,
            ctaText = UiText.of(if (trialAvailable) "Try for $0.00" else "Continue"),
            aboveCtaText = UiText.of(
                if (trialAvailable) "No payment required now" else "Cancel anytime",
            ),
            belowCtaText = if (trialAvailable) {
                UiText.of("3 days free, then $49.99/year. Cancel anytime.")
            } else {
                null
            },
        )
    }

    /**
     * Subscription state where the selected plan has a paid-intro phase. Covers
     * the third footer variant: "Save N% on your first 3 months" reassurance +
     * pay-as-you-go disclosure.
     */
    fun paidIntroSubscriptionState(): PaywallUiState {
        val monthly = subscriptionPackage(
            id = "sub_monthly_paid_intro",
            title = UiText.of("1 Month"),
            subtitle = UiText.of("$2.30 per week"),
            priceText = UiText.of("$9.99/month"),
            isSelected = true,
        )
        val yearly = subscriptionPackage(
            id = "sub_yearly_no_intro",
            title = UiText.of("1 Year"),
            subtitle = UiText.of("$0.96 per week"),
            priceText = UiText.of("$49.99/year"),
            isRecommended = true,
            savingsBadge = UiText.of("BEST VALUE"),
        )
        return PaywallUiState(
            isLoading = false,
            packages = listOf(monthly, yearly),
            buyButtonEnabled = true,
            ctaText = UiText.of("Continue"),
            aboveCtaText = UiText.of("Save 90% on your first 3 months"),
            belowCtaText = UiText.of(
                "$0.99/month for first 3 months, then $9.99/month. Cancel anytime.",
            ),
        )
    }

    /** Three credit packs (10 / 30 / 80) with the 30-pack flagged as BEST VALUE. */
    fun creditPackState(): PaywallUiState = PaywallUiState(
        isLoading = false,
        packages = listOf(
            creditPack(10, "$4.99", "$0.49 per credit"),
            creditPack(30, "$9.99", "$0.33 per credit", isBestValue = true, isSelected = true),
            creditPack(80, "$19.99", "$0.25 per credit"),
        ),
        buyButtonEnabled = true,
        ctaText = UiText.of("Buy credits"),
        aboveCtaText = UiText.of("No subscription · One-time purchase"),
        mode = PaywallMode.CREDIT_PACK,
    )
}
