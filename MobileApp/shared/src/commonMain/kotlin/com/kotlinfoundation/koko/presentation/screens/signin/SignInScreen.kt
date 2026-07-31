package com.kotlinfoundation.koko.presentation.screens.signin

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.data.repository.UserRepository
import com.kotlinfoundation.koko.designsystem.components.AgreePrivacyPolicyTermsConditionsText
import com.kotlinfoundation.koko.designsystem.components.AppButton
import com.kotlinfoundation.koko.designsystem.components.ButtonStyle
import com.kotlinfoundation.koko.designsystem.components.LogoImage
import com.kotlinfoundation.koko.designsystem.components.ScreenWithToolbar
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_back
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.generated.resources.Res
import com.kotlinfoundation.koko.generated.resources.auth_continue_as_guest_failed
import com.kotlinfoundation.koko.generated.resources.auth_switch_to_sign_in
import com.kotlinfoundation.koko.generated.resources.auth_switch_to_sign_in_action
import com.kotlinfoundation.koko.generated.resources.auth_switch_to_sign_up
import com.kotlinfoundation.koko.generated.resources.auth_switch_to_sign_up_action
import com.kotlinfoundation.koko.generated.resources.auth_user_already_exists_message
import com.kotlinfoundation.koko.generated.resources.btn_continue_as_guest
import com.kotlinfoundation.koko.generated.resources.sign_in_to
import com.kotlinfoundation.koko.generated.resources.sign_up_to
import com.kotlinfoundation.koko.generated.resources.title_sign_in
import com.kotlinfoundation.koko.generated.resources.title_sign_up
import com.kotlinfoundation.koko.generated.resources.txt_main_action_to_sign_in
import com.kotlinfoundation.koko.presentation.components.AuthUIHelperButtons
import com.kotlinfoundation.koko.root.AppConfiguration
import com.kotlinfoundation.koko.root.AppGlobalUiState
import com.kotlinfoundation.koko.util.UiMessage
import com.kotlinfoundation.koko.util.logging.AppLogger
import com.mmk.kmpauth.core.auth.KMPAuthUserCollisionException
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun SignInScreen(
    isSignIn: Boolean,
    modifier: Modifier = Modifier,
    onSuccessfulSignIn: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val userRepository = koinInject<UserRepository>()

    SignInScreen(
        isSignIn = isSignIn,
        modifier = modifier,
        onSuccessfulSignIn = onSuccessfulSignIn,
        onNavigateBack = onNavigateBack,
        onOauthSigned = userRepository::onSuccessfulOauthSign,
        continueAsGuest = userRepository::continueAsGuest,
    )
}

/**
 * Pure overload — the two account actions come in as lambdas instead of injecting
 * [UserRepository], so this renders in `@Preview` and `runComposeUiTest` with no Koin.
 * The ViewModel-less overload above wires the real repository.
 */
@Composable
fun SignInScreen(
    isSignIn: Boolean,
    modifier: Modifier = Modifier,
    onSuccessfulSignIn: () -> Unit,
    onNavigateBack: () -> Unit,
    onOauthSigned: () -> Unit,
    continueAsGuest: suspend () -> Result<Unit>,
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(true) {
        scrollState.animateScrollTo(scrollState.maxValue, tween(500))
    }
    val coroutineScope = rememberCoroutineScope()
    var signInMode by remember { mutableStateOf(isSignIn) }
    var isGuestLoading by remember { mutableStateOf(false) }

    ScreenWithToolbar(
        modifier = modifier.fillMaxSize().background(AppTheme.colors.background),
        title = stringResource(if (signInMode) Res.string.title_sign_in else Res.string.title_sign_up),
        includeBottomInsets = true,
        // Scrolls below instead: BoxWithConstraints must sit outside the scroll to read a real height.
        isScrollableContent = false,
        navigationIcon = UiRes.drawable.ic_back,
        onNavigationIconClick = { onNavigateBack() },
    ) {
        BoxWithConstraints {
            // Not Spacer(weight(1f)) — verticalScroll gives infinite height, so weights resolve to 0.
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .heightIn(min = maxHeight),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LogoImage(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)
                            .padding(AppTheme.spacing.defaultSpacing),
                    )
                    TitleText(
                        modifier = Modifier.padding(top = AppTheme.spacing.largeSpacing),
                        isSignIn = signInMode,
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Google/Apple buttons only when social login is enabled; otherwise anonymous-only.
                    if (AppConfiguration.AUTH_SOCIAL_LOGIN_ENABLED) {
                        AuthUIHelperButtons(
                            linkAccount = signInMode.not(),
                            modifier = Modifier.padding(top = AppTheme.spacing.largeSpacing).fillMaxWidth(),
                        ) { result ->
                            result.onSuccess {
                                AppLogger.d("Successful sign in")
                                onOauthSigned()
                                onSuccessfulSignIn()
                            }.onFailure { error ->
                                AppLogger.e("Error occurred while signing in, $error")
                                if (error is KMPAuthUserCollisionException) {
                                    signInMode = true
                                    coroutineScope.launch {
                                        AppGlobalUiState.showUiMessage(
                                            UiMessage.Resource(Res.string.auth_user_already_exists_message),
                                        )
                                    }
                                } else {
                                    coroutineScope.launch {
                                        AppGlobalUiState.showUiMessage(UiMessage.Message(error.message))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(AppTheme.spacing.defaultSpacing))
                    }

                    AppButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(Res.string.btn_continue_as_guest),
                        // Secondary to Google/Apple when they're shown; the only way in when they're not.
                        style = if (AppConfiguration.AUTH_SOCIAL_LOGIN_ENABLED) ButtonStyle.TEXT else ButtonStyle.PRIMARY,
                        isLoading = isGuestLoading,
                        onClick = {
                            isGuestLoading = true
                            coroutineScope.launch {
                                continueAsGuest()
                                    .onSuccess { onSuccessfulSignIn() }
                                    .onFailure { error ->
                                        AppLogger.e("Continue as guest failed: ${error.message}")
                                        AppGlobalUiState.showUiMessage(
                                            UiMessage.Resource(Res.string.auth_continue_as_guest_failed),
                                        )
                                    }
                                isGuestLoading = false
                            }
                        },
                    )

                    AuthModeToggle(
                        modifier = Modifier.padding(top = AppTheme.spacing.defaultSpacing).fillMaxWidth(),
                        isSignIn = signInMode,
                        onToggle = { signInMode = !signInMode },
                    )

                    AgreePrivacyPolicyTermsConditionsText(
                        modifier = Modifier.padding(top = AppTheme.spacing.largeSpacing).fillMaxWidth(),
                        privacyPolicyUrl = AppConfiguration.URL_PRIVACY_POLICY,
                        termsConditionsUrl = AppConfiguration.URL_TERMS_CONDITIONS,
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthModeToggle(
    isSignIn: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val promptRes =
        if (isSignIn) Res.string.auth_switch_to_sign_up else Res.string.auth_switch_to_sign_in
    val actionRes =
        if (isSignIn) Res.string.auth_switch_to_sign_up_action else Res.string.auth_switch_to_sign_in_action

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${stringResource(promptRes)} ",
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.text.secondary,
        )
        Text(
            text = stringResource(actionRes),
            style = AppTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.primary,
            modifier = Modifier.clickable(onClick = onToggle),
        )
    }
}

@Composable
private fun TitleText(modifier: Modifier, isSignIn: Boolean) {
    val titleRes = if (isSignIn) Res.string.sign_in_to else Res.string.sign_up_to
    val annotatedString = buildAnnotatedString {
        append(stringResource(titleRes))
        appendLine()
        withStyle(
            style = SpanStyle(
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.SemiBold,
            ),
        ) {
            append(stringResource(Res.string.txt_main_action_to_sign_in))
        }
    }
    Text(
        modifier = modifier,
        text = annotatedString,
        color = AppTheme.colors.text.primary,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Medium,
        style = AppTheme.typography.h3,
    )
}

@Preview
@Composable
private fun SignInScreenSignInPreview() {
    AppTheme {
        SignInScreen(
            isSignIn = true,
            onSuccessfulSignIn = {},
            onNavigateBack = {},
            onOauthSigned = {},
            continueAsGuest = { Result.success(Unit) },
        )
    }
}

@Preview
@Composable
private fun SignInScreenSignUpPreview() {
    AppTheme {
        SignInScreen(
            isSignIn = false,
            onSuccessfulSignIn = {},
            onNavigateBack = {},
            onOauthSigned = {},
            continueAsGuest = { Result.success(Unit) },
        )
    }
}
