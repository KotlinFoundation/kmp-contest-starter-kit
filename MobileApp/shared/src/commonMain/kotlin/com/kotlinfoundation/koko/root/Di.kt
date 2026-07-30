package com.kotlinfoundation.koko.root

import com.kotlinfoundation.koko.common.BuildConfig
import com.kotlinfoundation.koko.data.BackgroundExecutor
import com.kotlinfoundation.koko.data.repository.CreditRepository
import com.kotlinfoundation.koko.data.repository.GenerationRepository
import com.kotlinfoundation.koko.data.repository.SubscriptionRepository
import com.kotlinfoundation.koko.data.repository.UserRepository
import com.kotlinfoundation.koko.data.source.ai.OpenAiImageGenerationProvider
import com.kotlinfoundation.koko.data.source.ai.ReplicateGenerationProvider
import com.kotlinfoundation.koko.data.source.preferences.PreferencesDataStoreProvider
import com.kotlinfoundation.koko.data.source.preferences.UserPreferences
import com.kotlinfoundation.koko.data.source.preferences.UserPreferencesImpl
import com.kotlinfoundation.koko.data.source.remote.HttpClientFactory
import com.kotlinfoundation.koko.data.source.remote.apiservices.ApiService
import com.kotlinfoundation.koko.data.source.remote.apiservices.TemporaryFileUploadApiService
import com.kotlinfoundation.koko.data.source.remote.apiservices.ai.AiTransport
import com.kotlinfoundation.koko.data.source.remote.apiservices.ai.OpenAiApiService
import com.kotlinfoundation.koko.data.source.remote.apiservices.ai.ReplicateApiService
import com.kotlinfoundation.koko.domain.model.credit.creditSystemConfig
import com.kotlinfoundation.koko.domain.usecase.AiGenerationProvider
import com.kotlinfoundation.koko.presentation.screens.account.AccountViewModel
import com.kotlinfoundation.koko.presentation.screens.creditbalance.CreditBalanceViewModel
import com.kotlinfoundation.koko.presentation.screens.gallery.GalleryViewModel
import com.kotlinfoundation.koko.presentation.screens.generationresult.GenerationResultViewModel
import com.kotlinfoundation.koko.presentation.screens.home.HomeViewModel
import com.kotlinfoundation.koko.presentation.screens.onboarding.OnBoardingViewModel
import com.kotlinfoundation.koko.presentation.screens.paywall.PaywallViewModel
import com.kotlinfoundation.koko.presentation.screens.profile.ProfileViewModel
import com.kotlinfoundation.koko.presentation.screens.subscriptions.SubscriptionsViewModel
import com.kotlinfoundation.koko.subscription.api.MockSubscriptionProvider
import com.kotlinfoundation.koko.subscription.api.NoOpSubscriptionProviderUi
import com.kotlinfoundation.koko.subscription.api.SubscriptionProvider
import com.kotlinfoundation.koko.subscription.api.SubscriptionProviderFactory
import com.kotlinfoundation.koko.subscription.api.SubscriptionProviderUi
import com.kotlinfoundation.koko.util.ApplicationScope
import com.kotlinfoundation.koko.util.Constants
import com.kotlinfoundation.koko.util.defaultAsyncDispatcher
import com.kotlinfoundation.koko.util.extensions.nowEpochMillis
import com.kotlinfoundation.koko.util.isAndroid
import com.kotlinfoundation.koko.util.logging.Logger
import com.kotlinfoundation.koko.util.logging.NapierLogger
import com.kotlinfoundation.koko.util.logging.TelegramLogger
import com.kotlinfoundation.koko.util.platformModule
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

/**
 * Koin module graph for the shared app. [AppInitializer] loads [appModules] at startup.
 * Layered by concern: [domainModule] (pure, empty), [dataModule] (infra + repositories),
 * [presentationModule] (ViewModels), plus the per-target expect/actual `platformModule`.
 */

// Empty by design — the domain layer is pure (models/exceptions), nothing to inject.
private val domainModule = module {
}

// Infrastructure + repositories: scopes/dispatchers, preferences, network, auth & subscription
// providers (selected via Constants), repositories, loggers, AI provider, and the credit system.
private val dataModule = module {
    singleOf(::ApplicationScope)
    factory { defaultAsyncDispatcher } bind CoroutineContext::class
    factory { BackgroundExecutor.IO } bind BackgroundExecutor::class

    // Preferences Source. The DataStore instance stays out of the Koin graph on
    // purpose — generic types erase to `DataStore`, so a second DataStore<T>
    // registered later would silently collide with this one.
    single { UserPreferencesImpl(get<PreferencesDataStoreProvider>().providePreferencesDataStore()) } bind UserPreferences::class

    // Remote source
    single { HttpClientFactory.default() }
    single(named("aiDirectClient")) { HttpClientFactory.noAuth() }
    single { AiTransport(proxyClient = get(), directClient = get(named("aiDirectClient"))) }
    single { TemporaryFileUploadApiService(HttpClientFactory.fileUpload()) }

    factoryOf(::ApiService)
    factoryOf(::OpenAiApiService)
    factoryOf(::ReplicateApiService)

    // Subscription Provider. When no real SDK key is set (isSubscriptionMockActive), swap in the
    // MockSubscriptionProvider so the paywall/purchase/unlock flow is explorable with zero keys.
    // Auto-reverts to the real provider (Adapty/RevenueCat) the moment a key is configured.
    factory { AppConfiguration.subscriptionProviderFactory } bind SubscriptionProviderFactory::class
    single {
        if (isSubscriptionMockActive()) {
            val userPreferences = get<UserPreferences>()
            MockSubscriptionProvider(
                readPremiumPurchased = {
                    userPreferences.getBoolean(MockSubscriptionProvider.KEY_MOCK_PREMIUM_PURCHASED, false)
                },
                writePremiumPurchased = {
                    userPreferences.putBoolean(MockSubscriptionProvider.KEY_MOCK_PREMIUM_PURCHASED, it)
                },
                premiumAccessId = Constants.PAYWALL_PREMIUM_ACCESS,
                creditPackPrefix = Constants.CREDIT_PACK_PRODUCT_ID_PREFIX,
                creditPackPlacementId = Constants.PAYWALL_PLACEMENT_CREDITS_PACK,
                currentTimeMillis = ::nowEpochMillis,
            )
        } else {
            get<SubscriptionProviderFactory>().createProvider()
        }
    } bind SubscriptionProvider::class
    factory {
        if (isSubscriptionMockActive()) {
            NoOpSubscriptionProviderUi
        } else {
            get<SubscriptionProviderFactory>().createProviderUi()
        }
    } bind SubscriptionProviderUi::class

    // Repositories
    single { UserRepository(get(), get(), get(), get()) }
    single { SubscriptionRepository(get(), get(), get(), get()) }
    single { GenerationRepository(get(), get(), get(), get(), get(), get(), get()) }

    // Loggers
    factory { TelegramLogger(get(), get(), get()) } bind Logger::class
    factory { NapierLogger() } bind Logger::class

//    factory<AiGenerationProvider> { OpenAiImageGenerationProvider(get(), get()) }
    factory<AiGenerationProvider> { ReplicateGenerationProvider(get(), get()) }

    initializeCreditSystem()
}

// ViewModels, scoped per NavEntry. PaywallViewModel takes a placementId param.
private val presentationModule = module {
    viewModelOf(::OnBoardingViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::GalleryViewModel)
    viewModelOf(::ProfileViewModel)
    viewModel { (placementId: String?) ->
        PaywallViewModel(
            placementId = placementId,
            subscriptionRepository = get(),
            creditRepository = get(),
            userRepository = get(),
            featureFlagManager = get(),
        )
    }
    viewModelOf(::AccountViewModel)
    viewModelOf(::SubscriptionsViewModel)
    viewModelOf(::GenerationResultViewModel)
    viewModelOf(::CreditBalanceViewModel)

    // Add new view models below — generate_screen.sh inserts here.
}

private fun Module.initializeCreditSystem() {
    single {
        val userPreferences = get<UserPreferences>()
        val subscriptionRepository = get<SubscriptionRepository>()
        val appCreditSystemConfig = creditSystemConfig {
            oneTimeBonus("welcome_bonus_credit", 1)
//            oneTimeBonus(
//                id = "referral_bonus",
//                amount = 1,
//                condition = {
//                    userPreferences.getBoolean(UserPreferences.KEY_REFERRAL_COMPLETED)
//                }
//            )
//            recurringWeekly(
//                id = "free_plan_weekly",
//                amount = 2,
//                condition = {
//                    !subscriptionRepository.hasPremiumAccess()
//                }
//            )

            recurringWeekly(
                id = "premium_plan_weekly", // Gives all premium users 10 credits per week
                amount = 10,
                condition = {
                    subscriptionRepository.hasPremiumAccess()
                },
            )
        }

        CreditRepository(appCreditSystemConfig, get(), get(), get(), get())
    }
}

/**
 * True when the active-platform subscription SDK key is still a placeholder, i.e. no real
 * Adapty/RevenueCat account is wired yet. While true the app runs [MockSubscriptionProvider] so the
 * whole paywall → purchase → unlock flow is explorable with zero keys. Auto-off once a real key is set.
 */
private fun isSubscriptionMockActive(): Boolean {
    val key =
        if (isAndroid) {
            BuildConfig.SUBSCRIPTION_PROVIDER_ANDROID_API_KEY
        } else {
            BuildConfig.SUBSCRIPTION_PROVIDER_IOS_API_KEY
        }
    return key.isBlank() || key == MockSubscriptionProvider.PLACEHOLDER_KEY
}

// All Koin modules loaded at startup. platformModule is the expect/actual per-target module.
internal val appModules: List<Module> get() = platformModule + domainModule + dataModule + presentationModule
