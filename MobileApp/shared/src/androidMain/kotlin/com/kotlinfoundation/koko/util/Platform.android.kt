package com.kotlinfoundation.koko.util

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.kotlinfoundation.koko.common.BuildConfig
import com.kotlinfoundation.koko.data.source.featureflag.FeatureFlagManager
import com.kotlinfoundation.koko.data.source.featureflag.FeatureFlagManagerImpl
import com.kotlinfoundation.koko.data.source.local.DatabaseProvider
import com.kotlinfoundation.koko.data.source.local.DatabaseProviderImpl
import com.kotlinfoundation.koko.data.source.local.databaseModule
import com.kotlinfoundation.koko.data.source.preferences.PreferencesDataStoreProvider
import com.kotlinfoundation.koko.data.source.preferences.PreferencesDataStoreProviderImpl
import com.kotlinfoundation.koko.presentation.components.ads.AdsManager
import com.kotlinfoundation.koko.presentation.components.ads.AdsManagerImpl
import com.kotlinfoundation.koko.shared.R
import com.kotlinfoundation.koko.util.analytics.Analytics
import com.kotlinfoundation.koko.util.analytics.FirebaseAnalyticsImpl
import com.kotlinfoundation.koko.util.file.FileManager
import com.kotlinfoundation.koko.util.file.FileManagerImpl
import com.mmk.kmpnotifier.KMPNotifier
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import com.mmk.kmpnotifier.push.firebase.FirebasePush
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule: Module = module {

    includes(databaseModule)
    singleOf(::DatabaseProviderImpl) bind DatabaseProvider::class
    singleOf(::PreferencesDataStoreProviderImpl) bind PreferencesDataStoreProvider::class
    factory<FileManager> { FileManagerImpl() }
    factoryOf(::AppUtilImpl) bind AppUtil::class
    single<FeatureFlagManagerImpl> {
        val remoteConfig = Firebase.remoteConfig.apply {
            setConfigSettingsAsync(
                remoteConfigSettings {
                    // set minimumFetchIntervalInSeconds to 0 to get fresh updates in debug mode for testing
                    if (BuildConfig.DEBUG) minimumFetchIntervalInSeconds = 3600
                },
            )
            setDefaultsAsync(FeatureFlagManager.DEFAULT_VALUES)
        }
        FeatureFlagManagerImpl(remoteConfig = remoteConfig)
    } bind FeatureFlagManager::class
    single { FirebaseAnalyticsImpl(firebaseAnalytics = Firebase.analytics) } bind Analytics::class
    singleOf(::AdsManagerImpl) bind AdsManager::class
}

internal actual fun onApplicationStartPlatformSpecific() {
    KMPNotifier.initialize(
        NotificationPlatformConfiguration.Android(
            notificationIconResId = R.drawable.ic_notification,
        ),
        FirebasePush,
    )
}

actual fun getPlatform(): Platform = Platform.Android

internal actual val isAndroid = true
internal actual val isDebug = BuildConfig.DEBUG
actual val defaultAsyncDispatcher: CoroutineDispatcher = Dispatchers.IO
