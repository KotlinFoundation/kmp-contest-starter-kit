package com.kotlinfoundation.kmpstarterkit.presentation.navigation

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
import com.kotlinfoundation.kmpstarterkit.data.source.featureflag.FeatureFlagManager
import com.kotlinfoundation.kmpstarterkit.designsystem.components.bottomnav.BottomNavItem
import com.kotlinfoundation.kmpstarterkit.designsystem.components.bottomnav.BottomNavigationBar
import com.kotlinfoundation.kmpstarterkit.designsystem.generated.resources.UiRes
import com.kotlinfoundation.kmpstarterkit.designsystem.generated.resources.ic_gallery
import com.kotlinfoundation.kmpstarterkit.designsystem.generated.resources.ic_home
import com.kotlinfoundation.kmpstarterkit.designsystem.generated.resources.ic_profile
import com.kotlinfoundation.kmpstarterkit.generated.resources.Res
import com.kotlinfoundation.kmpstarterkit.generated.resources.bottom_nav_label_gallery
import com.kotlinfoundation.kmpstarterkit.generated.resources.bottom_nav_label_home
import com.kotlinfoundation.kmpstarterkit.generated.resources.bottom_nav_label_profile
import com.kotlinfoundation.kmpstarterkit.presentation.screens.account.AccountScreen
import com.kotlinfoundation.kmpstarterkit.presentation.screens.account.AccountUiStateHolder
import com.kotlinfoundation.kmpstarterkit.presentation.screens.creditbalance.CreditBalanceScreen
import com.kotlinfoundation.kmpstarterkit.presentation.screens.creditbalance.CreditBalanceUiStateHolder
import com.kotlinfoundation.kmpstarterkit.presentation.screens.gallery.GalleryScreen
import com.kotlinfoundation.kmpstarterkit.presentation.screens.gallery.GalleryUiStateHolder
import com.kotlinfoundation.kmpstarterkit.presentation.screens.generationresult.GenerationResultScreen
import com.kotlinfoundation.kmpstarterkit.presentation.screens.generationresult.GenerationResultUiStateHolder
import com.kotlinfoundation.kmpstarterkit.presentation.screens.helpandsupport.HelpAndSupportScreen
import com.kotlinfoundation.kmpstarterkit.presentation.screens.home.HomeScreen
import com.kotlinfoundation.kmpstarterkit.presentation.screens.home.HomeUiStateHolder
import com.kotlinfoundation.kmpstarterkit.presentation.screens.onboarding.OnBoardingScreen
import com.kotlinfoundation.kmpstarterkit.presentation.screens.onboarding.OnBoardingScreenStyle
import com.kotlinfoundation.kmpstarterkit.presentation.screens.onboarding.OnBoardingUiStateHolder
import com.kotlinfoundation.kmpstarterkit.presentation.screens.paywall.PaywallScreen
import com.kotlinfoundation.kmpstarterkit.presentation.screens.paywall.PaywallUiStateHolder
import com.kotlinfoundation.kmpstarterkit.presentation.screens.paywall.remotepaywall.RemotePaywallScreen
import com.kotlinfoundation.kmpstarterkit.presentation.screens.profile.ProfileScreen
import com.kotlinfoundation.kmpstarterkit.presentation.screens.profile.ProfileUiStateHolder
import com.kotlinfoundation.kmpstarterkit.presentation.screens.signin.SignInScreen
import com.kotlinfoundation.kmpstarterkit.presentation.screens.subscriptions.SubscriptionsScreen
import com.kotlinfoundation.kmpstarterkit.presentation.screens.subscriptions.SubscriptionsUiStateHolder
import com.kotlinfoundation.kmpstarterkit.util.Constants
import com.kotlinfoundation.kmpstarterkit.util.extensions.isKeyboardOpen
import com.kotlinfoundation.kmpstarterkit.util.uiStateHolder
import org.koin.compose.koinInject
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
// Navigator; ViewModels are obtained per-entry via uiStateHolder<…>().
private fun EntryProviderScope<ScreenRoute>.screens(navigator: Navigator) {
    // ── Top-level (no animation when switching tabs) ────────────────────────────

    entry<HomeScreenRoute>(metadata = noAnimationMetadata) {
        val holder = uiStateHolder<HomeUiStateHolder>()
        HomeScreen(
            uiStateHolder = holder,
            onPremiumRequired = { navigator.navigate(PaywallScreenRoute()) },
            onMoreCreditsNeeded = { navigator.navigate(CreditBalanceScreenRoute) },
            onAuthRequired = { navigator.navigate(SignInScreenRoute()) },
            onGenerationResult = { generationOutput ->
                navigator.navigate(GenerationResultScreenRoute(id = generationOutput.id))
            },
        )
    }

    entry<GalleryScreenRoute>(metadata = noAnimationMetadata) {
        val holder = uiStateHolder<GalleryUiStateHolder>()
        GalleryScreen(
            uiStateHolder = holder,
            onNavigateToResult = { id -> navigator.navigate(GenerationResultScreenRoute(id = id)) },
            onNavigateToHome = { navigator.navigate(HomeScreenRoute) },
        )
    }

    entry<AccountScreenRoute>(metadata = noAnimationMetadata) {
        val holder = uiStateHolder<AccountUiStateHolder>()
        AccountScreen(
            uiStateHolder = holder,
            onNavigateHelpAndSupport = { navigator.navigate(HelpAndSupportScreenRoute) },
            onNavigatePaywall = { navigator.navigate(PaywallScreenRoute()) },
            onNavigateSignIn = { navigator.navigate(SignInScreenRoute()) },
            onNavigateProfile = { navigator.navigate(ProfileScreenRoute) },
            onNavigateSubscriptions = { navigator.navigate(SubscriptionsScreenRoute) },
        )
    }

    // ── Pushed destinations ─────────────────────────────────────────────────────

    entry<OnBoardingScreenRoute> {
        val holder = uiStateHolder<OnBoardingUiStateHolder>()
        OnBoardingScreen(
            style = OnBoardingScreenStyle.STYLE1,
            uiStateHolder = holder,
            onNavigateMain = { navigator.goBack() },
            onNavigatePaywall = {
                navigator.navigate(
                    PaywallScreenRoute(placementId = Constants.PAYWALL_PLACEMENT_ONBOARDING),
                )
            },
        )
    }

    entry<ProfileScreenRoute> {
        val holder = uiStateHolder<ProfileUiStateHolder>()
        ProfileScreen(
            uiStateHolder = holder,
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
        val holder = uiStateHolder<SubscriptionsUiStateHolder>()
        SubscriptionsScreen(
            uiStateHolder = holder,
            onClickBack = { navigator.goBack() },
            onNavigatePaywall = { navigator.navigate(PaywallScreenRoute()) },
        )
    }

    entry<HelpAndSupportScreenRoute> {
        HelpAndSupportScreen(onNavigateBack = { navigator.goBack() })
    }

    entry<CreditBalanceScreenRoute> {
        val holder = uiStateHolder<CreditBalanceUiStateHolder>()
        CreditBalanceScreen(
            uiStateHolder = holder,
            onPurchaseRequired = { placementId ->
                navigator.navigate(PaywallScreenRoute(placementId = placementId))
            },
            onClickBack = { navigator.goBack() },
        )
    }

    entry<GenerationResultScreenRoute> { key ->
        val holder =
            uiStateHolder<GenerationResultUiStateHolder>(parameters = { parametersOf(key.id) })
        GenerationResultScreen(
            uiStateHolder = holder,
            onNavigateToBack = { navigator.goBack() },
        )
    }

    // SHOW_REMOTE_PAYWALL flag picks the provider's hosted paywall UI vs. the app's custom one.
    entry<PaywallScreenRoute> { key ->
        val featureFlagManager = koinInject<FeatureFlagManager>()
        val holder = uiStateHolder<PaywallUiStateHolder>(
            parameters = { parametersOf(key.placementId) },
        )
        if (featureFlagManager.getBoolean(FeatureFlagManager.Keys.SHOW_REMOTE_PAYWALL)) {
            Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                RemotePaywallScreen(
                    uiStateHolder = holder,
                    onDismiss = {
                        holder.onPaywallDismissActionHandled()
                        navigator.goBack()
                    },
                    onSignInRequired = { navigator.navigate(SignInScreenRoute()) },
                )
            }
        } else {
            PaywallScreen(
                uiStateHolder = holder,
                onDismiss = {
                    holder.onPaywallDismissActionHandled()
                    navigator.goBack()
                },
                onSignInRequired = { navigator.navigate(SignInScreenRoute()) },
            )
        }
    }

    // Add new screen entries below — generate_screen.sh inserts here.
}
