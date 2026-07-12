package com.kotlinfoundation.koko.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.kotlinfoundation.koko.designsystem.components.GoogleSignInButton
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_continue_with_google
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_sign_in_with_google
import com.kotlinfoundation.koko.domain.model.AuthProvider
import com.mmk.kmpauth.google.GoogleButtonUiContainer

@Composable
actual fun AuthUiButton(
    authProvider: AuthProvider,
    shape: Shape,
    height: Dp,
    linkAccount: Boolean,
    autoClickEnabled: Boolean,
    onResult: (Result<Unit>) -> Unit,
) {
    when (authProvider) {
        AuthProvider.GOOGLE -> {
            // Google Sign-In Button and authentication
            GoogleButtonUiContainer(onGoogleSignInResult = { googleUser ->
                if (googleUser != null) {
                    onResult(Result.success(Unit))
                } else {
                    onResult(Result.failure(Exception("Google sign in failed")))
                }
            }) {
                LaunchedEffect(Unit) { if (autoClickEnabled) this@GoogleButtonUiContainer.onClick() }
                GoogleSignInButton(
                    height = height,
                    textRes = if (linkAccount) UiRes.string.btn_continue_with_google else UiRes.string.btn_sign_in_with_google,
                    shape = shape,
                ) { this.onClick() }
            }
        }

        AuthProvider.APPLE -> {
            // Apple sign in is not supported in this platform
        }
    }
}
