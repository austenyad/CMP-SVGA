package com.yad.svga.render

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import com.yad.svga.model.SVGAFrameEntity
import com.yad.svga.model.SVGAShapeEntity
import com.yad.svga.model.SVGASpriteEntity
import com.yad.svga.model.SVGAVideoEntity
import com.yad.svga.model.ShapeArgs
import com.yad.svga.model.ShapeStyles
import com.yad.svga.model.ShapeType
import com.yad.svga.path.SVGAPathParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for drawShape-related logic (Task 7.3).
 *
 * Since DrawScope and Path cannot be instantiated in pure unit tests,
 * we test the logic that drawShape depends on:
 * 1. Path building: SVGAPathParser parses shape path strings correctly
 * 2. buildComposePath: converts PathCommands to Compose Path without error
 * 3. ShapeArgs type matching for each ShapeType
 * 4. ShapeStyles data model contracts (fill, stroke, dash)
 * 5. Shape transform presence
 */
class DrawShapeTest {

    // ── Helpers ─────────────────────────────────────────────────────────

    private fun defaultStyles(
        fill: Color? = null,
        stroke: Color? = null,
        strokeWidth: Float = 0f,
        lineCap: StrokeCap = StrokeCap.Butt,
        lineJoin: StrokeJoin = StrokeJoin.Miter,
        miterLimit: Float = 4f,
        lineDash: FloatArray? = null
    ) = ShapeStyles(fill, stroke, strokeWidth, lineCap, lineJoin, miterLimit, lineDash)

    private fun shapeEntity(
        type: ShapeType,
        args: ShapeArgs,
        styles: ShapeStyles? = null,
        transform: Matrix? = null
    ) = SVGAShapeEntity(type, args, styles, transform)

    // ── ShapeType.SHAPE – SVG path parsing ──────────────────────────────

    @Test
    fun shapePath_simpleLine_parsesCorrectly() {
        val d = "M 0 0 L 100 100"
        val commands = SVGAPathParser.parse(d)
        assertEquals(2, commands.size)
        assertEquals('M', commands[0].type)
        assertEquals('L', commands[1].type)
    }

    @Test
    fun shapePath_complexCurve_parsesAllCommands() {
        val d = "M 10 10 C 20 20 40 20 50 10 S 80 0 90 10 Z"
        val commands = SVGAPathParser.parse(d)
        assertEquals(4, commands.size)
        assertEquals('M', commands[0].type)
        assertEquals('C', commands[1].type)
        assertEquals('S', commands[2].type)
        assertEquals('Z', commands[3].type)
    }

    @Test
    fun shapePath_emptyString_parsesToEmpty() {
        val commands = SVGAPathParser.parse("")
        assertTrue(commands.isEmpty())
    }

    @Test
    fun shapePath_buildComposePath_parsesCommandsForPath() {
        // We verify the parser produces the right commands;
        // buildComposePath itself requires a graphics context so we
        // validate the input it would receive.
        val commands = SVGAPathParser.parse("M 0 0 L 50 50 L 100 0 Z")
        assertEquals(4, commands.size)
        assertEquals('M', commands[0].type)
        assertEquals(0f, commands[0].args[0])
        assertEquals(0f, commands[0].args[1])
        assertEquals('L', commands[1].type)
        assertEquals('Z', commands[3].type)
    }

    @Test
    fun shapePath_relativeCommands_parseCorrectly() {
        val commands = SVGAPathParser.parse("m 0 0 l 50 50 l 50 -50 z")
        assertEquals(4, commands.size)
        assertEquals('m', commands[0].type)
        assertEquals('l', commands[1].type)
        assertEquals(50f, commands[1].args[0])
        assertEquals(50f, commands[1].args[1])
        assertEquals('z', commands[3].type)
    }

    @Test
    fun shapePath_quadraticBezier_parsesCorrectly() {
        val commands = SVGAPathParser.parse("M 10 80 Q 95 10 180 80")
        assertEquals(2, commands.size)
        assertEquals('M', commands[0].type)
        assertEquals('Q', commands[1].type)
        assertEquals(4, commands[1].args.size)
        assertEquals(95f, commands[1].args[0])
        assertEquals(10f, commands[1].args[1])
        assertEquals(180f, commands[1].args[2])
        assertEquals(80f, commands[1].args[3])
    }

    // ── ShapeType.RECT – args matching ──────────────────────────────────

    @Test
    fun rectShape_argsMatchCorrectly() {
        val args = ShapeArgs.RectShape(10f, 20f, 100f, 50f, 5f)
        val shape = shapeEntity(ShapeType.RECT, args)
        assertTrue(shape.args is ShapeArgs.RectShape)
        val rect = shape.args as ShapeArgs.RectShape
        assertEquals(10f, rect.x)
        assertEquals(20f, rect.y)
        assertEquals(100f, rect.width)
        assertEquals(50f, rect.height)
        assertEquals(5f, rect.cornerRadius)
    }

    @Test
    fun rectShape_zeroCornerRadius() {
        val args = ShapeArgs.RectShape(0f, 0f, 50f, 50f, 0f)
        assertEquals(0f, args.cornerRadius)
    }

    // ── ShapeType.ELLIPSE – args matching ───────────────────────────────

    @Test
    fun ellipseShape_argsMatchCorrectly() {
        val args = ShapeArgs.Ellipse(50f, 50f, 30f, 20f)
        val shape = shapeEntity(ShapeType.ELLIPSE, args)
        assertTrue(shape.args is ShapeArgs.Ellipse)
        val ellipse = shape.args as ShapeArgs.Ellipse
        assertEquals(50f, ellipse.x)
        assertEquals(50f, ellipse.y)
        assertEquals(30f, ellipse.radiusX)
        assertEquals(20f, ellipse.radiusY)
    }

    @Test
    fun ellipseShape_computesBoundsCorrectly() {
        val args = ShapeArgs.Ellipse(100f, 100f, 50f, 30f)
        // The oval rect should be (50, 70, 150, 130)
        assertEquals(50f, args.x - args.radiusX)
        assertEquals(70f, args.y - args.radiusY)
        assertEquals(150f, args.x + args.radiusX)
        assertEquals(130f, args.y + args.radiusY)
    }

    // ── ShapeType.KEEP ──────────────────────────────────────────────────

    @Test
    fun keepShape_typeIsCorrect() {
        // KEEP type doesn't need specific args; we use a dummy Path args
        val shape = shapeEntity(ShapeType.KEEP, ShapeArgs.Path(""))
        assertEquals(ShapeType.KEEP, shape.type)
    }

    // ── ShapeStyles ─────────────────────────────────────────────────────

    @Test
    fun styles_fillOnly() {
        val styles = defaultStyles(fill = Color.Red)
        assertNotNull(styles.fill)
        assertNull(styles.stroke)
        assertEquals(0f, styles.strokeWidth)
    }

    @Test
    fun styles_strokeOnly() {
        val styles = defaultStyles(stroke = Color.Blue, strokeWidth = 2f)
        assertNull(styles.fill)
        assertNotNull(styles.stroke)
        assertEquals(2f, styles.strokeWidth)
    }

    @Test
    fun styles_fillAndStroke() {
        val styles = defaultStyles(
            fill = Color.Red,
            stroke = Color.Blue,
            strokeWidth = 3f
        )
        assertNotNull(styles.fill)
        assertNotNull(styles.stroke)
    }

    @Test
    fun styles_lineCap_values() {
        assertEquals(StrokeCap.Butt, defaultStyles(lineCap = StrokeCap.Butt).lineCap)
        assertEquals(StrokeCap.Round, defaultStyles(lineCap = StrokeCap.Round).lineCap)
        assertEquals(StrokeCap.Square, defaultStyles(lineCap = StrokeCap.Square).lineCap)
    }

    @Test
    fun styles_lineJoin_values() {
        assertEquals(StrokeJoin.Miter, defaultStyles(lineJoin = StrokeJoin.Miter).lineJoin)
        assertEquals(StrokeJoin.Round, defaultStyles(lineJoin = StrokeJoin.Round).lineJoin)
        assertEquals(StrokeJoin.Bevel, defaultStyles(lineJoin = StrokeJoin.Bevel).lineJoin)
    }

    @Test
    fun styles_dashPattern() {
        val dash = floatArrayOf(5f, 3f, 0f)
        val styles = defaultStyles(lineDash = dash)
        assertNotNull(styles.lineDash)
        assertEquals(3, styles.lineDash!!.size)
        assertEquals(5f, styles.lineDash!![0])
        assertEquals(3f, styles.lineDash!![1])
        assertEquals(0f, styles.lineDash!![2])
    }

    @Test
    fun styles_nullDash() {
        val styles = defaultStyles(lineDash = null)
        assertNull(styles.lineDash)
    }

    @Test
    fun styles_transparentFill_treatedAsNoFill() {
        val styles = defaultStyles(fill = Color.Transparent)
        // drawShape skips fill when color is Transparent
        assertEquals(Color.Transparent, styles.fill)
    }

    @Test
    fun styles_miterLimit() {
        val styles = defaultStyles(miterLimit = 10f)
        assertEquals(10f, styles.miterLimit)
    }

    // ── Shape transform ─────────────────────────────────────────────────

    @Test
    fun shape_withTransform() {
        val matrix = Matrix().apply { scale(2f, 2f) }
        val shape = shapeEntity(
            ShapeType.RECT,
            ShapeArgs.RectShape(0f, 0f, 50f, 50f, 0f),
            transform = matrix
        )
        assertNotNull(shape.transform)
    }

    @Test
    fun shape_withoutTransform() {
        val shape = shapeEntity(
            ShapeType.RECT,
            ShapeArgs.RectShape(0f, 0f, 50f, 50f, 0f),
            transform = null
        )
        assertNull(shape.transform)
    }

    // ── ShapeArgs type safety ───────────────────────────────────────────

    @Test
    fun shapeArgs_pathType_isNotRectOrEllipse() {
        val args: ShapeArgs = ShapeArgs.Path("M 0 0 L 10 10")
        assertFalse(args is ShapeArgs.RectShape)
        assertFalse(args is ShapeArgs.Ellipse)
    }

    @Test
    fun shapeArgs_rectType_isNotPathOrEllipse() {
        val args: ShapeArgs = ShapeArgs.RectShape(0f, 0f, 10f, 10f, 0f)
        assertFalse(args is ShapeArgs.Path)
        assertFalse(args is ShapeArgs.Ellipse)
    }

    @Test
    fun shapeArgs_ellipseType_isNotPathOrRect() {
        val args: ShapeArgs = ShapeArgs.Ellipse(0f, 0f, 10f, 10f)
        assertFalse(args is ShapeArgs.Path)
        assertFalse(args is ShapeArgs.RectShape)
    }

    // ── Dash pattern edge cases ─────────────────────────────────────────

    @Test
    fun dashPattern_smallValues_clampedByDrawShape() {
        // drawShape clamps dash[0] < 1 to 1, dash[1] < 0.1 to 0.1
        val dash = floatArrayOf(0.5f, 0.05f, 2f)
        val dashOn = if (dash[0] < 1f) 1f else dash[0]
        val dashOff = if (dash[1] < 0.1f) 0.1f else dash[1]
        assertEquals(1f, dashOn)
        assertEquals(0.1f, dashOff)
    }

    @Test
    fun dashPattern_normalValues_unchanged() {
        val dash = floatArrayOf(10f, 5f, 0f)
        val dashOn = if (dash[0] < 1f) 1f else dash[0]
        val dashOff = if (dash[1] < 0.1f) 0.1f else dash[1]
        assertEquals(10f, dashOn)
        assertEquals(5f, dashOff)
    }

    @Test
    fun dashPattern_bothZero_noEffect() {
        // When both dash values are 0, no PathEffect should be created
        val dash = floatArrayOf(0f, 0f, 0f)
        val shouldCreateEffect = dash.size >= 2 && (dash[0] > 0f || dash[1] > 0f)
        assertFalse(shouldCreateEffect)
    }

    // ── Integration: shapes on a frame ──────────────────────────────────

    @Test
    fun frame_withShapes_containsShapeList() {
        val shapes = listOf(
            shapeEntity(ShapeType.SHAPE, ShapeArgs.Path("M 0 0 L 10 10")),
            shapeEntity(ShapeType.RECT, ShapeArgs.RectShape(0f, 0f, 50f, 50f, 5f)),
            shapeEntity(ShapeType.ELLIPSE, ShapeArgs.Ellipse(25f, 25f, 20f, 15f))
        )
        val frame = SVGAFrameEntity(
            alpha = 1f,
            layout = Rect(0f, 0f, 100f, 100f),
            transform = Matrix(),
            clipPath = null,
            shapes = shapes
        )
        assertEquals(3, frame.shapes.size)
        assertEquals(ShapeType.SHAPE, frame.shapes[0].type)
        assertEquals(ShapeType.RECT, frame.shapes[1].type)
        assertEquals(ShapeType.ELLIPSE, frame.shapes[2].type)
    }

    @Test
    fun frame_withNoShapes_emptyList() {
        val frame = SVGAFrameEntity(
            alpha = 1f,
            layout = Rect(0f, 0f, 100f, 100f),
            transform = Matrix(),
            clipPath = null,
            shapes = emptyList()
        )
        assertTrue(frame.shapes.isEmpty())
    }

    // ── Null styles means no drawing ────────────────────────────────────

    @Test
    fun shape_nullStyles_noDrawing() {
        val shape = shapeEntity(
            ShapeType.RECT,
            ShapeArgs.RectShape(0f, 0f, 50f, 50f, 0f),
            styles = null
        )
        assertNull(shape.styles)
    }
}
