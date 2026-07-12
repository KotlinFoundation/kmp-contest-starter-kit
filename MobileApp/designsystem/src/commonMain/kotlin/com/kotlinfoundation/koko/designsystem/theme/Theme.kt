package com.kotlinfoundation.koko.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

val LocalThemeIsDark = compositionLocalOf { true }
val LocalAppColors = staticCompositionLocalOf { lightModeAppColors }
val LocalAppTypography =
    staticCompositionLocalOf<AppTypography> { error("Typography not provided") }
val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }

@Composable
fun AppTheme(
    isDarkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalThemeIsDark provides isDarkMode,
        LocalAppColors provides if (isDarkMode) darkModeAppColors else lightModeAppColors,
        LocalAppTypography provides appTypography,
        LocalAppSpacing provides appSpacing,
    ) {
        SystemAppearance(isDarkMode)
        MaterialTheme(
            colorScheme = LocalAppColors.current.asMaterialColorScheme(isDarkMode),
            typography = MaterialThemAppTypography,
            content = {
                Surface(
                    content = content,
                    color = AppTheme.colors.background,
                )
            },
        )
    }
}

object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable
        get() = LocalAppColors.current

    val typography: AppTypography
        @Composable @ReadOnlyComposable
        get() = LocalAppTypography.current

    val spacing: AppSpacing
        @Composable @ReadOnlyComposable
        get() = LocalAppSpacing.current
}

@Composable
internal expect fun SystemAppearance(isDark: Boolean)
