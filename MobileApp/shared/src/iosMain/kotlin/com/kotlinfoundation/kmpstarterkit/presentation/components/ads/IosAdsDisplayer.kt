package com.kotlinfoundation.kmpstarterkit.presentation.components.ads

interface IosAdsDisplayer {

    fun provideInterstitialAdDisplayer(adLoader: FullScreenAdLoader): FullScreenAdDisplayer
    fun provideRewardedAdDisplayer(
        adLoader: FullScreenAdLoader,
        onRewarded: (AdsRewardItem) -> Unit,
    ): FullScreenAdDisplayer
}
