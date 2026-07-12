package com.kotlinfoundation.koko.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
class AppSpacing(
    val defaultSpacing: Dp = 8.dp, // Default spacing between elements
    val outerSpacing: Dp = 24.dp, // Padding around the screen or container edges
    val sectionSpacing: Dp = 24.dp, // Space between major sections of the UI
    val sectionHeaderSpacing: Dp = 20.dp, // Space between a section header and its content
    val groupedVerticalElementSpacing: Dp = 12.dp, // Space between grouped elements in a vertical style. For example title and description
    val groupedVerticalElementSpacingSmall: Dp = 6.dp, // Small Space between grouped elements in a vertical style. If title is small, then use this one. For example if title is h6
    val inputIconTextSpacing: Dp = 12.dp, // Space between an icon and its text
    val horizontalItemSpacing: Dp = 16.dp, // Horizontal spacing between items in a group (e.g., list, card)
    val verticalListItemSpacing: Dp = 20.dp, // Vertical spacing between items in a list
    val verticalListItemSpacingSmall: Dp = 16.dp, // Vertical spacing between items in a list if the item is small, like body text
    val cardContentSpacing: Dp = 16.dp, // Spacing inside a card content
    val dialogContentSpacing: Dp = 32.dp, // Spacing inside a card content
    val largeSpacing: Dp = 36.dp, // Larger spacing
)

val appSpacing = AppSpacing()
