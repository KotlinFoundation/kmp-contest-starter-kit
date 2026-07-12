package com.kotlinfoundation.koko.presentation.components.ads

object NoImplFullScreenAdLoader : FullScreenAdLoader {
    override fun load() {
        println("Implementation is not available in this platform")
    }
}

object NoImplFullScreenAdDisplayer : FullScreenAdDisplayer {
    override fun show() {
        println("Implementation is not available in this platform")
    }
}

object NoImplAdsManager : AdsManager {
    override fun initialize() {
        println("Implementation is not available in this platform")
    }

    override val interstitialAdLoader: FullScreenAdLoader
        get() = NoImplFullScreenAdLoader
    override val rewardedAdLoader: FullScreenAdLoader
        get() = NoImplFullScreenAdLoader
}
