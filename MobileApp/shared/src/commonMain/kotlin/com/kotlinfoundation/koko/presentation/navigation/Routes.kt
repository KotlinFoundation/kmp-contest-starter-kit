package com.kotlinfoundation.koko.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Every navigation destination in the app.
 *
 * `@SerialName` values are the wire format on disk — keep them stable when
 * renaming routes.
 */
@Serializable
sealed interface ScreenRoute : NavKey

/** Marker for routes that root a top-level back stack (a tab in the bottom nav). */
@Serializable
sealed interface TopLevelScreenRoute : ScreenRoute

@Serializable
@SerialName("Home")
data object HomeScreenRoute : TopLevelScreenRoute

@Serializable
@SerialName("Gallery")
data object GalleryScreenRoute : TopLevelScreenRoute

@Serializable
@SerialName("Account")
data object AccountScreenRoute : TopLevelScreenRoute

@Serializable
@SerialName("OnBoarding")
data object OnBoardingScreenRoute : ScreenRoute

@Serializable
@SerialName("Profile")
data object ProfileScreenRoute : ScreenRoute

@Serializable
@SerialName("SignIn")
data class SignInScreenRoute(val isSignIn: Boolean = false) : ScreenRoute

@Serializable
@SerialName("Subscriptions")
data object SubscriptionsScreenRoute : ScreenRoute

@Serializable
@SerialName("HelpAndSupport")
data object HelpAndSupportScreenRoute : ScreenRoute

@Serializable
@SerialName("CreditBalance")
data object CreditBalanceScreenRoute : ScreenRoute

@Serializable
@SerialName("GenerationResult")
data class GenerationResultScreenRoute(val id: String) : ScreenRoute

@Serializable
@SerialName("Paywall")
data class PaywallScreenRoute(val placementId: String? = null) : ScreenRoute

// Add new routes below — generate_screen.sh inserts here.
