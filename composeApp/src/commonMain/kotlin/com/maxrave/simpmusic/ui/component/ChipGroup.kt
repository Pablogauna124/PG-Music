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
 * PG Music Home filter pill.
 *
 * Keeps the control lightweight (no infinite border animation or shadow) while giving the Home
 * header the compact outlined-pill treatment used by PG Music. Colours remain theme driven so the
 * selected chip follows the app accent and unselected chips stay subtle over the OLED background.
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
            shape = RoundedCornerShape(22.dp),
            elevation = FilterChipDefaults.elevatedFilterChipElevation(elevation = 0.dp),
            colors =
                FilterChipDefaults.elevatedFilterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.12f),
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    labelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.82f),
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            border =
                FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.58f),
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                ),
            label = {
                Text(
                    text = text,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                )
            },
        )
    }
}
