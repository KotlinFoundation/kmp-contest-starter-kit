package com.kotlinfoundation.koko.presentation.components.premium

import com.kotlinfoundation.koko.designsystem.components.premium.PremiumFeatureUiState
import com.kotlinfoundation.koko.designsystem.util.UiText
import com.kotlinfoundation.koko.domain.model.Subscription
import com.kotlinfoundation.koko.domain.model.isFree
import com.kotlinfoundation.koko.generated.resources.Res
import com.kotlinfoundation.koko.generated.resources.paywall_sub_feature_1
import com.kotlinfoundation.koko.generated.resources.paywall_sub_feature_2
import com.kotlinfoundation.koko.generated.resources.paywall_sub_feature_3
import com.kotlinfoundation.koko.generated.resources.paywall_sub_feature_4

/**
 * Single source of truth for the list of features displayed everywhere that
 * advertises the premium tier — paywall, onboarding upsell, subscriptions screen,
 * successful-purchase view. Pulls from `paywall_sub_feature_*` string resources so
 * the same wording (and translations) appears in every surface.
 *
 * Features carry their text as [UiText], so this object is plain (not @Composable)
 * and safe to call from any layer — the string is resolved at the render site.
 *
 * If a backend / provider returns per-subscription benefits, [ofSubscription] uses
 * those; otherwise it falls back to [defaultPremiumFeatures].
 */
// TODO Add default features here
object PremiumFeatureFactory {
    val defaultPremiumFeatures: List<PremiumFeatureUiState> = listOf(
        PremiumFeatureUiState(UiText.of(Res.string.paywall_sub_feature_1)),
        PremiumFeatureUiState(UiText.of(Res.string.paywall_sub_feature_2)),
        PremiumFeatureUiState(UiText.of(Res.string.paywall_sub_feature_3)),
        PremiumFeatureUiState(UiText.of(Res.string.paywall_sub_feature_4)),
    )

    val defaultFreeFeatures: List<PremiumFeatureUiState> = listOf(
        PremiumFeatureUiState(UiText.of(Res.string.paywall_sub_feature_1), isIncluded = false),
        PremiumFeatureUiState(UiText.of(Res.string.paywall_sub_feature_2), isIncluded = false),
        PremiumFeatureUiState(UiText.of(Res.string.paywall_sub_feature_3), isIncluded = false),
        PremiumFeatureUiState(UiText.of(Res.string.paywall_sub_feature_4), isIncluded = false),
    )

    fun ofSubscription(subscription: Subscription?): List<PremiumFeatureUiState> = if (subscription.isFree) {
        defaultFreeFeatures
    } else {
        (subscription?.benefits?.map { PremiumFeatureUiState(UiText.of(it)) } ?: emptyList())
            .ifEmpty { defaultPremiumFeatures }
    }
}
