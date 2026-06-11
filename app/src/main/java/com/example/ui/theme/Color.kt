package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ── Background gradient ──────────────────────────────────────
val BgGradientStart = Color(0xFFF0F4F8)
val BgGradientEnd = Color(0xFFE8EEF5)
val BgBase = Color(0xFFF2F5F9)

// ── Glass surfaces ───────────────────────────────────────────
val GlassFill = Color.White.copy(alpha = 0.65f)
val GlassFillStrong = Color.White.copy(alpha = 0.75f)
val GlassFillSubtle = Color.White.copy(alpha = 0.55f)
val SheetSurface = Color(0xFFF8FAFC).copy(alpha = 0.96f)
val SheetScrim = Color.Black.copy(alpha = 0.42f)
val SheetCardFill = Color.White.copy(alpha = 0.82f)
val GlassBorder = Color.White.copy(alpha = 0.85f)
val GlassBorderSubtle = Color(0x66FFFFFF)
val GlassShadow = Color(0x1A5B6B8A)

// Legacy aliases — remapped to light glass tokens
val BgDark = BgBase
val SurfaceElevated = GlassFillStrong
val CardBg = GlassFill
val CardBorder = Color(0x55FFFFFF)

// ── Overlays & dividers ──────────────────────────────────────
val SurfaceOverlay = Color(0x147C89A8)
val SurfaceOverlaySubtle = Color(0x0A7C89A8)
val SurfaceOverlayMedium = Color(0x1F7C89A8)
val SurfaceOverlayLight = Color(0x287C89A8)
val SurfaceOverlayStrong = Color(0x337C89A8)
val DividerColor = Color(0x227C89A8)
val ScrimDark = Color(0x35000000)
val RingTrack = Color(0x337C89A8)

// ── Primary accent (soft indigo-violet) ──────────────────────
val AccentGreen = Color(0xFF6366A8)
val DarkGreen = Color(0xFF8186CF)
val TealGreen = Color(0xFF5258A3)
val AccentGreenBg = Color(0x286366A8)
val AccentGreenBorder = Color(0x506366A8)

// ── Errors / punch-out (muted rose) ──────────────────────────
val LightRed = Color(0xFFC4777A)
val RedBg = Color(0x28C4777A)
val RedBorder = Color(0x45C4777A)

// ── Warnings / leave alerts (soft gold) ──────────────────────
val LightAmber = Color(0xFFC9A962)
val AmberBg = Color(0x24C9A962)
val AmberBorder = Color(0x55C9A962)
val AmberSurface = Color(0x33C9A962)

// ── Text ─────────────────────────────────────────────────────
val TextPrimary = Color(0xFF2C3340)
val TextWhite = Color.White
val MutedText = Color(0xFF6B7585)
val HintText = Color(0xFF9AA3B2)
val TextOnAccent = Color.White
val TimelineLabel = Color(0xCCFFFFFF)

// ── Utility ──────────────────────────────────────────────────
val InactiveGray = Color(0xFFB0B8C4)
val InactiveDot = Color(0x337C89A8)
