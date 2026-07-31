package com.kotlinfoundation.koko.designsystem.components.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.theme.AppTheme

@Composable
fun FilterDialog(
    title: String,
    groupItems: List<FilterItemGroupUiState>,
    onFilterSelected: (FilterItemUiState) -> Unit,
    onDismiss: () -> Unit,
) {
    AppDialog(
        hideButtons = true,
        title = title,
        contentPaddingSpacing = AppTheme.spacing.cardContentSpacing,
        onDismiss = onDismiss,
        content = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing),
            ) {
                groupItems.forEach { groupItem ->
                    FilterSection(
                        title = groupItem.title,
                        items = groupItem.items,
                        onClick = { item ->
                            onFilterSelected(item)
                        },
                    )
                }
            }
        },
    )
}

@Composable
fun FilterSection(
    title: String,
    items: List<FilterItemUiState>,
    onClick: (FilterItemUiState) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacingSmall),
    ) {
        Text(
            text = title,
            style = AppTheme.typography.h6,
            color = AppTheme.colors.text.primary,
        )

        items.forEach { filter ->
            FilterItem(
                item = filter,
                onClick = {
                    onClick(filter)
                },
            )
        }
    }
}

data class FilterItemGroupUiState(
    val title: String,
    val items: List<FilterItemUiState>,
)

data class FilterItemUiState(
    val id: String,
    val icon: ImageVector,
    val text: String,
    val isSelected: Boolean = false,
)

// Individual filter item in modal
@Composable
private fun FilterItem(
    item: FilterItemUiState,
    onClick: () -> Unit,
) {
    val isSelected = item.isSelected

    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) AppTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent)
            .clickable {
                onClick()
            }.padding(AppTheme.spacing.cardContentSpacing),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inputIconTextSpacing),
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = if (isSelected) AppTheme.colors.primary else AppTheme.colors.text.secondary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = item.text,
            style =
            AppTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = if (isSelected) AppTheme.colors.primary else AppTheme.colors.text.primary,
            modifier = Modifier.weight(1f),
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = AppTheme.colors.primary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
