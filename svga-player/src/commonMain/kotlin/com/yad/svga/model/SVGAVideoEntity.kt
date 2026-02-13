package com.yad.svga.model

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin

data class SVGAVideoEntity(
    val version: String,
    val videoSize: Size,
    val fps: Int,
    val frames: Int,
    val spriteList: List<SVGASpriteEntity>,
    val imageMap: Map<String, ImageBitmap>,
    val audioList: List<SVGAAudioEntity>
)

data class SVGASpriteEntity(
    val imageKey: String?,
    val matteKey: String?,
    val frames: List<SVGAFrameEntity>
)

data class SVGAFrameEntity(
    val alpha: Float,
    val layout: Rect,
    val transform: Matrix,
    val clipPath: String?,
    val shapes: List<SVGAShapeEntity>
)

data class SVGAShapeEntity(
    val type: ShapeType,
    val args: ShapeArgs,
    val styles: ShapeStyles?,
    val transform: Matrix?
)

enum class ShapeType { SHAPE, RECT, ELLIPSE, KEEP }

sealed class ShapeArgs {
    data class Path(val d: String) : ShapeArgs()
    data class RectShape(
        val x: Float, val y: Float,
        val width: Float, val height: Float,
        val cornerRadius: Float
    ) : ShapeArgs()
    data class Ellipse(
        val x: Float, val y: Float,
        val radiusX: Float, val radiusY: Float
    ) : ShapeArgs()
}

data class ShapeStyles(
    val fill: Color?,
    val stroke: Color?,
    val strokeWidth: Float,
    val lineCap: StrokeCap,
    val lineJoin: StrokeJoin,
    val miterLimit: Float,
    val lineDash: FloatArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ShapeStyles) return false
        return fill == other.fill &&
            stroke == other.stroke &&
            strokeWidth == other.strokeWidth &&
            lineCap == other.lineCap &&
            lineJoin == other.lineJoin &&
            miterLimit == other.miterLimit &&
            lineDash.contentEquals(other.lineDash)
    }

    override fun hashCode(): Int {
        var result = fill.hashCode()
        result = 31 * result + stroke.hashCode()
        result = 31 * result + strokeWidth.hashCode()
        result = 31 * result + lineCap.hashCode()
        result = 31 * result + lineJoin.hashCode()
        result = 31 * result + miterLimit.hashCode()
        result = 31 * result + (lineDash?.contentHashCode() ?: 0)
        return result
    }
}

data class SVGAAudioEntity(
    val audioKey: String?,
    val startFrame: Int,
    val endFrame: Int,
    val startTime: Int,
    val totalTime: Int
)
