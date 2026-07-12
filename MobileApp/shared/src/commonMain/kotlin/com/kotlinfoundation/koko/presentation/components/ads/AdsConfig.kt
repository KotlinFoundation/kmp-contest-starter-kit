package com.kotlinfoundation.koko.presentation.components.ads

import com.kotlinfoundation.koko.common.BuildConfig
import com.kotlinfoundation.koko.util.isAndroid
import com.kotlinfoundation.koko.util.isDebug

object AdsConfig {

    // This values provided by Google for testing admob ads
    private object DEBUG {
        const val ADMOB_BANNER_AD_ID = "ca-app-pub-3940256099942544/9214589741"
        const val ADMOB_INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/1033173712"
        const val ADMOB_REWARDED_AD_ID = "ca-app-pub-3940256099942544/5224354917"
    }

    fun getBannerAdId(): String {
        val debugAdId = DEBUG.ADMOB_BANNER_AD_ID
        val releaseAdId =
            if (isAndroid) {
                BuildConfig.ADMOB_BANNER_AD_ID_ANDROID
            } else {
                BuildConfig.ADMOB_BANNER_AD_ID_IOS
            }

        return if (isDebug) debugAdId else releaseAdId.ifEmpty { debugAdId }
    }

    fun getInterstitialAdId(): String {
        val debugAdId = DEBUG.ADMOB_INTERSTITIAL_AD_ID
        val releaseAdId =
            if (isAndroid) {
                BuildConfig.ADMOB_INTERSTITIAL_AD_ID_ANDROID
            } else {
                BuildConfig.ADMOB_INTERSTITIAL_AD_ID_IOS
            }

        return if (isDebug) debugAdId else releaseAdId.ifEmpty { debugAdId }
    }

    fun getRewardedAdId(): String {
        val debugAdId = DEBUG.ADMOB_REWARDED_AD_ID
        val releaseAdId =
            if (isAndroid) {
                BuildConfig.ADMOB_REWARDED_AD_ID_ANDROID
            } else {
                BuildConfig.ADMOB_REWARDED_AD_ID_IOS
            }

        return if (isDebug) debugAdId else releaseAdId.ifEmpty { debugAdId }
    }
}
