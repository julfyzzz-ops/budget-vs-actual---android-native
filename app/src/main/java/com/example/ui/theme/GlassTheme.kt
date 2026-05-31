package com.example.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    // Elegant neon & luxury colors for Android 17 visual theme
    val GlowPink = Color(0x33FF2E93)
    val GlowBlue = Color(0x2B4361EE)
    val GlowTeal = Color(0x2410B981)
    val DarkSpace = Color(0xFF0B0F19)

    // Glass frosted colors
    val GlassBgLight = Color(0x1CFFFFFF) // Light reflection on top
    val GlassBgDark = Color(0x29111827)  // Translucent dark center
    val GlassBorder = Color(0x33FFFFFF)  // 20% white border for sharp glare reflection
    
    @Composable
    fun GlassBackground(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        // Subtle animate offsets of glowing blobs for that organic premium feel (Android 17)
        val infiniteTransition = rememberInfiniteTransition(label = "glass_glow")
        val animOffsetMultiplier by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2f * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "offset"
        )

        Box(
            modifier = modifier
                .fillMaxSize()
                .drawBehind {
                    // Draw Dark Space base
                    drawRect(color = DarkSpace)

                    // Draw soft glow blobs based on trig offsets for modern fluid feel
                    val width = size.width
                    val height = size.height

                    val xOffset1 = Math.cos(animOffsetMultiplier.toDouble()).toFloat() * 100f
                    val yOffset1 = Math.sin(animOffsetMultiplier.toDouble()).toFloat() * 80f
                    
                    val xOffset2 = Math.sin(animOffsetMultiplier.toDouble() + 2.0).toFloat() * 120f
                    val yOffset2 = Math.cos(animOffsetMultiplier.toDouble() + 1.0).toFloat() * 100f

                    // 1. Top Right pink/purple glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GlowPink, Color.Transparent),
                            center = Offset(width * 0.8f + xOffset1, height * 0.2f + yOffset1),
                            radius = width * 0.6f
                        ),
                        center = Offset(width * 0.8f + xOffset1, height * 0.2f + yOffset1),
                        radius = width * 0.6f
                    )

                    // 2. Middle Left royal blue glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GlowBlue, Color.Transparent),
                            center = Offset(width * 0.1f + xOffset2, height * 0.5f + yOffset2),
                            radius = width * 0.7f
                        ),
                        center = Offset(width * 0.1f + xOffset2, height * 0.5f + yOffset2),
                        radius = width * 0.7f
                    )

                    // 3. Bottom Right green-teal glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GlowTeal, Color.Transparent),
                            center = Offset(width * 0.7f, height * 0.85f),
                            radius = width * 0.5f
                        ),
                        center = Offset(width * 0.7f, height * 0.85f),
                        radius = width * 0.5f
                    )
                }
        ) {
            content()
        }
    }

    /**
     * Applies glassmorphism border and background brush
     */
    fun Modifier.glassyCard(
        shape: Shape,
        borderWidth: Dp = 1.dp
    ): Modifier {
        return this
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(GlassBgLight, GlassBgDark)
                )
            )
            .border(borderWidth, GlassBorder, shape)
    }
}
