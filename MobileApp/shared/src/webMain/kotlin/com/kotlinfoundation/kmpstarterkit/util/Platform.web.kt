package com.kotlinfoundation.kmpstarterkit.util

import com.kotlinfoundation.kmpstarterkit.data.source.featureflag.FeatureFlagManager
import com.kotlinfoundation.kmpstarterkit.data.source.featureflag.NoImplFeatureFlagManager
import com.kotlinfoundation.kmpstarterkit.data.source.local.webDatabaseModule
import com.kotlinfoundation.kmpstarterkit.data.source.preferences.PreferencesDataStoreProvider
import com.kotlinfoundation.kmpstarterkit.data.source.preferences.PreferencesDataStoreProviderImpl
import com.kotlinfoundation.kmpstarterkit.presentation.components.ads.AdsManager
import com.kotlinfoundation.kmpstarterkit.presentation.components.ads.NoImplAdsManager
import com.kotlinfoundation.kmpstarterkit.util.analytics.Analytics
import com.kotlinfoundation.kmpstarterkit.util.analytics.NoImplAnalytics
import com.kotlinfoundation.kmpstarterkit.util.file.FileManager
import com.kotlinfoundation.kmpstarterkit.util.file.FileManagerImpl
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
    factoryOf(::FileManagerImpl) bind FileManager::class
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

internal actual val isAndroid: Boolean get() = false
internal actual val isDebug: Boolean get() = false

actual val defaultAsyncDispatcher: CoroutineDispatcher = Dispatchers.Default
