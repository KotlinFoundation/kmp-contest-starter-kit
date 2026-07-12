package com.kotlinfoundation.koko.presentation.components

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.kotlinfoundation.koko.designsystem.components.AppleSignInButton
import com.kotlinfoundation.koko.designsystem.components.GoogleSignInButton
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_continue_with_apple
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_continue_with_google
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_sign_in_with_apple
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_sign_in_with_google
import com.kotlinfoundation.koko.domain.exceptions.UserAlreadyExistsException
import com.kotlinfoundation.koko.domain.model.AuthProvider
import com.kotlinfoundation.koko.util.logging.AppLogger
import com.mmk.kmpauth.firebase.apple.AppleButtonUiContainer
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException

@androidx.compose.runtime.Composable
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
            // Google Sign-In Button and authentication with Firebase
            GoogleButtonUiContainerFirebase(
                linkAccount = linkAccount,
                onResult = { firebaseUserResult ->
                    AppLogger.d("GoogleSignIn with Firebase Result: $firebaseUserResult")
                    firebaseUserResult.handleFirebaseAuthResult(onResult)
                },
            ) {
                LaunchedEffect(Unit) { if (autoClickEnabled) this@GoogleButtonUiContainerFirebase.onClick() }
                GoogleSignInButton(
                    height = height,
                    textRes = if (linkAccount) UiRes.string.btn_continue_with_google else UiRes.string.btn_sign_in_with_google,
                    shape = shape,
                ) { this.onClick() }
            }
        }

        AuthProvider.APPLE -> {
            // Apple Sign-In Button and authentication with Firebase
            AppleButtonUiContainer(linkAccount = linkAccount, onResult = { firebaseUserResult ->
                AppLogger.d("AppleSignIn with Firebase Result: $firebaseUserResult")
                firebaseUserResult.handleFirebaseAuthResult(onResult)
            }) {
                LaunchedEffect(Unit) { if (autoClickEnabled) this@AppleButtonUiContainer.onClick() }
                AppleSignInButton(
                    height = height,
                    textRes = if (linkAccount) UiRes.string.btn_continue_with_apple else UiRes.string.btn_sign_in_with_apple,
                    shape = shape,
                ) { this.onClick() }
            }
        }
    }
}

private fun <T> Result<T>.handleFirebaseAuthResult(onResult: (Result<Unit>) -> Unit) = this.onSuccess {
    onResult(Result.success(Unit))
}.onFailure { error ->
    val errorMessage = error.message ?: ""

    // TODO check if it is already existing or collision error. Checking with empty message is not reliable
    val isUserAlreadyRegistered =
        error is FirebaseAuthUserCollisionException || errorMessage.isEmpty()

    if (isUserAlreadyRegistered) {
        onResult(Result.failure(UserAlreadyExistsException()))
    } else {
        onResult(Result.failure(error))
    }
}
