package com.yad.svga.render

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Platform-specific text drawing onto a Compose [DrawScope].
 *
 * Draws [text] centered within the given [layout] rectangle using the
 * specified [textStyle] (color, fontSize).
 *
 * On Skia-backed platforms (Desktop, iOS via Compose Multiplatform) this
 * accesses the native Skia canvas for text rendering.
 * On Android it uses the Android-native canvas text API.
 */
expect fun drawTextOnCanvas(
    drawScope: DrawScope,
    text: String,
    textStyle: SVGATextStyle,
    layout: Rect
)
