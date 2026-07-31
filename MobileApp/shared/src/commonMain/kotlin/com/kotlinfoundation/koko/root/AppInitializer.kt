package com.kotlinfoundation.koko.root

import com.kotlinfoundation.koko.common.BuildConfig
import com.kotlinfoundation.koko.data.repository.SubscriptionRepository
import com.kotlinfoundation.koko.data.repository.UserRepository
import com.kotlinfoundation.koko.data.source.featureflag.FeatureFlagManager
import com.kotlinfoundation.koko.presentation.components.ads.AdsManager
import com.kotlinfoundation.koko.subscription.api.SubscriptionProvider
import com.kotlinfoundation.koko.subscription.api.runCatchingSuspend
import com.kotlinfoundation.koko.util.ApplicationScope
import com.kotlinfoundation.koko.util.Constants
import com.kotlinfoundation.koko.util.analytics.Analytics
import com.kotlinfoundation.koko.util.defaultAsyncDispatcher
import com.kotlinfoundation.koko.util.isAndroid
import com.kotlinfoundation.koko.util.isDebug
import com.kotlinfoundation.koko.util.logging.AppLogger
import com.kotlinfoundation.koko.util.onApplicationStartPlatformSpecific
import com.mmk.kmpauth.core.KMPAuth
import com.mmk.kmpauth.firebase.firebase
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.google
import com.mmk.kmpnotifier.KMPNotifier
import com.mmk.kmpnotifier.notification.PayloadData
import com.mmk.kmpnotifier.push.PushListener
import com.mmk.kmpnotifier.push.firebase.addPushListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

/**
 * App bootstrap: starts Koin with all modules and runs one-time startup side effects
 * (logging, analytics, notifications, billing, ads, anonymous sign-in). Each platform's entry
 * point calls [initialize] once, passing platform-specific Koin setup via [onKoinStart].
 */
object AppInitializer {

    fun initialize(onKoinStart: KoinApplication.() -> Unit) {
        startKoin {
            onKoinStart()
            modules(appModules)
            onApplicationStart()
        }
    }

    private fun KoinApplication.onApplicationStart() {
        onApplicationStartPlatformSpecific()
        // Add common application start functions below

        // initialize logging
        AppLogger.initialize(isDebug = isDebug)

        refreshFeatureFlags()
        initializeAnalytics()
        initializeNotification()
        initializeAuthentication()
        initializeInAppPurchase()
        initializeAds()

        val userRepository by this.koin.inject<UserRepository>()
        userRepository.signInAnonymouslyIfNecessary()
    }
}

private fun KoinApplication.refreshFeatureFlags() {
    val featureFlagManager by this.koin.inject<FeatureFlagManager>()
    featureFlagManager.syncsFlagsAsync()
}

private fun KoinApplication.initializeAnalytics() {
    val featureFlagManager by this.koin.inject<FeatureFlagManager>()
    val analytics by this.koin.inject<Analytics>()
    val isAnalyticsEnabled =
        featureFlagManager.getBoolean(FeatureFlagManager.Keys.IS_ANALYTICS_ENABLED)
    analytics.setEnabled(enabled = isAnalyticsEnabled)
}

private fun KoinApplication.initializeAds() {
    val backgroundScope = CoroutineScope(defaultAsyncDispatcher)
    val adsManager by this.koin.inject<AdsManager>()
    val featureFlagManager by this.koin.inject<FeatureFlagManager>()
    val isAdsEnabled = featureFlagManager.getBoolean(FeatureFlagManager.Keys.IS_ADS_ENABLED)
    if (isAdsEnabled.not()) return

    // Initialize ads
    backgroundScope.launch {
        adsManager.initialize()
    }
}

private fun initializeNotification() {
    KMPNotifier.addListener(object : KMPNotifier.Listener {

        /**
         * This method is invoked when the user clicks on a notification.
         * @param data parameter contains the payload data sent with the notification
         */
        override fun onNotificationClicked(data: PayloadData) {
            super.onNotificationClicked(data)
            AppLogger.d("onNotification clicked: $data")
        }
    })

    KMPNotifier.addPushListener(object : PushListener {

        /**
         * This method is called when a new FCM token is generated.
         * You can use this token for sending notifications to the specific device or saving in the server.
         * It is logged for debugging purposes.
         */
        override fun onNewToken(token: String) {
            super.onNewToken(token)
            AppLogger.d("Firebase onNewToken: $token")
        }

        /**
         * This method is invoked when receiving a push notification.
         * @param title parameter contains the notification title
         * @param body parameter contains the notification body
         * @param data parameter contains the payload data sent with the notification
         */
        override fun onPushNotificationWithPayloadData(
            title: String?,
            body: String?,
            data: PayloadData,
        ) {
            super.onPushNotificationWithPayloadData(title, body, data)
            AppLogger.d("Firebase onPushNotification: title: $title, body: $body, data: $data")
        }
    })
}

private fun initializeAuthentication() {
    KMPAuth.initialize {
        google(GoogleAuthCredentials(serverId = BuildConfig.GOOGLE_WEB_CLIENT_ID))
        // Desktop/Web only: the Firebase backend has no native SDK there, so it needs explicit config.
        // No-op on Android/iOS. Blank until the developer sets the FIREBASE_* keys (Firebase console → Web app).
        if (BuildConfig.FIREBASE_API_KEY.isNotBlank()) {
            firebase(
                apiKey = BuildConfig.FIREBASE_API_KEY,
                projectId = BuildConfig.FIREBASE_PROJECT_ID,
                applicationId = BuildConfig.FIREBASE_APPLICATION_ID,
            )
        }
    }
}

private fun KoinApplication.initializeInAppPurchase() {
    // No premium features (PREMIUM_FEATURES_ENABLED = false): don't initialize billing or preload paywalls.
    if (!AppConfiguration.PREMIUM_FEATURES_ENABLED) return

    val subscriptionProvider by this.koin.inject<SubscriptionProvider>()
    val subscriptionRepository by this.koin.inject<SubscriptionRepository>()
    val applicationScope by this.koin.inject<ApplicationScope>()

    applicationScope.launch(Dispatchers.Main.immediate) {
        val subscriptionProviderApiKey =
            if (isAndroid) BuildConfig.SUBSCRIPTION_PROVIDER_ANDROID_API_KEY else BuildConfig.SUBSCRIPTION_PROVIDER_IOS_API_KEY
        subscriptionProvider.initialize(subscriptionProviderApiKey)

        listOf(
            Constants.PAYWALL_PLACEMENT_DEFAULT,
            Constants.PAYWALL_PLACEMENT_ONBOARDING,
            Constants.PAYWALL_PLACEMENT_CREDITS_PACK,
        ).forEach { placementId ->
            launch(defaultAsyncDispatcher) {
                runCatchingSuspend { subscriptionRepository.getPackageList(placementId) }
                    .onFailure { error ->
                        AppLogger.d("Preload paywall '$placementId' failed: ${error.message}")
                    }
            }
        }
    }
}

// Koin module definitions (appModules, dataModule, presentationModule, …) live in Di.kt.
