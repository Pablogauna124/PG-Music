package com.maxrave.simpmusic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.simpmusic.expect.ui.SystemBarAppearanceEffect
import com.maxrave.simpmusic.expect.ui.platformDynamicColorScheme

@Immutable
data class AppColors(
    val favorite: Color,
    val lyricActive: Color,
    val shimmerBackground: Color,
    val shimmerLine: Color,
    val overlay: Color,
    val overlayHeavy: Color,
)

private val DarkAppColors =
    AppColors(
        favorite = favoriteColor,
        lyricActive = lyricActiveColor,
        shimmerBackground = shimmerBackground,
        shimmerLine = shimmerLine,
        overlay = overlay,
        overlayHeavy = blackMoreOverlay,
    )

private val LightAppColors =
    DarkAppColors.copy(
        shimmerBackground = shimmerBackgroundLight,
        shimmerLine = shimmerLineLight,
    )

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }
val LocalIsDarkTheme = staticCompositionLocalOf { true }
val LocalLiquidGlassEnabled = staticCompositionLocalOf { true }
val LocalForcedDarkColorScheme = staticCompositionLocalOf<ColorScheme?> { null }

fun parseThemeColorHex(hex: String): Color? {
    val clean = hex.trim().removePrefix("#")
    val argb =
        when (clean.length) {
            6 -> "FF$clean"
            8 -> clean
            else -> return null
        }
    return argb.toLongOrNull(16)?.let { Color(it) }
}

private fun ColorScheme.withNeutralLightSurfaces(): ColorScheme =
    copy(
        background = Color(0xFFFAFAFA),
        onBackground = Color(0xFF1B1B1B),
        surface = Color(0xFFFAFAFA),
        onSurface = Color(0xFF1B1B1B),
        surfaceVariant = Color(0xFFE2E2E2),
        onSurfaceVariant = Color(0xFF474747),
        surfaceTint = primary,
        surfaceBright = Color(0xFFFFFFFF),
        surfaceDim = Color(0xFFDADADA),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF7F7F7),
        surfaceContainer = Color(0xFFF1F1F1),
        surfaceContainerHigh = Color(0xFFECECEC),
        surfaceContainerHighest = Color(0xFFE6E6E6),
        outline = Color(0xFF777777),
        outlineVariant = Color(0xFFC7C7C7),
        inverseSurface = Color(0xFF303030),
        inverseOnSurface = Color(0xFFF1F1F1),
    )

private fun ColorScheme.withPgBrandColors(isDark: Boolean): ColorScheme =
    copy(
        primary = if (isDark) Color(0xFFFF2638) else Color(0xFFD50018),
        onPrimary = Color.White,
        // PG Music: a deeper wine-red container keeps the Home hero branded without flooding
        // the upper half of an OLED screen with bright red.
        primaryContainer = if (isDark) Color(0xFF350006) else Color(0xFFFFDADD),
        onPrimaryContainer = if (isDark) Color(0xFFFFDADD) else Color(0xFF410005),
        secondary = if (isDark) Color(0xFFB388FF) else Color(0xFF7042C1),
        onSecondary = if (isDark) Color(0xFF2D005F) else Color.White,
        secondaryContainer = if (isDark) Color(0xFF44177A) else Color(0xFFEBDDFF),
        onSecondaryContainer = if (isDark) Color(0xFFEBDDFF) else Color(0xFF270057),
        tertiary = if (isDark) Color(0xFFD95CFF) else Color(0xFF8B25AC),
        surfaceTint = if (isDark) Color(0xFFFF2638) else Color(0xFFD50018),
    )

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppTheme(
    themeMode: String = DataStoreManager.THEME_MODE_DARK,
    themeColorSource: String = DataStoreManager.THEME_COLOR_DEFAULT,
    customThemeColor: Color? = null,
    liquidGlassEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val isDark = isDarkTheme(themeMode)
    val wallpaperScheme =
        if (themeColorSource == DataStoreManager.THEME_COLOR_WALLPAPER) {
            platformDynamicColorScheme(isDark)
        } else null
    val seedColor =
        if (themeColorSource == DataStoreManager.THEME_COLOR_CUSTOM) {
            customThemeColor ?: seed
        } else seed
    val colorScheme =
        wallpaperScheme
            ?: rememberDynamicColorScheme(
                seedColor = seedColor,
                isDark = isDark,
                isAmoled = isDark,
                style = PaletteStyle.TonalSpot,
                modifyColorScheme = { cs ->
                    val neutral = if (isDark) cs else cs.withNeutralLightSurfaces()
                    if (themeColorSource == DataStoreManager.THEME_COLOR_DEFAULT) {
                        neutral.withPgBrandColors(isDark)
                    } else neutral
                },
            )
    val forcedDarkScheme =
        if (isDark) {
            colorScheme
        } else {
            rememberDynamicColorScheme(
                seedColor = seedColor,
                isDark = true,
                isAmoled = true,
                style = PaletteStyle.TonalSpot,
            )
        }
    SystemBarAppearanceEffect(isDark)
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        content = {
            CompositionLocalProvider(
                LocalRippleConfiguration provides SoftRippleConfiguration,
                LocalContentColor provides colorScheme.onSurfaceVariant,
                LocalAppColors provides if (isDark) DarkAppColors else LightAppColors,
                LocalIsDarkTheme provides isDark,
                LocalForcedDarkColorScheme provides forcedDarkScheme,
                LocalLiquidGlassEnabled provides liquidGlassEnabled,
                content = content,
            )
        },
        typography = typo(colorScheme),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
private val SoftRippleConfiguration =
    RippleConfiguration(
        rippleAlpha =
            RippleAlpha(
                draggedAlpha = 0.06f,
                focusedAlpha = 0.04f,
                hoveredAlpha = 0.03f,
                pressedAlpha = 0.04f,
            ),
    )

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ForceDarkContent(content: @Composable () -> Unit) {
    val darkScheme = LocalForcedDarkColorScheme.current ?: MaterialTheme.colorScheme
    MaterialExpressiveTheme(
        colorScheme = darkScheme,
        content = {
            CompositionLocalProvider(
                LocalForceDarkText provides true,
                LocalIsDarkTheme provides true,
                LocalContentColor provides darkScheme.onSurfaceVariant,
                LocalAppColors provides DarkAppColors,
                content = content,
            )
        },
        typography = typo(darkScheme, forceDark = true),
    )
}

@Composable
fun isDarkTheme(themeMode: String): Boolean =
    when (themeMode) {
        DataStoreManager.THEME_MODE_LIGHT -> false
        DataStoreManager.THEME_MODE_SYSTEM -> isSystemInDarkTheme()
        else -> true
    }
