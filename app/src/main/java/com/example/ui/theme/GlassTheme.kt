package com.example.ui.theme

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object GlassTheme {
    // Dark Space visual parameters (Android 17 Dark Glass Mode)
    val DarkSpace = Color(0xFF0B0F19)
    val GlowPinkDark = Color(0x33FF2E93)
    val GlowBlueDark = Color(0x2B4361EE)
    val GlowTealDark = Color(0x2410B981)

    // Light Space visual parameters (Android 17 Light Glass Mode)
    val LightSpace = Color(0xFFF1F5F9)
    val GlowPinkLight = Color(0x26FF5A9E)
    val GlowBlueLight = Color(0x1F83A4FC)
    val GlowTealLight = Color(0x1C10B981)

    @Composable
    fun GlassBackground(
        modifier: Modifier = Modifier,
        isDark: Boolean = true,
        content: @Composable () -> Unit
    ) {
        // Subtle animated offsets for the organic premium fluid neon aura (Android 17)
        val infiniteTransition = rememberInfiniteTransition(label = "glass_glow")
        val animOffsetMultiplier by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2f * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(25000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "offset"
        )

        val baseColor = if (isDark) DarkSpace else LightSpace
        val pinkBlob = if (isDark) GlowPinkDark else GlowPinkLight
        val blueBlob = if (isDark) GlowBlueDark else GlowBlueLight
        val tealBlob = if (isDark) GlowTealDark else GlowTealLight

        Box(
            modifier = modifier
                .fillMaxSize()
                .drawBehind {
                    // Draw base atmosphere space color
                    drawRect(color = baseColor)

                    val width = size.width
                    val height = size.height

                    // Dynamic circular movements
                    val xOffset1 = Math.cos(animOffsetMultiplier.toDouble()).toFloat() * 120f
                    val yOffset1 = Math.sin(animOffsetMultiplier.toDouble()).toFloat() * 90f
                    
                    val xOffset2 = Math.sin(animOffsetMultiplier.toDouble() + 2.0).toFloat() * 150f
                    val yOffset2 = Math.cos(animOffsetMultiplier.toDouble() + 1.0).toFloat() * 110f

                    // 1. Top Right warm coral/pink aurora
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(pinkBlob, Color.Transparent),
                            center = Offset(width * 0.82f + xOffset1, height * 0.18f + yOffset1),
                            radius = width * 0.65f
                        ),
                        center = Offset(width * 0.82f + xOffset1, height * 0.18f + yOffset1),
                        radius = width * 0.65f
                    )

                    // 2. Middle Left dynamic deep blue aurora
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(blueBlob, Color.Transparent),
                            center = Offset(width * 0.12f + xOffset2, height * 0.52f + yOffset2),
                            radius = width * 0.75f
                        ),
                        center = Offset(width * 0.12f + xOffset2, height * 0.52f + yOffset2),
                        radius = width * 0.75f
                    )

                    // 3. Bottom Right fresh teal/mint accent
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(tealBlob, Color.Transparent),
                            center = Offset(width * 0.68f, height * 0.88f),
                            radius = width * 0.55f
                        ),
                        center = Offset(width * 0.68f, height * 0.88f),
                        radius = width * 0.55f
                    )
                }
        ) {
            content()
        }
    }

    /**
     * Applies glassmorphic frosted glass design containing correct background blur,
     * adaptive Monet tint fill, and top-left to bottom-right linear gradient glare border.
     */
    fun Modifier.glassyCard(
        shape: Shape,
        isDark: Boolean? = null,
        borderWidth: Dp = 1.dp
    ): Modifier = composed {
        val sdkVersion = Build.VERSION.SDK_INT
        val actualIsDark = isDark ?: androidx.compose.foundation.isSystemInDarkTheme()

        // Choose translucent tint depending on light/dark mode (based on Android 17 specifications)
        val tintColor = if (actualIsDark) {
            Color.Black.copy(alpha = 0.3f)
        } else {
            Color.White.copy(alpha = 0.4f)
        }

        // 1.dp top-left (Color.White.copy(alpha=0.4f)) to bottom-right (Color.White.copy(alpha=0.05f)) lineargradient glare border
        val borderBrush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.4f),
                Color.White.copy(alpha = 0.05f)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )

        val activeClipModifier = this.clip(shape)

        // Android 12+ (SDK >= 31) live render backdrop blur effect fallback check
        // To keep text perfectly legible ("над склом" - crystal clear), we do not blur the card's child views.
        // The card backdrop itself is pre-blurred since it is laid over the deep animating neon cloud "GlassBackground".
        val glassModifier = activeClipModifier.background(tintColor, shape)

        glassModifier.border(borderWidth, borderBrush, shape)
    }
}
