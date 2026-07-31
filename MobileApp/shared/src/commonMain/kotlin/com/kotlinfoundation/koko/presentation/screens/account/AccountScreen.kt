package com.kotlinfoundation.koko.presentation.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.kotlinfoundation.koko.designsystem.components.AppCardContainer
import com.kotlinfoundation.koko.designsystem.components.ScreenWithToolbar
import com.kotlinfoundation.koko.designsystem.components.SettingItemListContainer
import com.kotlinfoundation.koko.designsystem.components.SmallTitle
import com.kotlinfoundation.koko.designsystem.components.modals.AppModalBottomSheet
import com.kotlinfoundation.koko.designsystem.components.premium.UpgradePremiumBanner
import com.kotlinfoundation.koko.designsystem.components.premium.UpgradePremiumBannerStyle
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_cancel
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_logout_confirm
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_arrow_right
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_copy_content
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_profile_img_placeholder
import com.kotlinfoundation.koko.designsystem.generated.resources.logout
import com.kotlinfoundation.koko.designsystem.generated.resources.text_logout_confirmation
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.domain.model.User
import com.kotlinfoundation.koko.generated.resources.Res
import com.kotlinfoundation.koko.generated.resources.help_and_support
import com.kotlinfoundation.koko.generated.resources.subscriptions
import com.kotlinfoundation.koko.generated.resources.title_screen_account
import com.kotlinfoundation.koko.generated.resources.title_sign_in
import com.kotlinfoundation.koko.root.AppConfiguration
import com.kotlinfoundation.koko.root.AppGlobalUiState
import com.kotlinfoundation.koko.util.UiMessage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel,
    onNavigateHelpAndSupport: () -> Unit,
    onNavigatePaywall: () -> Unit,
    onNavigateSignIn: () -> Unit,
    onNavigateProfile: () -> Unit,
    onNavigateSubscriptions: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLogoutDialogVisible) {
        LogoutModalBottomSheet(
            onConfirm = { viewModel.onUiEvent(AccountUiEvent.OnLogoutConfirmClick) },
            onDismiss = { viewModel.onUiEvent(AccountUiEvent.OnLogoutDialogDismiss) },
        )
    }

    AccountScreen(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        uiState = uiState,
        onUiEvent = {
            when (it) {
                is AccountUiEvent.OnSettingsItemClick -> {
                    when (it.item.textRes) {
                        Res.string.help_and_support -> onNavigateHelpAndSupport()
                        Res.string.subscriptions -> onNavigateSubscriptions()
                        else -> viewModel.onUiEvent(it)
                    }
                }

                is AccountUiEvent.OnClickUpgradePremium -> {
                    onNavigatePaywall()
                }

                AccountUiEvent.OnClickSignIn -> {
                    onNavigateSignIn()
                }

                AccountUiEvent.OnClickProfile -> {
                    onNavigateProfile()
                }

                else -> viewModel.onUiEvent(it)
            }
        },
    )
}

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    uiState: AccountUiState,
    onUiEvent: (AccountUiEvent) -> Unit,
) {
    ScreenWithToolbar(
        modifier = modifier,
        isScrollableContent = true,
        title = stringResource(Res.string.title_screen_account),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing)) {
            if (uiState.showUpgradePremiumBanner) {
                UpgradePremiumBanner(
                    style = UpgradePremiumBannerStyle.SMALL,
                    onClick = { onUiEvent(AccountUiEvent.OnClickUpgradePremium) },
                )
            }

            if (AppConfiguration.AUTH_SOCIAL_LOGIN_ENABLED || uiState.user?.id?.isNotEmpty() == true) {
                ProfileInfoBox(user = uiState.user, onClick = {
                    if (uiState.user == null) {
                        onUiEvent(AccountUiEvent.OnClickSignIn)
                    } else {
                        onUiEvent(AccountUiEvent.OnClickProfile)
                    }
                })
            }
            SettingItemListContainer(
                itemList = uiState.settingsItemList,
                onClick = { onUiEvent(AccountUiEvent.OnSettingsItemClick(it)) },
            )
        }
    }
}

@Composable
private fun ProfileInfoBox(user: User?, onClick: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    AppCardContainer(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            if (AppConfiguration.AUTH_SOCIAL_LOGIN_ENABLED) {
                onClick()
            } else {
                user?.id?.let {
                    clipboardManager.setText(AnnotatedString(it))
                    AppGlobalUiState.showUiMessage(UiMessage.Message("User ID is copied to clipboard"))
                }
            }
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.horizontalItemSpacing),
        ) {
            if (AppConfiguration.AUTH_SOCIAL_LOGIN_ENABLED) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(user?.photoUrl)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(UiRes.drawable.ic_profile_img_placeholder),
                    error = painterResource(UiRes.drawable.ic_profile_img_placeholder),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(60.dp).clip(CircleShape),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacingSmall),
                ) {
                    val displayName =
                        if (user == null) stringResource(Res.string.title_sign_in) else user.displayName

                    SmallTitle(text = displayName ?: "User Name")
                    user?.email?.let { email ->
                        Text(
                            email,
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.colors.text.secondary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = vectorResource(UiRes.drawable.ic_arrow_right),
                    contentDescription = null,
                    tint = AppTheme.colors.text.primary,
                )
            } else {
                Text(
                    modifier = Modifier.weight(1f),
                    text = "User ID: ${user?.id ?: ""}",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.primary,
                    fontWeight = FontWeight.Medium,
                )

                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = vectorResource(UiRes.drawable.ic_copy_content),
                    contentDescription = null,
                    tint = AppTheme.colors.text.primary,
                )
            }
        }
    }
}

@Composable
private fun LogoutModalBottomSheet(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppModalBottomSheet(
        title = stringResource(UiRes.string.logout),
        titleColor = AppTheme.colors.status.error,
        btnDismissText = stringResource(UiRes.string.btn_cancel),
        btnConfirmText = stringResource(UiRes.string.btn_logout_confirm),
        onConfirm = { onConfirm() },
        onDismiss = { onDismiss() },
        reverseButtonsOrder = true,
    ) {
        Text(
            text = stringResource(UiRes.string.text_logout_confirmation),
            textAlign = TextAlign.Center,
            color = AppTheme.colors.text.primary,
            style = AppTheme.typography.h5,
        )
    }
}

@Preview
@Composable
private fun AccountScreenPreview() {
    AppTheme {
        AccountScreen(uiState = AccountUiState(), onUiEvent = {})
    }
}
