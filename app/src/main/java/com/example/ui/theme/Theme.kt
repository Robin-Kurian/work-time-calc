package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun lightColorScheme() = lightColorScheme(
    primary = AccentGreen,
    secondary = DarkGreen,
    tertiary = LightAmber,
    background = BgGradientStart,
    surface = GlassFillStrong,
    surfaceVariant = GlassFill,
    outline = CardBorder,
    error = LightRed,
    onPrimary = TextOnAccent,
    onSecondary = TextOnAccent,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onError = TextOnAccent
)

private fun darkColorScheme() = darkColorScheme(
    primary = DarkAppColors.accentGreen,
    secondary = DarkAppColors.darkGreen,
    tertiary = DarkAppColors.lightAmber,
    background = DarkAppColors.bgGradientStart,
    surface = DarkAppColors.glassFillStrong,
    surfaceVariant = DarkAppColors.glassFill,
    outline = DarkAppColors.cardBorder,
    error = DarkAppColors.lightRed,
    onPrimary = DarkAppColors.textOnAccent,
    onSecondary = DarkAppColors.textOnAccent,
    onTertiary = DarkAppColors.textPrimary,
    onBackground = DarkAppColors.textPrimary,
    onSurface = DarkAppColors.textPrimary,
    onError = DarkAppColors.textOnAccent
)

@Composable
fun WorkTimeCalcTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = appColors.bgGradientStart.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    ProvideAppColors(darkTheme = darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = appTypography(appColors),
            content = content
        )
    }
}
