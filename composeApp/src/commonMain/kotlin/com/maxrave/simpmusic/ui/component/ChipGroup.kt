package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * PG Music filter chip.
 *
 * Deliberately avoids the old infinite animated border: these chips live in the Home header and
 * remain on screen while the user scrolls, so a permanent animation kept invalidating frames for
 * decoration that added very little. Selection is now communicated with a compact premium pill,
 * using the app theme so PG Music keeps its identity without hard-coded colours.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun Chip(
    isAnimated: Boolean = false,
    isSelected: Boolean = false,
    text: String,
    onClick: () -> Unit,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        ElevatedFilterChip(
            selected = isSelected,
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            elevation = FilterChipDefaults.elevatedFilterChipElevation(elevation = 0.dp),
            colors =
                FilterChipDefaults.elevatedFilterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.28f),
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            border =
                FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
                    selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                ),
            label = {
                Text(
                    text = text,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                )
            },
        )
    }
}
