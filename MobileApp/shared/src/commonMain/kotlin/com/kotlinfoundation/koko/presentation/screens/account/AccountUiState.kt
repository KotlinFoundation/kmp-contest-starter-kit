package com.kotlinfoundation.koko.presentation.screens.account

import com.kotlinfoundation.koko.designsystem.components.SettingsItemUiState
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_settings_item_logout
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_settings_item_subscriptions
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_settings_item_support_legal
import com.kotlinfoundation.koko.domain.model.User
import com.kotlinfoundation.koko.generated.resources.Res
import com.kotlinfoundation.koko.generated.resources.help_and_support
import com.kotlinfoundation.koko.generated.resources.logout
import com.kotlinfoundation.koko.generated.resources.subscriptions

data class AccountUiState(
    val settingsItemList: List<SettingsItemUiState> = listOf(
        SettingsItemUiState(
            startIcon = UiRes.drawable.ic_settings_item_subscriptions,
            textRes = Res.string.subscriptions,
        ),

        SettingsItemUiState(
            startIcon = UiRes.drawable.ic_settings_item_support_legal,
            textRes = Res.string.help_and_support,
        ),

        SettingsItemUiState(
            startIcon = UiRes.drawable.ic_settings_item_logout,
            textRes = Res.string.logout,
            showEndIcon = false,
        ),
    ),
    val user: User? = null,
    val isLogoutDialogVisible: Boolean = false,
    val showUpgradePremiumBanner: Boolean = false,
)

sealed interface AccountUiEvent {
    data class OnSettingsItemClick(val item: SettingsItemUiState) : AccountUiEvent
    data object OnLogoutConfirmClick : AccountUiEvent
    data object OnLogoutDialogDismiss : AccountUiEvent
    data object OnClickUpgradePremium : AccountUiEvent
    data object OnClickSignIn : AccountUiEvent
    data object OnClickProfile : AccountUiEvent
}
