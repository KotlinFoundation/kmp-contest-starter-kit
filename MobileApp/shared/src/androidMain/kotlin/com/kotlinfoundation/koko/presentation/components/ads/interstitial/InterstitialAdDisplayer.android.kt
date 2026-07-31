package com.kotlinfoundation.koko.presentation.components.ads.interstitial

import androidx.activity.ComponentActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.kotlinfoundation.koko.presentation.components.ads.FullScreenAdDisplayer
import com.kotlinfoundation.koko.presentation.components.ads.FullScreenAdLoader
import com.kotlinfoundation.koko.util.logging.AppLogger

class InterstitialAdDisplayer(
    private val activity: ComponentActivity?,
    private val adLoader: FullScreenAdLoader,
) : FullScreenAdDisplayer {

    override fun show() {
        if (adLoader !is InterstitialAdLoader) return
        val interstitialAd = adLoader.interstitialAd

        if (interstitialAd == null || activity == null) {
            AppLogger.d("Interstitial ad is not loaded yet")
            adLoader.load()
            return
        }

        interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                AppLogger.d("Interstitial ad is dismissed, loading new one")
                adLoader.interstitialAd = null
                adLoader.load()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                super.onAdFailedToShowFullScreenContent(p0)
                AppLogger.e("Interstitial ad, failed to show: ${p0.message}")
                adLoader.interstitialAd = null
            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                AppLogger.d("Interstitial ad is shown")
            }
        }
        interstitialAd.show(activity)
    }
}
