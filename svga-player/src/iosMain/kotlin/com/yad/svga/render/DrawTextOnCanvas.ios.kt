package com.yad.svga.render

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import org.jetbrains.skia.Color4f
import org.jetbrains.skia.Font
import org.jetbrains.skia.TextLine

actual fun drawTextOnCanvas(
    drawScope: DrawScope,
    text: String,
    textStyle: SVGATextStyle,
    layout: Rect
) {
    drawScope.drawIntoCanvas { canvas ->
        val font = Font(null, textStyle.fontSize)
        val skPaint = org.jetbrains.skia.Paint().apply {
            color4f = Color4f(
                r = textStyle.color.red,
                g = textStyle.color.green,
                b = textStyle.color.blue,
                a = textStyle.color.alpha
            )
            isAntiAlias = true
        }
        val textLine = TextLine.make(text, font)
        val textX = layout.left + (layout.width - textLine.width) / 2f
        val textY = layout.top + (layout.height + textLine.capHeight) / 2f
        canvas.nativeCanvas.drawTextLine(textLine, textX, textY, skPaint)
    }
}
