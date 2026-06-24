package com.kotlinfoundation.kmpstarterkit.util

import com.kotlinfoundation.kmpstarterkit.data.source.featureflag.FeatureFlagManager
import com.kotlinfoundation.kmpstarterkit.presentation.components.ads.AdsManager
import com.kotlinfoundation.kmpstarterkit.presentation.components.ads.IosAdsDisplayer
import com.kotlinfoundation.kmpstarterkit.util.analytics.Analytics

/**
This factory is used to help to use swift libraries in KMP. Actual implementations are provided in swift.
 */
interface SwiftLibDependencyFactory {
    fun provideFeatureFlagManagerImpl(): FeatureFlagManager
    fun provideFirebaseAnalyticsImpl(): Analytics
    fun provideAdsManagerImpl(): AdsManager
    fun provideIosAdsDisplayer(): IosAdsDisplayer
}
