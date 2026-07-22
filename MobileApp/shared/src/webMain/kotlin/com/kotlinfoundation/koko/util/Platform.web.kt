package com.kotlinfoundation.koko.util

import com.kotlinfoundation.koko.data.source.featureflag.FeatureFlagManager
import com.kotlinfoundation.koko.data.source.featureflag.NoImplFeatureFlagManager
import com.kotlinfoundation.koko.data.source.local.webDatabaseModule
import com.kotlinfoundation.koko.data.source.preferences.PreferencesDataStoreProvider
import com.kotlinfoundation.koko.data.source.preferences.PreferencesDataStoreProviderImpl
import com.kotlinfoundation.koko.presentation.components.ads.AdsManager
import com.kotlinfoundation.koko.presentation.components.ads.NoImplAdsManager
import com.kotlinfoundation.koko.util.analytics.Analytics
import com.kotlinfoundation.koko.util.analytics.NoImplAnalytics
import com.kotlinfoundation.koko.util.file.FileManager
import com.kotlinfoundation.koko.util.file.FileManagerImpl
import com.mmk.kmpnotifier.KMPNotifier
import com.mmk.kmpnotifier.local.LocalNotifications
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule: Module = module {
    includes(webDatabaseModule)
    singleOf(::PreferencesDataStoreProviderImpl) bind PreferencesDataStoreProvider::class
    factoryOf(::AppUtilImpl) bind AppUtil::class
    // Singleton, not factory: the web FileManager holds an in-memory byte store, so every
    // consumer must share the same instance (a fresh factory instance would be empty).
    singleOf(::FileManagerImpl) bind FileManager::class
    single { NoImplFeatureFlagManager } bind FeatureFlagManager::class
    single { NoImplAnalytics } bind Analytics::class
    single { NoImplAdsManager } bind AdsManager::class
}

internal actual fun onApplicationStartPlatformSpecific() {
    KMPNotifier.initialize(
        NotificationPlatformConfiguration.Web(askNotificationPermissionOnStart = false),
        LocalNotifications,
    )
}

actual fun getPlatform(): Platform = Platform.Web

internal actual val isAndroid: Boolean get() = false
internal actual val isDebug: Boolean get() = false

actual val defaultAsyncDispatcher: CoroutineDispatcher = Dispatchers.Default
