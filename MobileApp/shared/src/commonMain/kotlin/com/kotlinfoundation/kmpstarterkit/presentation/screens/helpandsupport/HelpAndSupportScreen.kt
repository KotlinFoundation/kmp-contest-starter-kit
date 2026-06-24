package com.kotlinfoundation.kmpstarterkit.presentation.screens.helpandsupport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import com.kotlinfoundation.kmpstarterkit.designsystem.components.ScreenWithToolbar
import com.kotlinfoundation.kmpstarterkit.designsystem.components.SettingItemListContainer
import com.kotlinfoundation.kmpstarterkit.designsystem.components.SettingsItemUiState
import com.kotlinfoundation.kmpstarterkit.designsystem.generated.resources.UiRes
import com.kotlinfoundation.kmpstarterkit.designsystem.generated.resources.ic_back
import com.kotlinfoundation.kmpstarterkit.designsystem.generated.resources.privacy_policy
import com.kotlinfoundation.kmpstarterkit.designsystem.generated.resources.terms_conditions
import com.kotlinfoundation.kmpstarterkit.designsystem.theme.AppTheme
import com.kotlinfoundation.kmpstarterkit.generated.resources.Res
import com.kotlinfoundation.kmpstarterkit.generated.resources.help_and_support
import com.kotlinfoundation.kmpstarterkit.generated.resources.item_contact_support
import com.kotlinfoundation.kmpstarterkit.util.AppUtil
import com.kotlinfoundation.kmpstarterkit.util.Constants
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun HelpAndSupportScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
) {
    val itemList = listOf(
        SettingsItemUiState(textRes = Res.string.item_contact_support),
        SettingsItemUiState(textRes = UiRes.string.privacy_policy),
        SettingsItemUiState(textRes = UiRes.string.terms_conditions),
    )
    val localUriHandler = LocalUriHandler.current
    val appUtil = koinInject<AppUtil>()
    ScreenWithToolbar(
        modifier = modifier.fillMaxSize().background(AppTheme.colors.background),
        title = stringResource(Res.string.help_and_support),
        includeBottomInsets = true,
        isScrollableContent = true,
        onNavigationIconClick = onNavigateBack,
        navigationIcon = UiRes.drawable.ic_back,
    ) {
        SettingItemListContainer(
            itemList = itemList,
            itemTextStyle = AppTheme.typography.h5.copy(fontWeight = FontWeight.SemiBold),
            onClick = {
                when (it.textRes) {
                    Res.string.item_contact_support -> {
                        appUtil.openFeedbackMail()
                    }

                    UiRes.string.privacy_policy -> {
                        localUriHandler.openUri(Constants.URL_PRIVACY_POLICY)
                    }

                    UiRes.string.terms_conditions -> {
                        localUriHandler.openUri(Constants.URL_TERMS_CONDITIONS)
                    }
                }
            },
        )
    }
}
