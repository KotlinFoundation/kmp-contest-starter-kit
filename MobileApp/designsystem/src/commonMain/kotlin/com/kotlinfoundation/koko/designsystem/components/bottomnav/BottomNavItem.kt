package com.kotlinfoundation.koko.designsystem.components.bottomnav

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/**
 * @param label Bottom navigation item label text resource
 * @param icon Bottom navigation item icon drawable resource
 * @param route Bottom navigation screen route. Used for navigation.
 * Make sure route strings are same as ScreenRoute class names. Ex: HomeScreenRoute
 */
data class BottomNavItem(
    val label: StringResource,
    val icon: DrawableResource? = null,
    val route: String = "",
)
