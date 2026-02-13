package com.yad.svga.path

import kotlin.test.Test
import kotlin.test.assertEquals

class SVGAPathPrinterTest {

    // --- Basic formatting ---

    @Test
    fun printEmptyList() {
        assertEquals("", SVGAPathPrinter.print(emptyList()))
    }

    @Test
    fun printSingleMoveTo() {
        val commands = listOf(PathCommand('M', floatArrayOf(10f, 20f)))
        assertEquals("M 10 20", SVGAPathPrinter.print(commands))
    }

    @Test
    fun printRelativeMoveTo() {
        val commands = listOf(PathCommand('m', floatArrayOf(5f, -3f)))
        assertEquals("m 5 -3", SVGAPathPrinter.print(commands))
    }

    @Test
    fun printLineTo() {
        val commands = listOf(PathCommand('L', floatArrayOf(30f, 40f)))
        assertEquals("L 30 40", SVGAPathPrinter.print(commands))
    }

    @Test
    fun printHorizontalLine() {
        val commands = listOf(PathCommand('H', floatArrayOf(50f)))
        assertEquals("H 50", SVGAPathPrinter.print(commands))
    }

    @Test
    fun printVerticalLine() {
        val commands = listOf(PathCommand('V', floatArrayOf(60f)))
        assertEquals("V 60", SVGAPathPrinter.print(commands))
    }

    @Test
    fun printCubicBezier() {
        val commands = listOf(PathCommand('C', floatArrayOf(1f, 2f, 3f, 4f, 5f, 6f)))
        assertEquals("C 1 2 3 4 5 6", SVGAPathPrinter.print(commands))
    }

    @Test
    fun printSmoothCubic() {
        val commands = listOf(PathCommand('S', floatArrayOf(1f, 2f, 3f, 4f)))
        assertEquals("S 1 2 3 4", SVGAPathPrinter.print(commands))
    }

    @Test
    fun printQuadraticBezier() {
        val commands = listOf(PathCommand('Q', floatArrayOf(1f, 2f, 3f, 4f)))
        assertEquals("Q 1 2 3 4", SVGAPathPrinter.print(commands))
    }

    @Test
    fun printArc() {
        val commands = listOf(PathCommand('A', floatArrayOf(25f, 26f, -30f, 0f, 1f, 50f, -25f)))
        assertEquals("A 25 26 -30 0 1 50 -25", SVGAPathPrinter.print(commands))
    }

    @Test
    fun printClosePath() {
        val commands = listOf(PathCommand('Z', floatArrayOf()))
        assertEquals("Z", SVGAPathPrinter.print(commands))
    }

    @Test
    fun printClosePathLowercase() {
        val commands = listOf(PathCommand('z', floatArrayOf()))
        assertEquals("z", SVGAPathPrinter.print(commands))
    }

    // --- Float formatting ---

    @Test
    fun formatWholeNumbersWithoutDecimal() {
        val commands = listOf(PathCommand('M', floatArrayOf(10.0f, 20.0f)))
        assertEquals("M 10 20", SVGAPathPrinter.print(commands))
    }

    @Test
    fun formatDecimalValues() {
        val commands = listOf(PathCommand('M', floatArrayOf(1.5f, 2.7f)))
        assertEquals("M 1.5 2.7", SVGAPathPrinter.print(commands))
    }

    @Test
    fun formatZeroValue() {
        val commands = listOf(PathCommand('M', floatArrayOf(0f, 0f)))
        assertEquals("M 0 0", SVGAPathPrinter.print(commands))
    }

    @Test
    fun formatNegativeValues() {
        val commands = listOf(PathCommand('M', floatArrayOf(-5f, -10.5f)))
        assertEquals("M -5 -10.5", SVGAPathPrinter.print(commands))
    }

    // --- Composite paths ---

    @Test
    fun printMultipleCommands() {
        val commands = listOf(
            PathCommand('M', floatArrayOf(0f, 0f)),
            PathCommand('L', floatArrayOf(10f, 10f)),
            PathCommand('Z', floatArrayOf())
        )
        assertEquals("M 0 0 L 10 10 Z", SVGAPathPrinter.print(commands))
    }

    @Test
    fun printTriangle() {
        val commands = listOf(
            PathCommand('M', floatArrayOf(0f, 0f)),
            PathCommand('L', floatArrayOf(100f, 0f)),
            PathCommand('L', floatArrayOf(50f, 80f)),
            PathCommand('Z', floatArrayOf())
        )
        assertEquals("M 0 0 L 100 0 L 50 80 Z", SVGAPathPrinter.print(commands))
    }

    @Test
    fun printAllCommandTypes() {
        val commands = listOf(
            PathCommand('M', floatArrayOf(1f, 2f)),
            PathCommand('L', floatArrayOf(3f, 4f)),
            PathCommand('H', floatArrayOf(5f)),
            PathCommand('V', floatArrayOf(6f)),
            PathCommand('C', floatArrayOf(7f, 8f, 9f, 10f, 11f, 12f)),
            PathCommand('S', floatArrayOf(13f, 14f, 15f, 16f)),
            PathCommand('Q', floatArrayOf(17f, 18f, 19f, 20f)),
            PathCommand('A', floatArrayOf(21f, 22f, 23f, 0f, 1f, 24f, 25f)),
            PathCommand('Z', floatArrayOf())
        )
        assertEquals(
            "M 1 2 L 3 4 H 5 V 6 C 7 8 9 10 11 12 S 13 14 15 16 Q 17 18 19 20 A 21 22 23 0 1 24 25 Z",
            SVGAPathPrinter.print(commands)
        )
    }

    // --- Round-trip consistency ---

    @Test
    fun roundTripSimplePath() {
        val original = listOf(
            PathCommand('M', floatArrayOf(10f, 20f)),
            PathCommand('L', floatArrayOf(30f, 40f)),
            PathCommand('Z', floatArrayOf())
        )
        val printed = SVGAPathPrinter.print(original)
        val parsed = SVGAPathParser.parse(printed)
        assertEquals(original, parsed)
    }

    @Test
    fun roundTripCubicBezier() {
        val original = listOf(
            PathCommand('M', floatArrayOf(0f, 0f)),
            PathCommand('C', floatArrayOf(1f, 2f, 3f, 4f, 5f, 6f)),
            PathCommand('Z', floatArrayOf())
        )
        val printed = SVGAPathPrinter.print(original)
        val parsed = SVGAPathParser.parse(printed)
        assertEquals(original, parsed)
    }

    @Test
    fun roundTripRelativeCommands() {
        val original = listOf(
            PathCommand('m', floatArrayOf(5f, 10f)),
            PathCommand('l', floatArrayOf(-3f, 4f)),
            PathCommand('z', floatArrayOf())
        )
        val printed = SVGAPathPrinter.print(original)
        val parsed = SVGAPathParser.parse(printed)
        assertEquals(original, parsed)
    }

    @Test
    fun roundTripArc() {
        val original = listOf(
            PathCommand('A', floatArrayOf(25f, 26f, -30f, 0f, 1f, 50f, -25f))
        )
        val printed = SVGAPathPrinter.print(original)
        val parsed = SVGAPathParser.parse(printed)
        assertEquals(original, parsed)
    }

    @Test
    fun roundTripDecimalValues() {
        val original = listOf(
            PathCommand('M', floatArrayOf(1.5f, 2.7f)),
            PathCommand('L', floatArrayOf(0.5f, 0.3f))
        )
        val printed = SVGAPathPrinter.print(original)
        val parsed = SVGAPathParser.parse(printed)
        assertEquals(original, parsed)
    }

    // --- formatFloat internal tests ---

    @Test
    fun formatFloatWholeNumber() {
        assertEquals("10", SVGAPathPrinter.formatFloat(10.0f))
    }

    @Test
    fun formatFloatDecimal() {
        assertEquals("1.5", SVGAPathPrinter.formatFloat(1.5f))
    }

    @Test
    fun formatFloatZero() {
        assertEquals("0", SVGAPathPrinter.formatFloat(0.0f))
    }

    @Test
    fun formatFloatNegativeZero() {
        assertEquals("0", SVGAPathPrinter.formatFloat(-0.0f))
    }

    @Test
    fun formatFloatNegativeWhole() {
        assertEquals("-5", SVGAPathPrinter.formatFloat(-5.0f))
    }

    @Test
    fun formatFloatNegativeDecimal() {
        assertEquals("-1.5", SVGAPathPrinter.formatFloat(-1.5f))
    }
}
