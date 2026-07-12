package com.kotlinfoundation.koko.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_back
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_close
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_crown
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper
import com.kotlinfoundation.koko.designsystem.util.fillWidthOfParent
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppToolbar(
    title: String,
    titleContent: (@Composable () -> Unit)? = null,
    onNavigationIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: Painter? = painterResource(UiRes.drawable.ic_back),
    contentPadding: PaddingValues = PaddingValues(horizontal = AppTheme.spacing.outerSpacing),
    actions: @Composable RowScope.() -> Unit = {},
) {
    BaseAppToolbar(
        title = title,
        titleContent = titleContent,
        onNavigationIconClick = onNavigationIconClick,
        modifier = modifier,
        navigationIcon = navigationIcon,
        contentPadding = contentPadding,
        actions = actions,
    )
}

@Composable
fun AppToolbarWithProgress(
    nbTotalSteps: Int,
    nbCompletedSteps: Int,
    onNavigationIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: Painter? = painterResource(UiRes.drawable.ic_back),
    contentPadding: PaddingValues = PaddingValues(horizontal = AppTheme.spacing.outerSpacing),
    actions: @Composable RowScope.() -> Unit = {},
) {
    val progress by remember(nbCompletedSteps) { mutableStateOf(nbCompletedSteps.toFloat() / nbTotalSteps) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    )

    BaseAppToolbar(
        titleContent = {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.height(12.dp).fillMaxWidth(),
                color = AppTheme.colors.primary,
                trackColor = AppTheme.colors.outline,
                gapSize = (-10).dp,
                drawStopIndicator = {},
            )
        },
        onNavigationIconClick = onNavigationIconClick,
        modifier = modifier,
        navigationIcon = navigationIcon,
        contentPadding = contentPadding,
        title = "",
        actions = {
            Text(
                modifier = Modifier.padding(AppTheme.spacing.defaultSpacing),
                text = "$nbCompletedSteps / $nbTotalSteps",
                style = AppTheme.typography.h5,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.text.primary,
            )
            actions()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BaseAppToolbar(
    title: String,
    titleContent: (@Composable () -> Unit)? = null,
    onNavigationIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: Painter? = painterResource(UiRes.drawable.ic_back),
    contentPadding: PaddingValues = PaddingValues(horizontal = AppTheme.spacing.outerSpacing),
    actions: @Composable RowScope.() -> Unit = {},
) {
    val navigationIconStartPadding =
        contentPadding.calculateStartPadding(LocalLayoutDirection.current)
    val actionsEndPadding = contentPadding.calculateEndPadding(LocalLayoutDirection.current)

    TopAppBar(
        modifier = modifier.defaultMinSize(minHeight = 72.dp),
        colors =
        TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.colors.background,
            titleContentColor = AppTheme.colors.text.primary,
            navigationIconContentColor = AppTheme.colors.text.primary,
            actionIconContentColor = AppTheme.colors.text.primary,
        ),
        title = {
            Box(modifier = Modifier.wrapContentHeight(), contentAlignment = Alignment.Center) {
                if (titleContent != null) titleContent() else ToolbarTitle(text = title)
            }
        },
        navigationIcon = {
            // Material toolbar has default 4dp extra padding, and 10dp icon touch padding
            val extraPadding = 10.dp
            val startPadding = (navigationIconStartPadding - extraPadding).coerceAtLeast(0.dp)
            Box(
                modifier =
                Modifier
                    .fillMaxHeight()
                    .offset(x = (-4).dp)
                    .padding(start = startPadding),
                contentAlignment = Alignment.Center,
            ) {
                navigationIcon?.let { NavigationIcon(it, onNavigationIconClick) }
            }
        },
        actions = {
            // Material toolbar has default 4dp extra padding
            val endPadding = actionsEndPadding.coerceAtLeast(0.dp)
            Row(
                modifier =
                Modifier
                    .fillMaxHeight()
                    .offset(x = (4).dp)
                    .padding(end = endPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                actions()
            }
        },
    )
}

@Composable
private fun NavigationIcon(
    navigationIcon: Painter,
    onNavigationIconClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier =
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(
                onClick = { onNavigationIconClick() },
                role = Role.Button,
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 20.dp),
            ).padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = navigationIcon,
            modifier = Modifier.fillMaxSize(),
            contentDescription = "Navigation Icon",
        )
    }
}

@Composable
private fun ToolbarTitle(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
) {
    Text(
        text = text,
        style = AppTheme.typography.h4,
        color = AppTheme.colors.text.primary,
        textAlign = textAlign,
        modifier = modifier,
    )
}

@Preview
@Composable
internal fun AppToolbarPreview() {
    PreviewHelper {
        val totalHorizontalPadding =
            LocalDensity.current.run { 2 * AppTheme.spacing.outerSpacing.toPx() }
        AppToolbar(
            title = "Home",
            onNavigationIconClick = {},
            navigationIcon = null,
            actions = {
                Chip(
                    text = "Pro",
                    style = ChipStyle.FILLED,
                    size = ChipSize.SMALL,
                    startIconRes = UiRes.drawable.ic_crown,
                    onClick = {},
                )
            },
            modifier = Modifier.fillWidthOfParent(totalHorizontalPadding),
        )
        AppToolbar(
            title = "Settings Screen",
            onNavigationIconClick = {},
            modifier = Modifier.fillWidthOfParent(totalHorizontalPadding),
        )
        AppToolbar(
            title = "Details Screen",
            navigationIcon = painterResource(UiRes.drawable.ic_close),
            onNavigationIconClick = {},
            modifier = Modifier.fillWidthOfParent(totalHorizontalPadding),
        )
    }
}
