package com.yad.svga.render

import androidx.compose.ui.graphics.Color

/**
 * Text styling configuration for dynamic text overlays in SVGA animations.
 */
data class SVGATextStyle(
    val color: Color = Color.Black,
    val fontSize: Float = 14f
)
