package com.yad.svga.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import kotlin.math.max
import kotlin.math.min

/**
 * Result of content scale calculation: a uniform scale factor and the offset
 * needed to center the content within the canvas.
 */
data class ScaleResult(
    val scaleX: Float,
    val scaleY: Float,
    val offsetX: Float,
    val offsetY: Float
)

/**
 * Calculates scale factors and centering offset for the given [contentScale] mode.
 *
 * Supported modes:
 * - [ContentScale.Fit]  – scale uniformly so the entire video fits inside the canvas
 * - [ContentScale.Fill] – scale non-uniformly to fill the canvas exactly (may distort)
 * - [ContentScale.Crop] – scale uniformly so the canvas is fully covered (may clip)
 * - [ContentScale.None] – no scaling, center the content
 * - [ContentScale.Inside] – same as Fit when video is larger, otherwise no scaling
 * - [ContentScale.FillWidth] – scale to match canvas width, maintain aspect ratio
 * - [ContentScale.FillHeight] – scale to match canvas height, maintain aspect ratio
 */
fun computeScaleResult(
    canvasSize: Size,
    videoSize: Size,
    contentScale: ContentScale
): ScaleResult {
    if (videoSize.width <= 0f || videoSize.height <= 0f ||
        canvasSize.width <= 0f || canvasSize.height <= 0f
    ) {
        return ScaleResult(1f, 1f, 0f, 0f)
    }

    val scaleFactor = contentScale.computeScaleFactor(videoSize, canvasSize)
    val scaleX = scaleFactor.scaleX
    val scaleY = scaleFactor.scaleY

    val scaledWidth = videoSize.width * scaleX
    val scaledHeight = videoSize.height * scaleY
    val offsetX = (canvasSize.width - scaledWidth) / 2f
    val offsetY = (canvasSize.height - scaledHeight) / 2f

    return ScaleResult(scaleX, scaleY, offsetX, offsetY)
}
