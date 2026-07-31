package com.kotlinfoundation.koko.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.components.AppleSignInButton
import com.kotlinfoundation.koko.designsystem.components.GoogleSignInButton
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_continue_with_apple
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_continue_with_google
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_sign_in_with_apple
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_sign_in_with_google
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.domain.model.AuthProvider
import com.kotlinfoundation.koko.util.Platform
import com.kotlinfoundation.koko.util.getPlatform
import com.kotlinfoundation.koko.util.logging.AppLogger
import com.mmk.kmpauth.apple.rememberAppleAuthState
import com.mmk.kmpauth.core.auth.KMPAuthUser
import com.mmk.kmpauth.google.rememberGoogleAuthState

@Composable
fun AuthUIHelperButtons(
    modifier: Modifier = Modifier,
    authProviders: List<AuthProvider> = AuthProvider.entries,
    shape: Shape = CircleShape,
    height: Dp = 56.dp,
    spaceBetweenButtons: Dp = AppTheme.spacing.groupedVerticalElementSpacing,
    autoClickEnabledIfOneProviderExists: Boolean = true,
    linkAccount: Boolean = false,
    onResult: (Result<Unit>) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spaceBetweenButtons)) {
        val isExistOnlyOneAuthProvider by remember { mutableStateOf(authProviders.size == 1) }
        val updatedOnResult by rememberUpdatedState(onResult)

        authProviders.forEach { authProvider ->
            AuthUiButton(
                authProvider = authProvider,
                shape = shape,
                height = height,
                linkAccount = linkAccount,
                autoClickEnabled = isExistOnlyOneAuthProvider && autoClickEnabledIfOneProviderExists,
                onResult = updatedOnResult,
            )
        }
    }
}

/**
 * Google / Apple sign-in button backed by [rememberGoogleAuthState] / [rememberAppleAuthState] — the
 * credential is exchanged for a session via the registered KMPAuth backend (Firebase), on every
 * platform. `linkAccount = true` links the credential to the current (anonymous) user.
 */
@Composable
fun AuthUiButton(
    authProvider: AuthProvider,
    shape: Shape,
    height: Dp,
    linkAccount: Boolean,
    autoClickEnabled: Boolean,
    onResult: (Result<Unit>) -> Unit,
) {
    when (authProvider) {
        AuthProvider.GOOGLE -> {
            val googleAuthState = rememberGoogleAuthState(linkAccount = linkAccount) { it.handle(onResult) }
            LaunchedEffect(Unit) { if (autoClickEnabled) googleAuthState.launch() }
            GoogleSignInButton(
                height = height,
                textRes = if (linkAccount) UiRes.string.btn_continue_with_google else UiRes.string.btn_sign_in_with_google,
                shape = shape,
            ) { googleAuthState.launch() }
        }

        // Apple sign-in only on mobile — desktop/web aren't wired for it yet.
        AuthProvider.APPLE -> if (getPlatform() == Platform.Android || getPlatform() == Platform.Ios) {
            val appleAuthState = rememberAppleAuthState(linkAccount = linkAccount) { it.handle(onResult) }
            LaunchedEffect(Unit) { if (autoClickEnabled) appleAuthState.launch() }
            AppleSignInButton(
                height = height,
                textRes = if (linkAccount) UiRes.string.btn_continue_with_apple else UiRes.string.btn_sign_in_with_apple,
                shape = shape,
            ) { appleAuthState.launch() }
        }
    }
}

private fun Result<KMPAuthUser>.handle(onResult: (Result<Unit>) -> Unit) {
    AppLogger.d("Auth result: $this")
    onSuccess { onResult(Result.success(Unit)) }.onFailure { onResult(Result.failure(it)) }
}
