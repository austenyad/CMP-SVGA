package com.yad.svga.render

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas

actual fun drawTextOnCanvas(
    drawScope: DrawScope,
    text: String,
    textStyle: SVGATextStyle,
    layout: Rect
) {
    drawScope.drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(
                (textStyle.color.alpha * 255).toInt(),
                (textStyle.color.red * 255).toInt(),
                (textStyle.color.green * 255).toInt(),
                (textStyle.color.blue * 255).toInt()
            )
            textSize = textStyle.fontSize
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val fontMetrics = paint.fontMetrics
        val textX = layout.left + layout.width / 2f
        val textY = layout.top + (layout.height - fontMetrics.ascent - fontMetrics.descent) / 2f
        canvas.nativeCanvas.drawText(text, textX, textY, paint)
    }
}
