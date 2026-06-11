package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val bgGradientStart: Color,
    val bgGradientEnd: Color,
    val bgBase: Color,
    val glassFill: Color,
    val glassFillStrong: Color,
    val glassFillSubtle: Color,
    val sheetSurface: Color,
    val sheetScrim: Color,
    val sheetCardFill: Color,
    val glassBorder: Color,
    val glassBorderSubtle: Color,
    val glassShadow: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val surfaceOverlay: Color,
    val surfaceOverlaySubtle: Color,
    val surfaceOverlayMedium: Color,
    val surfaceOverlayLight: Color,
    val surfaceOverlayStrong: Color,
    val dividerColor: Color,
    val scrimDark: Color,
    val ringTrack: Color,
    val accentGreen: Color,
    val darkGreen: Color,
    val tealGreen: Color,
    val accentGreenBg: Color,
    val accentGreenBorder: Color,
    val lightRed: Color,
    val redBg: Color,
    val redBorder: Color,
    val lightAmber: Color,
    val amberBg: Color,
    val amberBorder: Color,
    val amberSurface: Color,
    val textPrimary: Color,
    val textWhite: Color,
    val mutedText: Color,
    val hintText: Color,
    val textOnAccent: Color,
    val timelineLabel: Color,
    val inactiveGray: Color,
    val inactiveDot: Color
)

val LightAppColors = AppColors(
    bgGradientStart = BgGradientStart,
    bgGradientEnd = BgGradientEnd,
    bgBase = BgBase,
    glassFill = GlassFill,
    glassFillStrong = GlassFillStrong,
    glassFillSubtle = GlassFillSubtle,
    sheetSurface = SheetSurface,
    sheetScrim = SheetScrim,
    sheetCardFill = SheetCardFill,
    glassBorder = GlassBorder,
    glassBorderSubtle = GlassBorderSubtle,
    glassShadow = GlassShadow,
    cardBg = CardBg,
    cardBorder = CardBorder,
    surfaceOverlay = SurfaceOverlay,
    surfaceOverlaySubtle = SurfaceOverlaySubtle,
    surfaceOverlayMedium = SurfaceOverlayMedium,
    surfaceOverlayLight = SurfaceOverlayLight,
    surfaceOverlayStrong = SurfaceOverlayStrong,
    dividerColor = DividerColor,
    scrimDark = ScrimDark,
    ringTrack = RingTrack,
    accentGreen = AccentGreen,
    darkGreen = DarkGreen,
    tealGreen = TealGreen,
    accentGreenBg = AccentGreenBg,
    accentGreenBorder = AccentGreenBorder,
    lightRed = LightRed,
    redBg = RedBg,
    redBorder = RedBorder,
    lightAmber = LightAmber,
    amberBg = AmberBg,
    amberBorder = AmberBorder,
    amberSurface = AmberSurface,
    textPrimary = TextPrimary,
    textWhite = TextWhite,
    mutedText = MutedText,
    hintText = HintText,
    textOnAccent = TextOnAccent,
    timelineLabel = TimelineLabel,
    inactiveGray = InactiveGray,
    inactiveDot = InactiveDot
)

val DarkAppColors = AppColors(
    bgGradientStart = Color(0xFF0B0E14),
    bgGradientEnd = Color(0xFF151B26),
    bgBase = Color(0xFF0D1117),
    glassFill = Color(0xFF1C2433).copy(alpha = 0.78f),
    glassFillStrong = Color(0xFF232D3F).copy(alpha = 0.86f),
    glassFillSubtle = Color(0xFF182030).copy(alpha = 0.62f),
    sheetSurface = Color(0xFF1E2738).copy(alpha = 0.93f),
    sheetScrim = Color.Black.copy(alpha = 0.68f),
    sheetCardFill = Color(0xFF232D3F).copy(alpha = 0.88f),
    glassBorder = Color(0xFF8B9BB5).copy(alpha = 0.28f),
    glassBorderSubtle = Color(0xFF8B9BB5).copy(alpha = 0.16f),
    glassShadow = Color.Black.copy(alpha = 0.55f),
    cardBg = Color(0xFF1E2738).copy(alpha = 0.80f),
    cardBorder = Color(0xFF9AA8C2).copy(alpha = 0.22f),
    surfaceOverlay = Color(0xFF3D4F6F).copy(alpha = 0.30f),
    surfaceOverlaySubtle = Color(0xFF3D4F6F).copy(alpha = 0.18f),
    surfaceOverlayMedium = Color(0xFF4A5D7A).copy(alpha = 0.38f),
    surfaceOverlayLight = Color(0xFF5A6D8A).copy(alpha = 0.45f),
    surfaceOverlayStrong = Color(0xFF6B7E9A).copy(alpha = 0.52f),
    dividerColor = Color(0xFF8B9BB5).copy(alpha = 0.18f),
    scrimDark = Color.Black.copy(alpha = 0.62f),
    ringTrack = Color(0xFF4A5D7A).copy(alpha = 0.55f),
    accentGreen = Color(0xFF8B8FE8),
    darkGreen = Color(0xFFA8ABF0),
    tealGreen = Color(0xFF4ADE80),
    accentGreenBg = Color(0xFF6366E8).copy(alpha = 0.22f),
    accentGreenBorder = Color(0xFF8B8FE8).copy(alpha = 0.38f),
    lightRed = Color(0xFFFF8A8A),
    redBg = Color(0xFFE85D5D).copy(alpha = 0.20f),
    redBorder = Color(0xFFFF8A8A).copy(alpha = 0.38f),
    lightAmber = Color(0xFFE8C872),
    amberBg = Color(0xFFE8B84D).copy(alpha = 0.22f),
    amberBorder = Color(0xFFE8C872).copy(alpha = 0.40f),
    amberSurface = Color(0xFFE8B84D).copy(alpha = 0.28f),
    textPrimary = Color(0xFFF0F2F5),
    textWhite = Color.White,
    mutedText = Color(0xFFA8B0BD),
    hintText = Color(0xFF7A8494),
    textOnAccent = Color.White,
    timelineLabel = Color(0xE6F0F2F5),
    inactiveGray = Color(0xFF6B7585),
    inactiveDot = Color(0xFF8B9BB5).copy(alpha = 0.28f)
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

@Composable
fun ProvideAppColors(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAppColors provides if (darkTheme) DarkAppColors else LightAppColors,
        content = content
    )
}
