package com.kotlinfoundation.koko.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.kotlinfoundation.koko.data.source.featureflag.FeatureFlagManager
import com.kotlinfoundation.koko.designsystem.components.bottomnav.BottomNavItem
import com.kotlinfoundation.koko.designsystem.components.bottomnav.BottomNavigationBar
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_gallery
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_home
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_profile
import com.kotlinfoundation.koko.generated.resources.Res
import com.kotlinfoundation.koko.generated.resources.bottom_nav_label_gallery
import com.kotlinfoundation.koko.generated.resources.bottom_nav_label_home
import com.kotlinfoundation.koko.generated.resources.bottom_nav_label_profile
import com.kotlinfoundation.koko.presentation.screens.account.AccountScreen
import com.kotlinfoundation.koko.presentation.screens.account.AccountViewModel
import com.kotlinfoundation.koko.presentation.screens.creditbalance.CreditBalanceScreen
import com.kotlinfoundation.koko.presentation.screens.creditbalance.CreditBalanceViewModel
import com.kotlinfoundation.koko.presentation.screens.gallery.GalleryScreen
import com.kotlinfoundation.koko.presentation.screens.gallery.GalleryViewModel
import com.kotlinfoundation.koko.presentation.screens.generationresult.GenerationResultScreen
import com.kotlinfoundation.koko.presentation.screens.generationresult.GenerationResultViewModel
import com.kotlinfoundation.koko.presentation.screens.helpandsupport.HelpAndSupportScreen
import com.kotlinfoundation.koko.presentation.screens.home.HomeScreen
import com.kotlinfoundation.koko.presentation.screens.home.HomeViewModel
import com.kotlinfoundation.koko.presentation.screens.onboarding.OnBoardingScreen
import com.kotlinfoundation.koko.presentation.screens.onboarding.OnBoardingScreenStyle
import com.kotlinfoundation.koko.presentation.screens.onboarding.OnBoardingViewModel
import com.kotlinfoundation.koko.presentation.screens.paywall.PaywallScreen
import com.kotlinfoundation.koko.presentation.screens.paywall.PaywallViewModel
import com.kotlinfoundation.koko.presentation.screens.paywall.remotepaywall.RemotePaywallScreen
import com.kotlinfoundation.koko.presentation.screens.profile.ProfileScreen
import com.kotlinfoundation.koko.presentation.screens.profile.ProfileViewModel
import com.kotlinfoundation.koko.presentation.screens.signin.SignInScreen
import com.kotlinfoundation.koko.presentation.screens.subscriptions.SubscriptionsScreen
import com.kotlinfoundation.koko.presentation.screens.subscriptions.SubscriptionsViewModel
import com.kotlinfoundation.koko.root.AppConfiguration
import com.kotlinfoundation.koko.util.Constants
import com.kotlinfoundation.koko.util.extensions.isKeyboardOpen
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Access the [Navigator] from any screen without threading it through every composable. */
val LocalNavigator = compositionLocalOf<Navigator> { error("No Navigator provided") }

// Tab switches should be instant — no enter/exit animation (unlike pushes, which animate).
private val noAnimationTransition = EnterTransition.None togetherWith ExitTransition.None
private val noAnimationMetadata =
    NavDisplay.transitionSpec { noAnimationTransition } +
        NavDisplay.popTransitionSpec { noAnimationTransition } +
        NavDisplay.predictivePopTransitionSpec { noAnimationTransition }

// Bottom-nav tabs, in display order. The keys are the routes; the values render the nav bar.
private val TOP_LEVEL_ROUTES: Map<TopLevelScreenRoute, BottomNavItem> = linkedMapOf(
    HomeScreenRoute to BottomNavItem(
        label = Res.string.bottom_nav_label_home,
        icon = UiRes.drawable.ic_home,
    ),
    GalleryScreenRoute to BottomNavItem(
        label = Res.string.bottom_nav_label_gallery,
        icon = UiRes.drawable.ic_gallery,
    ),
    AccountScreenRoute to BottomNavItem(
        label = Res.string.bottom_nav_label_profile,
        icon = UiRes.drawable.ic_profile,
    ),
)

/**
 * The single nav host: owns the [Navigator], renders the current back stack via [NavDisplay],
 * and shows the bottom nav bar only at a tab root (hidden on pushed screens and when the keyboard
 * is open). Starts on onboarding.
 */
@Composable
fun AppNavigation() {
    val navigationState = rememberNavigationState(
        startRoute = OnBoardingScreenRoute,
        topLevelRoutes = TOP_LEVEL_ROUTES.keys,
        primaryTopLevelRoute = HomeScreenRoute,
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }

    val entryProvider = remember(navigator) { entryProvider { screens(navigator) } }

    val isAtRoot = navigationState.currentBackstack.size <= 1
    val activeTopLevel = navigationState.topLevelRoute
    val selectedTopLevelIndex = TOP_LEVEL_ROUTES.keys.indexOf(activeTopLevel).coerceAtLeast(0)
    val bottomNavItems = remember { TOP_LEVEL_ROUTES.values.toList() }
    val isBottomNavVisible = isAtRoot && activeTopLevel != null && !isKeyboardOpen()

    CompositionLocalProvider(LocalNavigator provides navigator) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                NavDisplay(
                    entries = navigationState.toDecoratedEntries(entryProvider),
                    onBack = { navigator.goBack() },
                )
            }
            AnimatedVisibility(isBottomNavVisible, enter = fadeIn(snap()), exit = fadeOut(snap())) {
                BottomNavigationBar(
                    modifier = Modifier.fillMaxWidth(),
                    items = bottomNavItems,
                    selectedIndex = selectedTopLevelIndex,
                ) { clickedItem ->
                    val targetRoute = TOP_LEVEL_ROUTES.entries
                        .firstOrNull { it.value === clickedItem }?.key ?: return@BottomNavigationBar
                    navigator.navigate(targetRoute)
                }
            }
        }
    }
}

// Maps each route to its screen. Every `entry<X>` wires the screen's navigation callbacks to the
// Navigator; ViewModels are obtained per-entry via koinViewModel<…>().
private fun EntryProviderScope<ScreenRoute>.screens(navigator: Navigator) {
    // ── Top-level (no animation when switching tabs) ────────────────────────────

    entry<HomeScreenRoute>(metadata = noAnimationMetadata) {
        val viewModel = koinViewModel<HomeViewModel>()
        HomeScreen(
            viewModel = viewModel,
            onPremiumRequired = { navigator.navigate(PaywallScreenRoute()) },
            onMoreCreditsNeeded = { navigator.navigate(CreditBalanceScreenRoute) },
            onAuthRequired = { navigator.navigate(SignInScreenRoute()) },
            onGenerationResult = { generationOutput ->
                navigator.navigate(GenerationResultScreenRoute(id = generationOutput.id))
            },
        )
    }

    entry<GalleryScreenRoute>(metadata = noAnimationMetadata) {
        val viewModel = koinViewModel<GalleryViewModel>()
        GalleryScreen(
            viewModel = viewModel,
            onNavigateToResult = { id -> navigator.navigate(GenerationResultScreenRoute(id = id)) },
            onNavigateToHome = { navigator.navigate(HomeScreenRoute) },
        )
    }

    entry<AccountScreenRoute>(metadata = noAnimationMetadata) {
        val viewModel = koinViewModel<AccountViewModel>()
        AccountScreen(
            viewModel = viewModel,
            onNavigateHelpAndSupport = { navigator.navigate(HelpAndSupportScreenRoute) },
            onNavigatePaywall = { navigator.navigate(PaywallScreenRoute()) },
            onNavigateSignIn = { navigator.navigate(SignInScreenRoute()) },
            onNavigateProfile = { navigator.navigate(ProfileScreenRoute) },
            onNavigateSubscriptions = { navigator.navigate(SubscriptionsScreenRoute) },
        )
    }

    // ── Pushed destinations ─────────────────────────────────────────────────────

    entry<OnBoardingScreenRoute> {
        val viewModel = koinViewModel<OnBoardingViewModel>()
        OnBoardingScreen(
            style = OnBoardingScreenStyle.STYLE1,
            viewModel = viewModel,
            onOnBoardingFinished = { isNewUser ->
                navigator.set(HomeScreenRoute)
                if (isNewUser && AppConfiguration.PREMIUM_FEATURES_ENABLED) {
                    navigator.navigate(
                        PaywallScreenRoute(placementId = Constants.PAYWALL_PLACEMENT_ONBOARDING),
                    )
                }
            },
        )
    }

    entry<ProfileScreenRoute> {
        val viewModel = koinViewModel<ProfileViewModel>()
        ProfileScreen(
            viewModel = viewModel,
            onSignInRequired = { navigator.replace(SignInScreenRoute()) },
            onNavigateToBack = { navigator.goBack() },
        )
    }

    entry<SignInScreenRoute> { key ->
        SignInScreen(
            isSignIn = key.isSignIn,
            onSuccessfulSignIn = { navigator.goBack() },
            onNavigateBack = { navigator.goBack() },
        )
    }

    entry<SubscriptionsScreenRoute> {
        val viewModel = koinViewModel<SubscriptionsViewModel>()
        SubscriptionsScreen(
            viewModel = viewModel,
            onClickBack = { navigator.goBack() },
            onNavigatePaywall = { navigator.navigate(PaywallScreenRoute()) },
        )
    }

    entry<HelpAndSupportScreenRoute> {
        HelpAndSupportScreen(onNavigateBack = { navigator.goBack() })
    }

    entry<CreditBalanceScreenRoute> {
        val viewModel = koinViewModel<CreditBalanceViewModel>()
        CreditBalanceScreen(
            viewModel = viewModel,
            onPurchaseRequired = { placementId ->
                navigator.navigate(PaywallScreenRoute(placementId = placementId))
            },
            onClickBack = { navigator.goBack() },
        )
    }

    entry<GenerationResultScreenRoute> { key ->
        val viewModel =
            koinViewModel<GenerationResultViewModel>(parameters = { parametersOf(key.id) })
        GenerationResultScreen(
            viewModel = viewModel,
            onNavigateToBack = { navigator.goBack() },
        )
    }

    // SHOW_REMOTE_PAYWALL flag picks the provider's hosted paywall UI vs. the app's custom one.
    entry<PaywallScreenRoute> { key ->
        val featureFlagManager = koinInject<FeatureFlagManager>()
        val viewModel = koinViewModel<PaywallViewModel>(
            parameters = { parametersOf(key.placementId) },
        )
        if (featureFlagManager.getBoolean(FeatureFlagManager.Keys.SHOW_REMOTE_PAYWALL)) {
            Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                RemotePaywallScreen(
                    viewModel = viewModel,
                    onDismiss = {
                        viewModel.onPaywallDismissActionHandled()
                        navigator.goBack()
                    },
                    onSignInRequired = { navigator.navigate(SignInScreenRoute()) },
                )
            }
        } else {
            PaywallScreen(
                viewModel = viewModel,
                onDismiss = {
                    viewModel.onPaywallDismissActionHandled()
                    navigator.goBack()
                },
                onSignInRequired = { navigator.navigate(SignInScreenRoute()) },
            )
        }
    }

    // Add new screen entries below — generate_screen.sh inserts here.
}
