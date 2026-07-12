package com.kotlinfoundation.koko.presentation.components.ads

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import com.kotlinfoundation.koko.util.LocalNativeViewFactory
import com.kotlinfoundation.koko.util.logging.AppLogger
import org.koin.compose.koinInject

@Composable
actual fun NativeAdmobBanner(modifier: Modifier) {
    var isAdLoadFailed by remember { mutableStateOf(false) }
    if (isAdLoadFailed) return
    val height = 50.dp
    val factory = LocalNativeViewFactory.current
    val view = remember(factory) {
        factory.createAdmobBannerView(
            bannerId = AdsConfig.getBannerAdId(),
            onAdLoaded = {
                AppLogger.d("AdMob: Ad is loaded")
            },
            onAdFailedToLoad = {
                AppLogger.d("AdMob: Ad failed to load")
                isAdLoadFailed = true
            },
        )
    }
    UIKitViewController(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = height)
            .height(height),
        factory = { view },
        update = {},
        properties = UIKitInteropProperties(
            isInteractive = true,
            isNativeAccessibilityEnabled = true,
        ),
    )
}

@Composable
actual fun rememberNativeInterstitialAdDisplayer(): FullScreenAdDisplayer {
    val iosAdsDisplayer = koinInject<IosAdsDisplayer>()
    val adsManager = koinInject<AdsManager>()
    val adLoader = adsManager.interstitialAdLoader
    LaunchedEffect(Unit) { adLoader.load() }
    return remember { iosAdsDisplayer.provideInterstitialAdDisplayer(adLoader) }
}

@Composable
actual fun rememberNativeRewardedAdDisplayer(onRewarded: (AdsRewardItem) -> Unit): FullScreenAdDisplayer {
    val iosAdsDisplayer = koinInject<IosAdsDisplayer>()
    val adsManager = koinInject<AdsManager>()
    val adLoader = adsManager.rewardedAdLoader
    LaunchedEffect(Unit) { adLoader.load() }
    val updatedOnRewarded by rememberUpdatedState(onRewarded)
    return remember { iosAdsDisplayer.provideRewardedAdDisplayer(adLoader, updatedOnRewarded) }
}
