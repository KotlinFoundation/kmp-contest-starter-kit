package com.kotlinfoundation.koko.designsystem.theme

import androidx.compose.ui.graphics.Color

val Color.Companion.GRAY_50: Color get() = Color(0xFFFAFAFA)
val Color.Companion.GRAY_100: Color get() = Color(0xFFF5F5F5)
val Color.Companion.GRAY_200: Color get() = Color(0xFFEEEEEE)
val Color.Companion.GRAY_300: Color get() = Color(0xFFE0E0E0)
val Color.Companion.GRAY_400: Color get() = Color(0xFFBDBDBD)
val Color.Companion.GRAY_500: Color get() = Color(0xFF9E9E9E)
val Color.Companion.GRAY_600: Color get() = Color(0xFF757575)
val Color.Companion.GRAY_700: Color get() = Color(0xFF616161)
val Color.Companion.GRAY_800: Color get() = Color(0xFF424242)
val Color.Companion.GRAY_900: Color get() = Color(0xFF212121)

val Color.Companion.BLACK_1_HEX_18: Color get() = Color(0xFF181A20)
val Color.Companion.BLACK_2_HEX_1E: Color get() = Color(0xFF1E2025)
val Color.Companion.BLACK_3_HEX_1F: Color get() = Color(0xFF1F222A)
val Color.Companion.BLACK_4_HEX_26: Color get() = Color(0xFF262A35)
val Color.Companion.BLACK_5_HEX_35: Color get() = Color(0xFF35383F)

/**
 * https://mui.com/material-ui/customization/color/
 * Below list are 500 shades of material colors hex codes.
 */
internal object MaterialColor {
    const val RED = 0xFFF44336
    const val PINK = 0xFFE91E63
    const val PURPLE = 0xFF9C27B0
    const val DEEP_PURPLE = 0xFF673AB7
    const val INDIGO = 0xFF3F51B5
    const val BLUE = 0xFF2196F3
    const val LIGHT_BLUE = 0xFF03A9F4
    const val CYAN = 0xFF00BCD4
    const val TEAL = 0xFF009688
    const val GREEN = 0xFF4CAF50
    const val LIGHT_GREEN = 0xFF8BC34A
    const val LIME = 0xFFCDDC39
    const val YELLOW = 0xFFFFEB3B
    const val AMBER = 0xFFFFC107
    const val ORANGE = 0xFFFF9800
    const val DEEP_ORANGE = 0xFFFF5722
    const val BROWN = 0xFF795548
    const val GREY = 0xFF9E9E9E
    const val BLUE_GREY = 0xFF607D8B
}
