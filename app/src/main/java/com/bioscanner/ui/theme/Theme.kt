package com.bioscanner.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = SurfaceWhite,
    primaryContainer = PillGreen,
    onPrimaryContainer = ForestGreen,
    secondary = TextSecondary,
    onSecondary = SurfaceWhite,
    secondaryContainer = BorderCard,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentGold,
    onTertiary = SurfaceWhite,
    tertiaryContainer = PillGreen,
    onTertiaryContainer = ForestGreen,
    background = CreamBackground,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = BorderCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight,
    outlineVariant = BorderCard,
    error = AlertRed,
    onError = SurfaceWhite,
    errorContainer = AlertRedContainer,
    onErrorContainer = AlertRedOnContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary = LightGreenAccent,
    onPrimary = ForestGreenDark,
    primaryContainer = ForestGreenDark,
    onPrimaryContainer = PillGreen,
    secondary = BorderChip,
    onSecondary = TextPrimary,
    tertiary = AccentGold,
    onTertiary = ForestGreenDark,
    tertiaryContainer = ForestGreenDark,
    onTertiaryContainer = PillGreen,
    background = DarkBackground,
    onBackground = CreamBackground,
    surface = DarkSurface,
    onSurface = CreamBackground,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = BorderChip,
    outline = TextSecondary,
    outlineVariant = DarkSurface,
    error = Color(0xFFE2897A),
    onError = Color(0xFF3A0F09),
    errorContainer = Color(0xFF5C2015),
    onErrorContainer = AlertRedContainer,
)

@Composable
fun BioScannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // A paleta "guia de campo" é intencional (verde + dourado sobre creme);
    // cor dinâmica do Android 12+ substituiria isso pelo wallpaper do
    // usuário, então fica desligada por padrão.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
