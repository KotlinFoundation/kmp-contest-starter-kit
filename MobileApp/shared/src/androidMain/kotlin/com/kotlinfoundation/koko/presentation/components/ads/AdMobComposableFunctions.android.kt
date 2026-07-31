package com.kotlinfoundation.koko.presentation.components.ads

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.kotlinfoundation.koko.presentation.components.ads.interstitial.InterstitialAdDisplayer
import com.kotlinfoundation.koko.presentation.components.ads.rewarded.RewardedAdDisplayer
import com.kotlinfoundation.koko.util.logging.AppLogger
import org.koin.compose.koinInject

@Composable
actual fun NativeAdmobBanner(modifier: Modifier) {
    var isAdLoadFailed by remember { mutableStateOf(false) }
    if (isAdLoadFailed) return

    val height = 50.dp

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = height)
            .height(height),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdsConfig.getBannerAdId()
                loadAd(AdRequest.Builder().build())
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        AppLogger.d("AdMob: Ad is loaded")
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        AppLogger.d("AdMob: Ad failed to load")
                        isAdLoadFailed = true
                    }
                }
            }
        },
    )
}

@Composable
actual fun rememberNativeInterstitialAdDisplayer(): FullScreenAdDisplayer {
    val adsManager = koinInject<AdsManager>()
    val interstitialAdLoader = adsManager.interstitialAdLoader
    LaunchedEffect(Unit) { interstitialAdLoader.load() }
    val activity = LocalContext.current.getActivity()
    return remember {
        InterstitialAdDisplayer(activity = activity, adLoader = interstitialAdLoader)
    }
}

@Composable
actual fun rememberNativeRewardedAdDisplayer(onRewarded: (AdsRewardItem) -> Unit): FullScreenAdDisplayer {
    val adsManager = koinInject<AdsManager>()
    val rewardedAdLoader = adsManager.rewardedAdLoader
    LaunchedEffect(Unit) { rewardedAdLoader.load() }
    val activity = LocalContext.current.getActivity()
    val updatedOnRewarded by rememberUpdatedState(onRewarded)
    return remember {
        RewardedAdDisplayer(
            activity = activity,
            adLoader = rewardedAdLoader,
            onRewarded = updatedOnRewarded,
        )
    }
}

private fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}
