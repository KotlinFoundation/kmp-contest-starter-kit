package com.kotlinfoundation.koko.presentation.screens.paywall

import com.kotlinfoundation.koko.designsystem.util.UiText
import com.kotlinfoundation.koko.domain.model.Subscription
import com.kotlinfoundation.koko.subscription.api.PurchasePackage
import com.kotlinfoundation.koko.subscription.api.PurchasePackageId
import com.kotlinfoundation.koko.util.UiMessage

data class PaywallUiState(
    val isLoading: Boolean = true,
    val isDismissRequired: Boolean = false,
    val errorMessage: UiMessage? = null,
    val packages: List<PaywallPackageUiState> = emptyList(),
    val buyButtonEnabled: Boolean = false,
    val successfulSubscription: Subscription? = null,
    val signInActionRequired: Boolean = false,
    val mode: PaywallMode = PaywallMode.SUBSCRIPTION,
    val currentPlacementId: String? = null,
    /** Text on the buy CTA button (e.g. "Continue", "Try for $0.00", "Buy credits"). */
    val ctaText: UiText = UiText.empty(),
    /**
     * Bold one-liner shown immediately ABOVE the CTA button — the conversion
     * lever. "No payment required now" on free trials, "Save N% on your
     * first 3 months" on paid intros, "Cancel anytime" otherwise.
     */
    val aboveCtaText: UiText = UiText.empty(),
    /**
     * Fine-print legal text shown immediately BELOW the CTA button. Required
     * by the App Store and Play Store when an intro phase exists ("3 days
     * free, then $X/month. Cancel anytime."). Null when there's nothing to
     * disclose (credit packs and no-intro plans).
     */
    val belowCtaText: UiText? = null,
    /**
     * True when the built-in mock subscription provider is active (no real Adapty/RevenueCat key
     * set). Drives the "Demo paywall" banner so simulated purchases are never mistaken for real.
     */
    val isMock: Boolean = false,
)

enum class PaywallMode { SUBSCRIPTION, CREDIT_PACK }

data class PaywallPackageUiState(
    val purchasePackage: PurchasePackage,
    val title: UiText,
    val subtitle: UiText,
    val priceText: UiText,
    val isSelected: Boolean,
    val isRecommended: Boolean,
    val savingsBadge: UiText? = null,
) {
    val id: PurchasePackageId get() = purchasePackage.id
}

sealed interface PaywallUiEvent {
    data object OnClickBuy : PaywallUiEvent
    data object OnClickRestore : PaywallUiEvent
    data class OnSelectPackage(val packageId: PurchasePackageId) : PaywallUiEvent
}
