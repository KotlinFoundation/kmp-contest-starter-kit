package com.kotlinfoundation.kmpstarterkit.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.kotlinfoundation.kmpstarterkit.designsystem.components.LoadingProgress
import com.kotlinfoundation.kmpstarterkit.designsystem.components.LoadingProgressMode
import com.kotlinfoundation.kmpstarterkit.designsystem.components.ScreenWithToolbar
import com.kotlinfoundation.kmpstarterkit.designsystem.components.SettingItemListContainer
import com.kotlinfoundation.kmpstarterkit.designsystem.components.SettingsItemUiState
import com.kotlinfoundation.kmpstarterkit.designsystem.components.UserInput
import com.kotlinfoundation.kmpstarterkit.designsystem.components.modals.AppDialog
import com.kotlinfoundation.kmpstarterkit.designsystem.components.modals.DeleteUserConfirmation
import com.kotlinfoundation.kmpstarterkit.designsystem.components.modals.DialogType
import com.kotlinfoundation.kmpstarterkit.designsystem.generated.resources.UiRes
import com.kotlinfoundation.kmpstarterkit.designsystem.generated.resources.btn_delete_account
import com.kotlinfoundation.kmpstarterkit.designsystem.generated.resources.ic_back
import com.kotlinfoundation.kmpstarterkit.designsystem.generated.resources.ic_delete
import com.kotlinfoundation.kmpstarterkit.designsystem.generated.resources.ic_profile_img_placeholder
import com.kotlinfoundation.kmpstarterkit.designsystem.theme.AppTheme
import com.kotlinfoundation.kmpstarterkit.domain.model.User
import com.kotlinfoundation.kmpstarterkit.generated.resources.Res
import com.kotlinfoundation.kmpstarterkit.generated.resources.title_screen_profile
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel,
    onSignInRequired: () -> Unit,
    onNavigateToBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.signInActionRequired) {
        LaunchedEffect(uiState) {
            onSignInRequired()
        }
    }
    if (uiState.deleteUserDialogShown) {
        DeleteUserConfirmation(
            onConfirm = viewModel::onConfirmDeleteAccount,
            onDismiss = viewModel::onDismissDeleteUserConfirmationDialog,
        )
    }

    if (uiState.errorMessage.isNullOrEmpty().not()) {
        AppDialog(
            type = DialogType.ERROR,
            text = uiState.errorMessage,
            onConfirm = { viewModel.onErrorMessageShown() },
        )
    }
    if (uiState.isLoading) {
        LoadingProgress(mode = LoadingProgressMode.FULLSCREEN)
    } else {
        val currentUser = uiState.user
        currentUser?.let {
            ScreenWithToolbar(
                modifier = modifier.fillMaxSize().background(AppTheme.colors.background),
                title = stringResource(Res.string.title_screen_profile),
                navigationIcon = UiRes.drawable.ic_back,
                onNavigationIconClick = onNavigateToBack,
                isScrollableContent = true,
                includeBottomInsets = true,
            ) {
                ProfileScreen(
                    modifier = Modifier.fillMaxSize(),
                    currentUser = it,
                    onUiEvent = viewModel::onUiEvent,
                )
            }
        }
    }
}

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    currentUser: User,
    onUiEvent: (ProfileScreenUiEvent) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Profile Picture
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(currentUser.photoUrl)
                .crossfade(true)
                .build(),
            placeholder = painterResource(UiRes.drawable.ic_profile_img_placeholder),
            error = painterResource(UiRes.drawable.ic_profile_img_placeholder),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(100.dp).clip(CircleShape),
        )

        // Full Name
        UserInputWithLabel(label = "Display Name") {
            UserInput(
                value = currentUser.displayName ?: "",
                readOnly = true,
                onValueChange = {},
            )
        }

        // Email
        UserInputWithLabel(label = "Email") {
            UserInput(
                value = currentUser.email ?: "",
                readOnly = true,
                onValueChange = {},
            )
        }

        SettingItemListContainer(
            onClick = { onUiEvent(ProfileScreenUiEvent.OnClickDeleteAccount) },
            itemTextStyle = AppTheme.typography.h5.copy(fontWeight = FontWeight.SemiBold),
            itemList = listOf(
                SettingsItemUiState(
                    textRes = UiRes.string.btn_delete_account,
                    startIcon = UiRes.drawable.ic_delete,
                    showEndIcon = false,
                    textIconColor = AppTheme.colors.status.error,
                ),
            ),
        )
    }
}

@Composable
fun UserInputWithLabel(
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    userInput: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
    ) {
        Text(
            text = label,
            style = AppTheme.typography.bodyExtraLarge,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.text.primary,
        )
        userInput()
    }
}
