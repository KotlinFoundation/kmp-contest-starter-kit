package com.kotlinfoundation.koko.presentation.components.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.kotlinfoundation.koko.presentation.components.ads.interstitial.InterstitialAdLoader
import com.kotlinfoundation.koko.presentation.components.ads.rewarded.RewardedAdLoader

class AdsManagerImpl(private val context: Context) : AdsManager {
    override fun initialize() {
        MobileAds.initialize(context)
    }

    override val interstitialAdLoader: FullScreenAdLoader by lazy { InterstitialAdLoader(context) }
    override val rewardedAdLoader: FullScreenAdLoader by lazy { RewardedAdLoader(context) }
}
