package com.kotlinfoundation.koko.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.help_and_support
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_arrow_right
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_settings_item_logout
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_settings_item_support_legal
import com.kotlinfoundation.koko.designsystem.generated.resources.logout
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

data class SettingsItemUiState(
    val textRes: StringResource,
    val startIcon: DrawableResource? = null,
    val helperTextRes: StringResource? = null,
    val textIconColor: Color? = null,
    val showEndIcon: Boolean = true,
)

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    settingsItemUiState: SettingsItemUiState,
    textStyle: TextStyle = AppTheme.typography.h6,
) {
    val textIconColor = settingsItemUiState.textIconColor ?: AppTheme.colors.text.primary
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.horizontalItemSpacing),
    ) {
        settingsItemUiState.startIcon?.let {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = vectorResource(it),
                contentDescription = null,
                tint = textIconColor,
            )
        }

        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(settingsItemUiState.textRes),
            style = textStyle,
            color = textIconColor,
        )

        Row {
            settingsItemUiState.helperTextRes?.let {
                Text(
                    text = stringResource(it),
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.text.secondary,
                )
                Spacer(modifier = Modifier.width(AppTheme.spacing.inputIconTextSpacing))
            }

            if (settingsItemUiState.showEndIcon) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = vectorResource(UiRes.drawable.ic_arrow_right),
                    contentDescription = null,
                    tint = textIconColor,
                )
            }
        }
    }
}

@Composable
fun SettingItemListContainer(
    itemList: List<SettingsItemUiState>,
    itemTextStyle: TextStyle = AppTheme.typography.h6,
    onClick: (SettingsItemUiState) -> Unit = {},
) {
    AppCardContainer(contentPaddingValues = PaddingValues(0.dp)) {
        Column {
            itemList.forEach {
                SettingsItem(
                    settingsItemUiState = it,
                    textStyle = itemTextStyle,
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            onClick(it)
                        }.padding(AppTheme.spacing.cardContentSpacing),
                )
            }
        }
    }
}

@Composable
@Preview
internal fun SettingsItemPreview() {
    PreviewHelper {
        val itemList =
            listOf(
                SettingsItemUiState(
                    startIcon = UiRes.drawable.ic_settings_item_support_legal,
                    textRes = UiRes.string.help_and_support,
                ),
                SettingsItemUiState(
                    startIcon = UiRes.drawable.ic_settings_item_logout,
                    textRes = UiRes.string.logout,
                    showEndIcon = false,
                ),
            )
        SettingItemListContainer(itemList = itemList)
    }
}
