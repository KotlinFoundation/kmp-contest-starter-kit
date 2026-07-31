package com.kotlinfoundation.koko.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.defaultScreenPadding
import com.kotlinfoundation.koko.designsystem.util.scrollableScreenPadding
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * @param includeBottomInsets - If bottom nav is visible, then this should be false, otherwise set true
 */
@Composable
fun ScreenWithToolbar(
    modifier: Modifier = Modifier,
    title: String = "",
    titleContent: @Composable (() -> Unit)? = null,
    includeBottomInsets: Boolean = false,
    toolbarExtraContent: @Composable RowScope.() -> Unit = {},
    onNavigationIconClick: () -> Unit = {},
    navigationIcon: DrawableResource? = null,
    isScrollableContent: Boolean = false,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
        modifier
            .background(AppTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .then(
                if (includeBottomInsets) {
                    Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
                    )
                } else {
                    Modifier
                },
            ),
    ) {
        AppToolbar(
            title = title,
            titleContent = titleContent,
            navigationIcon = navigationIcon?.let { painterResource(navigationIcon) },
            onNavigationIconClick = onNavigationIconClick,
            actions = toolbarExtraContent,
        )
        val contentModifier =
            if (isScrollableContent) {
                Modifier.scrollableScreenPadding(
                    scrollState = rememberScrollState(),
                    appSpacing = AppTheme.spacing,
                )
            } else {
                Modifier.defaultScreenPadding(appSpacing = AppTheme.spacing)
            }

        Box(modifier = contentModifier) {
            content()
        }
    }
}

@Composable
fun ScreenWithProgressedToolbar(
    modifier: Modifier = Modifier,
    nbTotalSteps: Int,
    nbCompletedSteps: Int,
    onNavigationIconClick: () -> Unit,
    isScrollableContent: Boolean = false,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
        modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        AppToolbarWithProgress(
            nbTotalSteps = nbTotalSteps,
            nbCompletedSteps = nbCompletedSteps,
            onNavigationIconClick = onNavigationIconClick,
        )
        val contentModifier =
            if (isScrollableContent) {
                Modifier.scrollableScreenPadding(
                    scrollState = rememberScrollState(),
                    appSpacing = AppTheme.spacing,
                )
            } else {
                Modifier.defaultScreenPadding(appSpacing = AppTheme.spacing)
            }

        Box(modifier = contentModifier) {
            content()
        }
    }
}
