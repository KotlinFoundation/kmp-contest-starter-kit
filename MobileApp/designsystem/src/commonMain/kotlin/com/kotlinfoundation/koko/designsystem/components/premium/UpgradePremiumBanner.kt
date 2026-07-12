package com.kotlinfoundation.koko.designsystem.components.premium

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.components.AppCardContainer
import com.kotlinfoundation.koko.designsystem.components.Chip
import com.kotlinfoundation.koko.designsystem.components.ChipSize
import com.kotlinfoundation.koko.designsystem.components.ChipStyle
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.banner_subtitle_upgrade_premium
import com.kotlinfoundation.koko.designsystem.generated.resources.banner_title_upgrade_premium
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_action_upgrade_premium
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_big_crown_premium
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_crown
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

enum class UpgradePremiumBannerStyle {
    LARGE,
    SMALL,
}

@Composable
fun UpgradePremiumBanner(
    modifier: Modifier = Modifier,
    style: UpgradePremiumBannerStyle = UpgradePremiumBannerStyle.SMALL,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    onClick: () -> Unit = {},
) {
    when (style) {
        UpgradePremiumBannerStyle.LARGE -> LargeBannerStyle(modifier, shape, onClick)
        UpgradePremiumBannerStyle.SMALL -> SmallBannerStyle(modifier, shape, onClick)
    }
}

@Composable
private fun LargeBannerStyle(
    modifier: Modifier,
    shape: Shape,
    onClick: () -> Unit,
) {
    val imageSize = 120.dp
    Box {
        AppCardContainer(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            backgroundColor = AppTheme.colors.primary,
            onClick = onClick,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.horizontalItemSpacing),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing),
                    modifier = Modifier.weight(1f),
                ) {
                    TitleAndDescription()
                    Chip(
                        style = ChipStyle.INVERSE_FILLED,
                        text = stringResource(UiRes.string.btn_action_upgrade_premium),
                        size = ChipSize.SMALL,
                        onClick = onClick,
                    )
                }
                Spacer(modifier = Modifier.width(imageSize))
            }
        }

        Row(
            modifier =
            modifier
                .fillMaxWidth()
                .clip(shape = shape)
                .background(AppTheme.colors.primary)
                .clickable { onClick() }
                .padding(AppTheme.spacing.cardContentSpacing),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.horizontalItemSpacing),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing),
                modifier = Modifier.weight(1f),
            ) {
                TitleAndDescription()
                Chip(
                    style = ChipStyle.INVERSE_FILLED,
                    text = stringResource(UiRes.string.btn_action_upgrade_premium),
                    size = ChipSize.SMALL,
                    onClick = onClick,
                )
            }
            Spacer(modifier = Modifier.width(imageSize))
        }

        Box(
            modifier =
            Modifier
                .align(Alignment.TopEnd)
                .padding(end = AppTheme.spacing.cardContentSpacing),
        ) {
            Image(
                modifier =
                Modifier
                    .size(imageSize)
                    .offset(y = -AppTheme.spacing.cardContentSpacing)
                    .rotate(-12f),
                painter = painterResource(UiRes.drawable.ic_big_crown_premium),
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun SmallBannerStyle(
    modifier: Modifier,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
) {
    AppCardContainer(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        backgroundColor = AppTheme.colors.primary,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.horizontalItemSpacing),
        ) {
            CrownImage()
            TitleAndDescription()
        }
    }
}

@Composable
private fun CrownImage() {
    Box(
        modifier =
        Modifier
            .clip(CircleShape)
            .size(50.dp)
            .background(AppTheme.colors.onPrimary)
            .padding(AppTheme.spacing.defaultSpacing),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(UiRes.drawable.ic_crown),
            tint = Color(0xFFFF9800),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun TitleAndDescription(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacingSmall),
    ) {
        Text(
            text = stringResource(UiRes.string.banner_title_upgrade_premium),
            style = AppTheme.typography.h6,
            color = AppTheme.colors.onPrimary,
        )
        Text(
            text = stringResource(UiRes.string.banner_subtitle_upgrade_premium),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.onPrimary,
        )
    }
}

@Composable
@Preview
internal fun UpgradePremiumBannerPreview() {
    PreviewHelper {
        UpgradePremiumBanner(style = UpgradePremiumBannerStyle.SMALL)
        UpgradePremiumBanner(style = UpgradePremiumBannerStyle.LARGE)
    }
}
