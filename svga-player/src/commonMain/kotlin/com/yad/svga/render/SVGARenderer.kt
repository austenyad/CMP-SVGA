package com.yad.svga.render

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.yad.svga.model.SVGAFrameEntity
import com.yad.svga.model.SVGAShapeEntity
import com.yad.svga.model.SVGASpriteEntity
import com.yad.svga.model.SVGAVideoEntity
import com.yad.svga.model.ShapeArgs
import com.yad.svga.model.ShapeType
import com.yad.svga.path.SVGAPathParser
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Renders a single frame of an SVGA animation onto a Compose [DrawScope].
 *
 * Uses a direct rendering approach: extracts transform components (translation,
 * scale) from the 2D affine matrix and uses [DrawScope.drawImage] with explicit
 * dstOffset/dstSize. This avoids issues with Compose's [Matrix.translate]/[Matrix.scale]
 * and [canvas.concat] which behave differently from Android's native Matrix.
 */
open class SVGARenderer(
    private val videoEntity: SVGAVideoEntity,
    private val dynamicEntity: SVGADynamicEntity
) {
    // ── pre-computed sprite data (computed once at construction) ─────────

    /** Matte sprite lookup by imageKey */
    private val matteSprites: Map<String, SVGASpriteEntity> = buildMap {
        for (sprite in videoEntity.spriteList) {
            val key = sprite.imageKey
            if (key != null && key.endsWith(".matte")) {
                put(key, sprite)
            }
        }
    }

    /** Non-matte sprites in draw order */
    private val nonMatteSprites: List<SVGASpriteEntity> = videoEntity.spriteList.filter {
        it.imageKey?.endsWith(".matte") != true
    }

    /** Pre-computed matte layer begin/end flags */
    private val matteBeginIndices: BooleanArray = computeMatteBeginIndices(nonMatteSprites)
    private val matteEndIndices: BooleanArray = computeMatteEndIndices(nonMatteSprites)

    // ── reusable objects to avoid per-frame allocation ───────────────────

    /** Reusable Paint for matte / rotation-fallback rendering */
    private val reusablePaint = Paint()

    /** Reusable Matrix to avoid FloatArray(16) allocation per sprite */
    private val tempMatrix = Matrix()

    /** Cache for parsed clip paths: clipPathString → Compose Path */
    private val clipPathCache = HashMap<String, Path>()

    /** Cache for parsed shape paths: SVG d-string → Compose Path */
    private val shapePathCache = HashMap<String, Path>()

    /** Cached ScaleResult to avoid recomputing when canvas size hasn't changed */
    private var cachedCanvasWidth = 0f
    private var cachedCanvasHeight = 0f
    private var cachedContentScale: ContentScale? = null
    private var cachedScaleResult: ScaleResult? = null

    private fun getScaleResult(canvasSize: androidx.compose.ui.geometry.Size, contentScale: ContentScale): ScaleResult {
        if (canvasSize.width == cachedCanvasWidth &&
            canvasSize.height == cachedCanvasHeight &&
            contentScale === cachedContentScale
        ) {
            return cachedScaleResult!!
        }
        val result = computeScaleResult(canvasSize, videoEntity.videoSize, contentScale)
        cachedCanvasWidth = canvasSize.width
        cachedCanvasHeight = canvasSize.height
        cachedContentScale = contentScale
        cachedScaleResult = result
        return result
    }

    /**
     * Draw the frame at [frameIndex] onto [drawScope], scaling the video
     * content according to [contentScale].
     */
    fun drawFrame(drawScope: DrawScope, frameIndex: Int, contentScale: ContentScale) {
        val canvasSize = drawScope.size
        val videoSize = videoEntity.videoSize
        if (videoSize.width <= 0f || videoSize.height <= 0f) return

        val scaleResult = getScaleResult(canvasSize, contentScale)

        drawScope.drawIntoCanvas { canvas ->
            for ((index, sprite) in nonMatteSprites.withIndex()) {
                if (matteBeginIndices[index]) {
                    val layerRect = Rect(0f, 0f, canvasSize.width, canvasSize.height)
                    canvas.saveLayer(layerRect, Paint())
                }

                drawSprite(drawScope, sprite, frameIndex, scaleResult)

                if (matteEndIndices[index]) {
                    val matteKey = sprite.matteKey
                    if (matteKey != null) {
                        val matteSpriteEntity = matteSprites[matteKey]
                        if (matteSpriteEntity != null) {
                            drawMatteSprite(drawScope, matteSpriteEntity, frameIndex, scaleResult)
                        }
                    }
                    canvas.restore()
                }
            }
        }
    }

    internal fun computeMatteBeginIndices(sprites: List<SVGASpriteEntity>): BooleanArray {
        val result = BooleanArray(sprites.size)
        for (i in sprites.indices) {
            val matteKey = sprites[i].matteKey
            if (matteKey.isNullOrEmpty()) continue
            if (i == 0) {
                result[i] = true
            } else {
                val prevMatteKey = sprites[i - 1].matteKey
                if (prevMatteKey.isNullOrEmpty() || prevMatteKey != matteKey) {
                    result[i] = true
                }
            }
        }
        return result
    }

    internal fun computeMatteEndIndices(sprites: List<SVGASpriteEntity>): BooleanArray {
        val result = BooleanArray(sprites.size)
        for (i in sprites.indices) {
            val matteKey = sprites[i].matteKey
            if (matteKey.isNullOrEmpty()) continue
            if (i == sprites.size - 1) {
                result[i] = true
            } else {
                val nextMatteKey = sprites[i + 1].matteKey
                if (nextMatteKey.isNullOrEmpty() || nextMatteKey != matteKey) {
                    result[i] = true
                }
            }
        }
        return result
    }

    private fun drawMatteSprite(
        drawScope: DrawScope,
        matteSpriteEntity: SVGASpriteEntity,
        frameIndex: Int,
        scaleResult: ScaleResult
    ) {
        if (frameIndex < 0 || frameIndex >= matteSpriteEntity.frames.size) return
        val frame = matteSpriteEntity.frames[frameIndex]
        if (frame.alpha <= 0f) return

        val matteImageKey = matteSpriteEntity.imageKey ?: return
        val bitmapKey = matteImageKey.removeSuffix(".matte")
        val bitmap = videoEntity.imageMap[bitmapKey] ?: return
        val layout = frame.layout
        if (layout.width <= 0f || layout.height <= 0f) return
        if (bitmap.width <= 0 || bitmap.height <= 0) return

        // Build the Android-compatible matrix and use native canvas for matte (needs BlendMode)
        val nativeMatrix = buildNativeMatrix(frame.transform, scaleResult, layout, bitmap)

        drawScope.drawIntoCanvas { canvas ->
            canvas.save()
            canvas.concat(nativeMatrix)
            reusablePaint.alpha = frame.alpha.coerceIn(0f, 1f)
            reusablePaint.blendMode = BlendMode.DstIn
            canvas.drawImage(bitmap, Offset.Zero, reusablePaint)
            canvas.restore()
        }
    }

    // ── sprite dispatch ─────────────────────────────────────────────────

    internal fun drawSprite(
        drawScope: DrawScope,
        sprite: SVGASpriteEntity,
        frameIndex: Int,
        scaleResult: ScaleResult
    ) {
        if (frameIndex < 0 || frameIndex >= sprite.frames.size) return
        val frame = sprite.frames[frameIndex]
        if (frame.alpha <= 0f) return

        val imageKey = sprite.imageKey
        if (imageKey != null && dynamicEntity.isHidden(imageKey)) return
        if (imageKey?.endsWith(".matte") == true) return

        // Draw bitmap
        if (imageKey != null) {
            drawImage(drawScope, frame, imageKey, scaleResult)
        }

        // Compute combined matrix once for shapes + dynamic content
        val needsShapes = frame.shapes.isNotEmpty()
        val needsDynamic = imageKey != null && (
            dynamicEntity.getDynamicDrawer(imageKey) != null ||
            dynamicEntity.getDynamicText(imageKey) != null
        )

        if (needsShapes || needsDynamic) {
            val combinedMatrix = buildCombinedMatrix(frame.transform, scaleResult)
            drawScope.withTransform({
                transform(combinedMatrix)
            }) {
                if (needsShapes) {
                    lastShapePath = null
                    for (shape in frame.shapes) {
                        drawShape(this, shape, combinedMatrix)
                    }
                }
                if (needsDynamic && imageKey != null) {
                    drawDynamic(this, frame, frameIndex, imageKey, combinedMatrix)
                }
            }
        }
    }

    /**
     * Draw the bitmap for a sprite frame.
     *
     * Uses direct coordinate calculation instead of matrix concat:
     * extracts a, b, c, d, tx, ty from the 2D affine transform,
     * computes the final screen-space bounding box, and draws with
     * DrawScope.drawImage(dstOffset, dstSize).
     *
     * For sprites with rotation/skew (b != 0 or c != 0), falls back to
     * native canvas concat approach.
     */
    internal fun drawImage(
        drawScope: DrawScope,
        frame: SVGAFrameEntity,
        imageKey: String,
        scaleResult: ScaleResult
    ) {
        val bitmap = dynamicEntity.getDynamicImage(imageKey)
            ?: videoEntity.imageMap[imageKey]
            ?: return

        val layout = frame.layout
        if (layout.width <= 0f || layout.height <= 0f) return
        if (bitmap.width <= 0 || bitmap.height <= 0) return

        val alpha = frame.alpha.coerceIn(0f, 1f)
        val transform = frame.transform

        // Extract 2D affine components: a, b, c, d, tx, ty
        val a = transform[0, 0]  // scaleX (+ rotation)
        val b = transform[1, 0]  // skewY
        val c = transform[0, 1]  // skewX
        val d = transform[1, 1]  // scaleY (+ rotation)
        val tx = transform[0, 3] // translateX
        val ty = transform[1, 3] // translateY

        val hasRotationOrSkew = abs(b) > 0.001f || abs(c) > 0.001f

        if (hasRotationOrSkew) {
            // Fallback: use native canvas for rotated/skewed sprites
            val nativeMatrix = buildNativeMatrix(transform, scaleResult, layout, bitmap)
            if (!frame.clipPath.isNullOrEmpty()) {
                val clipComposePath = getOrParseClipPath(frame.clipPath)
                if (clipComposePath != null) {
                    drawScope.drawIntoCanvas { canvas ->
                        canvas.save()
                        canvas.clipPath(clipComposePath)
                        canvas.concat(nativeMatrix)
                        reusablePaint.alpha = alpha
                        reusablePaint.blendMode = BlendMode.SrcOver
                        canvas.drawImage(bitmap, Offset.Zero, reusablePaint)
                        canvas.restore()
                    }
                    return
                }
            }
            drawScope.drawIntoCanvas { canvas ->
                canvas.save()
                canvas.concat(nativeMatrix)
                reusablePaint.alpha = alpha
                reusablePaint.blendMode = BlendMode.SrcOver
                canvas.drawImage(bitmap, Offset.Zero, reusablePaint)
                canvas.restore()
            }
            return
        }

        // Fast path: no rotation/skew — use direct drawImage
        val dstLeft = scaleResult.offsetX + scaleResult.scaleX * tx
        val dstTop = scaleResult.offsetY + scaleResult.scaleY * ty
        val dstWidth = scaleResult.scaleX * layout.width * a
        val dstHeight = scaleResult.scaleY * layout.height * d

        if (dstWidth <= 0f || dstHeight <= 0f) return

        if (!frame.clipPath.isNullOrEmpty()) {
            val clipComposePath = getOrParseClipPath(frame.clipPath)
            if (clipComposePath != null) {
                drawScope.clipPath(clipComposePath) {
                    drawImage(
                        image = bitmap,
                        srcOffset = IntOffset.Zero,
                        srcSize = IntSize(bitmap.width, bitmap.height),
                        dstOffset = IntOffset(dstLeft.toInt(), dstTop.toInt()),
                        dstSize = IntSize(dstWidth.toInt(), dstHeight.toInt()),
                        alpha = alpha
                    )
                }
                return
            }
        }

        drawScope.drawImage(
            image = bitmap,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(bitmap.width, bitmap.height),
            dstOffset = IntOffset(dstLeft.toInt(), dstTop.toInt()),
            dstSize = IntSize(dstWidth.toInt(), dstHeight.toInt()),
            alpha = alpha
        )
    }

    // ── path caching helpers ────────────────────────────────────────────

    /** Get or parse and cache a clip path string into a Compose Path. */
    private fun getOrParseClipPath(clipPathStr: String): Path? {
        clipPathCache[clipPathStr]?.let { return it }
        val commands = SVGAPathParser.parse(clipPathStr)
        if (commands.isEmpty()) return null
        val path = buildComposePath(commands)
        clipPathCache[clipPathStr] = path
        return path
    }

    /** Get or parse and cache a shape SVG d-string into a Compose Path. */
    private fun getOrParseShapePath(d: String): Path? {
        shapePathCache[d]?.let { return it }
        val commands = SVGAPathParser.parse(d)
        if (commands.isEmpty()) return null
        val path = buildComposePath(commands)
        shapePathCache[d] = path
        return path
    }

    // ── matrix helpers ──────────────────────────────────────────────────

    /**
     * Build a native-compatible 4x4 matrix for canvas.concat().
     * Replicates the original Android approach:
     *   matrix = Translate * ContentScale * SpriteTransform * BitmapScale
     *
     * We construct this by directly setting the float values, avoiding
     * Compose Matrix.translate()/scale() which have unexpected behavior.
     */
    private fun buildNativeMatrix(
        spriteTransform: Matrix,
        scaleResult: ScaleResult,
        layout: Rect,
        bitmap: ImageBitmap
    ): Matrix {
        // Content scale matrix: scale then translate
        // CS = | scaleX  0      0  offsetX |
        //      | 0       scaleY 0  offsetY |
        //      | 0       0      1  0       |
        //      | 0       0      0  1       |
        val csx = scaleResult.scaleX
        val csy = scaleResult.scaleY
        val cox = scaleResult.offsetX
        val coy = scaleResult.offsetY

        // Sprite transform (2D affine)
        val sa = spriteTransform[0, 0]
        val sb = spriteTransform[1, 0]
        val sc = spriteTransform[0, 1]
        val sd = spriteTransform[1, 1]
        val stx = spriteTransform[0, 3]
        val sty = spriteTransform[1, 3]

        // Bitmap scale
        val bsx = layout.width / bitmap.width.toFloat()
        val bsy = layout.height / bitmap.height.toFloat()

        // Combined = ContentScale * SpriteTransform * BitmapScale
        // First: ST * BS (sprite transform * bitmap scale)
        val stbs00 = sa * bsx
        val stbs10 = sb * bsx
        val stbs01 = sc * bsy
        val stbs11 = sd * bsy
        // tx, ty unchanged by bitmap scale (bitmap scale has no translation)

        // Then: CS * (ST * BS)
        // | csx  0  | * | stbs00  stbs01  stx |   = | csx*stbs00  csx*stbs01  csx*stx+cox |
        // | 0  csy  |   | stbs10  stbs11  sty |     | csy*stbs10  csy*stbs11  csy*sty+coy |
        val m00 = csx * stbs00
        val m10 = csy * stbs10
        val m01 = csx * stbs01
        val m11 = csy * stbs11
        val mtx = csx * stx + cox
        val mty = csy * sty + coy

        return Matrix(
            floatArrayOf(
                m00,  m10,  0f, 0f,
                m01,  m11,  0f, 0f,
                0f,   0f,   1f, 0f,
                mtx,  mty,  0f, 1f
            )
        )
    }

    /**
     * Build a combined matrix for shape/dynamic rendering (no bitmap scale).
     * Combined = ContentScale * SpriteTransform
     */
    private fun buildCombinedMatrix(
        spriteTransform: Matrix,
        scaleResult: ScaleResult
    ): Matrix {
        val csx = scaleResult.scaleX
        val csy = scaleResult.scaleY
        val cox = scaleResult.offsetX
        val coy = scaleResult.offsetY

        val sa = spriteTransform[0, 0]
        val sb = spriteTransform[1, 0]
        val sc = spriteTransform[0, 1]
        val sd = spriteTransform[1, 1]
        val stx = spriteTransform[0, 3]
        val sty = spriteTransform[1, 3]

        return Matrix(
            floatArrayOf(
                csx * sa,  csy * sb,  0f, 0f,
                csx * sc,  csy * sd,  0f, 0f,
                0f,        0f,        1f, 0f,
                csx * stx + cox, csy * sty + coy, 0f, 1f
            )
        )
    }

    // ── shape drawing ───────────────────────────────────────────────────

    internal fun drawShape(drawScope: DrawScope, shape: SVGAShapeEntity, frameMatrix: Matrix) {
        val path: Path = when (shape.type) {
            ShapeType.SHAPE -> {
                val args = shape.args as? ShapeArgs.Path ?: return
                if (args.d.isEmpty()) return
                val cached = getOrParseShapePath(args.d) ?: return
                // Always copy: path.transform() below mutates in-place, and
                // lastShapePath / KEEP references could also mutate the cached object.
                Path().apply { addPath(cached) }
            }
            ShapeType.RECT -> {
                val args = shape.args as? ShapeArgs.RectShape ?: return
                Path().apply {
                    if (args.cornerRadius > 0f) {
                        addRoundRect(
                            RoundRect(
                                left = args.x, top = args.y,
                                right = args.x + args.width,
                                bottom = args.y + args.height,
                                cornerRadius = CornerRadius(args.cornerRadius, args.cornerRadius)
                            )
                        )
                    } else {
                        addRect(Rect(args.x, args.y, args.x + args.width, args.y + args.height))
                    }
                }
            }
            ShapeType.ELLIPSE -> {
                val args = shape.args as? ShapeArgs.Ellipse ?: return
                Path().apply {
                    addOval(Rect(
                        args.x - args.radiusX, args.y - args.radiusY,
                        args.x + args.radiusX, args.y + args.radiusY
                    ))
                }
            }
            ShapeType.KEEP -> lastShapePath ?: return
        }

        lastShapePath = path
        shape.transform?.let { path.transform(it) }

        val styles = shape.styles ?: return

        if (styles.fill != null && styles.fill != Color.Transparent) {
            drawScope.drawPath(path = path, color = styles.fill, style = Fill)
        }

        if (styles.stroke != null && styles.strokeWidth > 0f) {
            val pathEffect = styles.lineDash?.let { dash ->
                if (dash.size >= 2 && (dash[0] > 0f || dash[1] > 0f)) {
                    PathEffect.dashPathEffect(
                        intervals = floatArrayOf(
                            if (dash[0] < 1f) 1f else dash[0],
                            if (dash[1] < 0.1f) 0.1f else dash[1]
                        ),
                        phase = if (dash.size >= 3) dash[2] else 0f
                    )
                } else null
            }
            drawScope.drawPath(
                path = path, color = styles.stroke,
                style = Stroke(
                    width = styles.strokeWidth, cap = styles.lineCap,
                    join = styles.lineJoin, miter = styles.miterLimit,
                    pathEffect = pathEffect
                )
            )
        }
    }

    private var lastShapePath: Path? = null

    // ── dynamic content ─────────────────────────────────────────────────

    internal fun drawDynamic(
        drawScope: DrawScope,
        frame: SVGAFrameEntity,
        frameIndex: Int,
        imageKey: String,
        frameMatrix: Matrix
    ) {
        dynamicEntity.getDynamicDrawer(imageKey)?.invoke(drawScope, frameIndex)

        val textPair = dynamicEntity.getDynamicText(imageKey) ?: return
        val (text, textStyle) = textPair
        val layout = frame.layout
        if (layout.width <= 0f || layout.height <= 0f) return
        drawTextOnCanvas(drawScope, text, textStyle, layout)
    }
}

/**
 * Converts a list of SVG [PathCommand]s into a Compose [Path].
 */
internal fun buildComposePath(
    commands: List<com.yad.svga.path.PathCommand>
): Path {
    val path = Path()
    var currentX = 0f
    var currentY = 0f
    var lastControlX = 0f
    var lastControlY = 0f
    var lastCommand = ' '

    for (cmd in commands) {
        val a = cmd.args
        when (cmd.type) {
            'M' -> { path.moveTo(a[0], a[1]); currentX = a[0]; currentY = a[1] }
            'm' -> { currentX += a[0]; currentY += a[1]; path.moveTo(currentX, currentY) }
            'L' -> { path.lineTo(a[0], a[1]); currentX = a[0]; currentY = a[1] }
            'l' -> { currentX += a[0]; currentY += a[1]; path.lineTo(currentX, currentY) }
            'H' -> { currentX = a[0]; path.lineTo(currentX, currentY) }
            'h' -> { currentX += a[0]; path.lineTo(currentX, currentY) }
            'V' -> { currentY = a[0]; path.lineTo(currentX, currentY) }
            'v' -> { currentY += a[0]; path.lineTo(currentX, currentY) }
            'C' -> {
                path.cubicTo(a[0], a[1], a[2], a[3], a[4], a[5])
                lastControlX = a[2]; lastControlY = a[3]; currentX = a[4]; currentY = a[5]
            }
            'c' -> {
                val x1 = currentX + a[0]; val y1 = currentY + a[1]
                val x2 = currentX + a[2]; val y2 = currentY + a[3]
                val x = currentX + a[4]; val y = currentY + a[5]
                path.cubicTo(x1, y1, x2, y2, x, y)
                lastControlX = x2; lastControlY = y2; currentX = x; currentY = y
            }
            'S' -> {
                val rx = if (lastCommand in "CcSs") 2 * currentX - lastControlX else currentX
                val ry = if (lastCommand in "CcSs") 2 * currentY - lastControlY else currentY
                path.cubicTo(rx, ry, a[0], a[1], a[2], a[3])
                lastControlX = a[0]; lastControlY = a[1]; currentX = a[2]; currentY = a[3]
            }
            's' -> {
                val rx = if (lastCommand in "CcSs") 2 * currentX - lastControlX else currentX
                val ry = if (lastCommand in "CcSs") 2 * currentY - lastControlY else currentY
                val x2 = currentX + a[0]; val y2 = currentY + a[1]
                val x = currentX + a[2]; val y = currentY + a[3]
                path.cubicTo(rx, ry, x2, y2, x, y)
                lastControlX = x2; lastControlY = y2; currentX = x; currentY = y
            }
            'Q' -> {
                path.quadraticBezierTo(a[0], a[1], a[2], a[3])
                lastControlX = a[0]; lastControlY = a[1]; currentX = a[2]; currentY = a[3]
            }
            'q' -> {
                val x1 = currentX + a[0]; val y1 = currentY + a[1]
                val x = currentX + a[2]; val y = currentY + a[3]
                path.quadraticBezierTo(x1, y1, x, y)
                lastControlX = x1; lastControlY = y1; currentX = x; currentY = y
            }
            'A', 'a' -> {
                val isRelative = cmd.type == 'a'
                val endX = if (isRelative) currentX + a[5] else a[5]
                val endY = if (isRelative) currentY + a[6] else a[6]
                path.lineTo(endX, endY); currentX = endX; currentY = endY
            }
            'Z', 'z' -> path.close()
        }
        lastCommand = cmd.type
    }
    return path
}
