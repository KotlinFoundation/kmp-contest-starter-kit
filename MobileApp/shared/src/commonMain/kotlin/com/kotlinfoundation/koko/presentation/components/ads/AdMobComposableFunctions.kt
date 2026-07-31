package com.kotlinfoundation.koko.presentation.components.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.kotlinfoundation.koko.data.source.featureflag.FeatureFlagManager
import com.kotlinfoundation.koko.util.logging.AppLogger
import org.koin.compose.koinInject

@Composable
fun AdmobBanner(modifier: Modifier) {
    val featureFlagManager = koinInject<FeatureFlagManager>()

    // You can add more condition here, such as if user is premium, don't show any ads
    val isShowingAdsAllowed =
        remember { featureFlagManager.getBoolean(FeatureFlagManager.Keys.IS_ADS_ENABLED) }

    if (isShowingAdsAllowed.not()) {
        AppLogger.d("Showing Banner Ads is not allowed, ads are disabled")
        return
    }

    NativeAdmobBanner(modifier)
}

@Composable
fun rememberInterstitialAdDisplayer(): FullScreenAdDisplayer? {
    val featureFlagManager = koinInject<FeatureFlagManager>()
    // You can add more condition here, such as if user is premium, don't show any ads
    val isShowingAdsAllowed =
        remember { featureFlagManager.getBoolean(FeatureFlagManager.Keys.IS_ADS_ENABLED) }

    if (isShowingAdsAllowed.not()) {
        AppLogger.d("Showing Interstitial Ads is not allowed, ads are disabled")
        return null
    }
    return rememberNativeInterstitialAdDisplayer()
}

@Composable
fun rememberRewardedAdDisplayer(onRewarded: (AdsRewardItem) -> Unit): FullScreenAdDisplayer? {
    val featureFlagManager = koinInject<FeatureFlagManager>()
    // You can add more condition here, such as if user is premium, don't show any ads
    val isShowingAdsAllowed =
        remember { featureFlagManager.getBoolean(FeatureFlagManager.Keys.IS_ADS_ENABLED) }

    if (isShowingAdsAllowed.not()) {
        AppLogger.d("Showing Rewarded Ads is not allowed, ads are disabled")
        return null
    }
    return rememberNativeRewardedAdDisplayer(onRewarded)
}

@Composable
expect fun NativeAdmobBanner(modifier: Modifier = Modifier)

@Composable
expect fun rememberNativeInterstitialAdDisplayer(): FullScreenAdDisplayer

@Composable
expect fun rememberNativeRewardedAdDisplayer(onRewarded: (AdsRewardItem) -> Unit): FullScreenAdDisplayer
