package com.yad.svga.model

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import com.yad.svga.proto.AudioEntity
import com.yad.svga.proto.FrameEntity
import com.yad.svga.proto.MovieEntity
import com.yad.svga.proto.ShapeEntity
import com.yad.svga.proto.SpriteEntity
import com.yad.svga.proto.Transform

fun MovieEntity.toVideoEntity(imageMap: Map<String, ImageBitmap>): SVGAVideoEntity {
    return SVGAVideoEntity(
        version = this.version ?: "",
        videoSize = Size(
            this.params?.viewBoxWidth ?: 0f,
            this.params?.viewBoxHeight ?: 0f
        ),
        fps = this.params?.fps ?: 20,
        frames = this.params?.frames ?: 0,
        spriteList = this.sprites.map { it.toSpriteEntity() },
        imageMap = imageMap,
        audioList = this.audios.map { it.toAudioEntity() }
    )
}

fun SpriteEntity.toSpriteEntity(): SVGASpriteEntity {
    return SVGASpriteEntity(
        imageKey = this.imageKey,
        matteKey = this.matteKey,
        frames = this.frames.map { it.toFrameEntity() }
    )
}

fun FrameEntity.toFrameEntity(): SVGAFrameEntity {
    return SVGAFrameEntity(
        alpha = this.alpha ?: 0f,
        layout = this.layout.let { l ->
            Rect(
                left = l?.x ?: 0f,
                top = l?.y ?: 0f,
                right = (l?.x ?: 0f) + (l?.width ?: 0f),
                bottom = (l?.y ?: 0f) + (l?.height ?: 0f)
            )
        },
        transform = this.transform.toMatrix(),
        clipPath = this.clipPath,
        shapes = this.shapes.map { it.toShapeEntity() }
    )
}

fun ShapeEntity.toShapeEntity(): SVGAShapeEntity {
    val shapeType = when (this.type) {
        ShapeEntity.ShapeType.SHAPE -> ShapeType.SHAPE
        ShapeEntity.ShapeType.RECT -> ShapeType.RECT
        ShapeEntity.ShapeType.ELLIPSE -> ShapeType.ELLIPSE
        ShapeEntity.ShapeType.KEEP -> ShapeType.KEEP
        else -> ShapeType.SHAPE
    }

    val args: ShapeArgs = when (shapeType) {
        ShapeType.RECT -> {
            val r = this.rect
            ShapeArgs.RectShape(
                x = r?.x ?: 0f,
                y = r?.y ?: 0f,
                width = r?.width ?: 0f,
                height = r?.height ?: 0f,
                cornerRadius = r?.cornerRadius ?: 0f
            )
        }
        ShapeType.ELLIPSE -> {
            val e = this.ellipse
            ShapeArgs.Ellipse(
                x = e?.x ?: 0f,
                y = e?.y ?: 0f,
                radiusX = e?.radiusX ?: 0f,
                radiusY = e?.radiusY ?: 0f
            )
        }
        else -> ShapeArgs.Path(d = this.shape?.d ?: "")
    }

    return SVGAShapeEntity(
        type = shapeType,
        args = args,
        styles = this.styles?.toShapeStyles(),
        transform = this.transform?.toMatrix()
    )
}

fun ShapeEntity.ShapeStyle.toShapeStyles(): ShapeStyles {
    return ShapeStyles(
        fill = this.fill?.toColor(),
        stroke = this.stroke?.toColor(),
        strokeWidth = this.strokeWidth ?: 0f,
        lineCap = when (this.lineCap) {
            ShapeEntity.ShapeStyle.LineCap.ROUND -> StrokeCap.Round
            ShapeEntity.ShapeStyle.LineCap.SQUARE -> StrokeCap.Square
            else -> StrokeCap.Butt
        },
        lineJoin = when (this.lineJoin) {
            ShapeEntity.ShapeStyle.LineJoin.ROUND -> StrokeJoin.Round
            ShapeEntity.ShapeStyle.LineJoin.BEVEL -> StrokeJoin.Bevel
            else -> StrokeJoin.Miter
        },
        miterLimit = this.miterLimit ?: 0f,
        lineDash = buildLineDash(this.lineDashI, this.lineDashII, this.lineDashIII)
    )
}

fun ShapeEntity.RGBAColor.toColor(): Color {
    return Color(
        red = (this.r ?: 0f).coerceIn(0f, 1f),
        green = (this.g ?: 0f).coerceIn(0f, 1f),
        blue = (this.b ?: 0f).coerceIn(0f, 1f),
        alpha = (this.a ?: 1f).coerceIn(0f, 1f)
    )
}

fun AudioEntity.toAudioEntity(): SVGAAudioEntity {
    return SVGAAudioEntity(
        audioKey = this.audioKey,
        startFrame = this.startFrame ?: 0,
        endFrame = this.endFrame ?: 0,
        startTime = this.startTime ?: 0,
        totalTime = this.totalTime ?: 0
    )
}

/**
 * Convert a proto Transform (2D affine: a, b, c, d, tx, ty) to a Compose 4x4 Matrix.
 *
 * 2D affine matrix:
 * | a  c  tx |
 * | b  d  ty |
 * | 0  0  1  |
 *
 * Maps to 4x4 column-major Matrix:
 * | a  c  0  tx |
 * | b  d  0  ty |
 * | 0  0  1  0  |
 * | 0  0  0  1  |
 */
fun Transform?.toMatrix(): Matrix {
    if (this == null) return Matrix() // identity
    return Matrix().apply {
        // Compose Matrix uses row, column indexing: values[row + col * 4]
        this[0, 0] = this@toMatrix.a ?: 1f
        this[0, 1] = this@toMatrix.c ?: 0f
        this[0, 3] = this@toMatrix.tx ?: 0f
        this[1, 0] = this@toMatrix.b ?: 0f
        this[1, 1] = this@toMatrix.d ?: 1f
        this[1, 3] = this@toMatrix.ty ?: 0f
        // [2,2] = 1f and [3,3] = 1f are already set by Matrix() default (identity)
    }
}

private fun buildLineDash(i: Float?, ii: Float?, iii: Float?): FloatArray? {
    if (i == null && ii == null && iii == null) return null
    return floatArrayOf(i ?: 0f, ii ?: 0f, iii ?: 0f)
}
