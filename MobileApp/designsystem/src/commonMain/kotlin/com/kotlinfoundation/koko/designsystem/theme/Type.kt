package com.kotlinfoundation.koko.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.poppins_bold
import com.kotlinfoundation.koko.designsystem.generated.resources.poppins_medium
import com.kotlinfoundation.koko.designsystem.generated.resources.poppins_regular
import com.kotlinfoundation.koko.designsystem.generated.resources.poppins_semibold
import org.jetbrains.compose.resources.Font

private val fontFamily
    @Composable get() =
        FontFamily(
            Font(
                UiRes.font.poppins_regular,
                FontWeight.Normal,
                FontStyle.Normal,
            ),
            Font(
                UiRes.font.poppins_medium,
                FontWeight.Medium,
                FontStyle.Normal,
            ),
            Font(
                UiRes.font.poppins_semibold,
                FontWeight.SemiBold,
                FontStyle.Normal,
            ),
            Font(
                UiRes.font.poppins_bold,
                FontWeight.Bold,
                FontStyle.Normal,
            ),
        )

/**
 * App typography values
 * @param h2: Used for example app name in splash screen
 * @param h3: h3 can be used in screens like big title, where it is like one part of screen.
 * For example, in Forgot Password screen or OnBoarding Screen title, this big text can use h3
 *
 * @param h4: Most used title. For example toolbar title, card title, dialog title, etc.
 * @param h5: Mostly used in section titles
 */
@Immutable
class AppTypography(
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val h4: TextStyle,
    val h5: TextStyle,
    val h6: TextStyle,
    val bodyExtraLarge: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val bodyExtraSmall: TextStyle,
)

// Default Material 3 typography values
private val baseline = Typography()

val MaterialThemAppTypography
    @Composable
    get() =
        Typography(
            displayLarge = baseline.displayLarge.copy(fontFamily = fontFamily),
            displayMedium = baseline.displayMedium.copy(fontFamily = fontFamily),
            displaySmall = baseline.displaySmall.copy(fontFamily = fontFamily),
            headlineLarge = baseline.headlineLarge.copy(fontFamily = fontFamily),
            headlineMedium = baseline.headlineMedium.copy(fontFamily = fontFamily),
            headlineSmall = baseline.headlineSmall.copy(fontFamily = fontFamily),
            titleLarge = baseline.titleLarge.copy(fontFamily = fontFamily),
            titleMedium = baseline.titleMedium.copy(fontFamily = fontFamily),
            titleSmall = baseline.titleSmall.copy(fontFamily = fontFamily),
            bodyLarge = baseline.bodyLarge.copy(fontFamily = fontFamily),
            bodyMedium = baseline.bodyMedium.copy(fontFamily = fontFamily),
            bodySmall = baseline.bodySmall.copy(fontFamily = fontFamily),
            labelLarge = baseline.labelLarge.copy(fontFamily = fontFamily),
            labelMedium = baseline.labelMedium.copy(fontFamily = fontFamily),
            labelSmall = baseline.labelSmall.copy(fontFamily = fontFamily),
        )

val appTypography
    @Composable get() =
        AppTypography(
            h1 =
            TextStyle(
                fontFamily = fontFamily,
                letterSpacing = 0.sp,
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp,
                lineHeight = 67.sp,
            ),
            h2 =
            TextStyle(
                letterSpacing = 0.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                lineHeight = 56.sp,
            ),
            h3 =
            TextStyle(
                letterSpacing = 0.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 45.sp,
            ),
            h4 =
            TextStyle(
                letterSpacing = 0.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 34.sp,
            ),
            h5 =
            TextStyle(
                letterSpacing = 0.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 28.sp,
            ),
            h6 =
            TextStyle(
                letterSpacing = 0.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 25.sp,
            ),
            bodyExtraLarge =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.2.sp,
                fontSize = 18.sp,
                lineHeight = 29.sp,
            ),
            bodyLarge =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.2.sp,
                fontSize = 16.sp,
                lineHeight = 26.sp,
            ),
            bodyMedium =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.2.sp,
                fontSize = 14.sp,
                lineHeight = 22.sp,
            ),
            bodySmall =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.2.sp,
                fontSize = 12.sp,
                lineHeight = 19.sp,
            ),
            bodyExtraSmall =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.2.sp,
                fontSize = 10.sp,
                lineHeight = 16.sp,
            ),
        )
