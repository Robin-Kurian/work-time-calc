package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.meshGradientBackground(): Modifier {
    val colors = AppTheme.colors
    return background(
        Brush.verticalGradient(listOf(colors.bgGradientStart, colors.bgGradientEnd))
    )
}

@Composable
fun Modifier.glassSurface(
    shape: Shape = RoundedCornerShape(16.dp),
    fillAlpha: Float? = null,
    borderAlpha: Float? = null,
    elevation: Dp = 6.dp
): Modifier {
    val colors = AppTheme.colors
    val fill = fillAlpha?.let { colors.glassFill.copy(alpha = it) } ?: colors.glassFill
    val border = borderAlpha?.let { colors.glassBorder.copy(alpha = it) } ?: colors.glassBorder
    val shadowElevation = if (colors.bgBase.red + colors.bgBase.green + colors.bgBase.blue < 0.35f) {
        elevation + 2.dp
    } else {
        elevation
    }
    return this
        .shadow(shadowElevation, shape, ambientColor = colors.glassShadow, spotColor = colors.glassShadow)
        .clip(shape)
        .background(fill, shape)
        .border(1.dp, border, shape)
}

@Composable
fun Modifier.sheetCardSurface(
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: Dp = 4.dp
): Modifier {
    val colors = AppTheme.colors
    return this
        .shadow(elevation, shape, ambientColor = colors.glassShadow, spotColor = colors.glassShadow)
        .clip(shape)
        .background(colors.sheetCardFill, shape)
        .border(1.dp, colors.glassBorderSubtle, shape)
}

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .meshGradientBackground(),
        content = content
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    fillAlpha: Float? = null,
    elevation: Dp = 6.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.glassSurface(
            shape = shape,
            fillAlpha = fillAlpha,
            elevation = elevation
        ),
        content = content
    )
}
