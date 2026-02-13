package com.yad.svga.path

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SVGAPathParserTest {

    // Helper to make assertions more readable
    private fun cmd(type: Char, vararg args: Float) = PathCommand(type, floatArrayOf(*args))

    @Test
    fun parseEmptyString() {
        assertEquals(emptyList(), SVGAPathParser.parse(""))
    }

    @Test
    fun parseWhitespaceOnly() {
        assertEquals(emptyList(), SVGAPathParser.parse("   \t\n"))
    }

    @Test
    fun parseMoveToAbsolute() {
        val result = SVGAPathParser.parse("M 10 20")
        assertEquals(listOf(cmd('M', 10f, 20f)), result)
    }

    @Test
    fun parseMoveToRelative() {
        val result = SVGAPathParser.parse("m 5 -3")
        assertEquals(listOf(cmd('m', 5f, -3f)), result)
    }

    @Test
    fun parseLineTo() {
        val result = SVGAPathParser.parse("L 30 40")
        assertEquals(listOf(cmd('L', 30f, 40f)), result)
    }

    @Test
    fun parseHorizontalLine() {
        val result = SVGAPathParser.parse("H 50")
        assertEquals(listOf(cmd('H', 50f)), result)
    }

    @Test
    fun parseVerticalLine() {
        val result = SVGAPathParser.parse("V 60")
        assertEquals(listOf(cmd('V', 60f)), result)
    }

    @Test
    fun parseCubicBezier() {
        val result = SVGAPathParser.parse("C 1 2 3 4 5 6")
        assertEquals(listOf(cmd('C', 1f, 2f, 3f, 4f, 5f, 6f)), result)
    }

    @Test
    fun parseSmoothCubic() {
        val result = SVGAPathParser.parse("S 1 2 3 4")
        assertEquals(listOf(cmd('S', 1f, 2f, 3f, 4f)), result)
    }

    @Test
    fun parseQuadraticBezier() {
        val result = SVGAPathParser.parse("Q 1 2 3 4")
        assertEquals(listOf(cmd('Q', 1f, 2f, 3f, 4f)), result)
    }

    @Test
    fun parseArc() {
        val result = SVGAPathParser.parse("A 25 26 -30 0 1 50 -25")
        assertEquals(listOf(cmd('A', 25f, 26f, -30f, 0f, 1f, 50f, -25f)), result)
    }

    @Test
    fun parseClosePath() {
        val result = SVGAPathParser.parse("Z")
        assertEquals(listOf(cmd('Z')), result)
    }

    @Test
    fun parseClosePathLowercase() {
        val result = SVGAPathParser.parse("z")
        assertEquals(listOf(cmd('z')), result)
    }

    // --- Composite paths ---

    @Test
    fun parseMultipleCommands() {
        val result = SVGAPathParser.parse("M 0 0 L 10 10 Z")
        assertEquals(
            listOf(
                cmd('M', 0f, 0f),
                cmd('L', 10f, 10f),
                cmd('Z')
            ),
            result
        )
    }

    @Test
    fun parseTriangle() {
        val result = SVGAPathParser.parse("M 0 0 L 100 0 L 50 80 Z")
        assertEquals(
            listOf(
                cmd('M', 0f, 0f),
                cmd('L', 100f, 0f),
                cmd('L', 50f, 80f),
                cmd('Z')
            ),
            result
        )
    }

    // --- Implicit commands ---

    @Test
    fun implicitLineToAfterMoveTo() {
        // After M, extra coordinate pairs become implicit L
        val result = SVGAPathParser.parse("M 10 20 30 40 50 60")
        assertEquals(
            listOf(
                cmd('M', 10f, 20f),
                cmd('L', 30f, 40f),
                cmd('L', 50f, 60f)
            ),
            result
        )
    }

    @Test
    fun implicitRelativeLineToAfterRelativeMoveTo() {
        // After m, extra coordinate pairs become implicit l
        val result = SVGAPathParser.parse("m 10 20 30 40")
        assertEquals(
            listOf(
                cmd('m', 10f, 20f),
                cmd('l', 30f, 40f)
            ),
            result
        )
    }

    @Test
    fun implicitRepeatLineTo() {
        // After L, extra pairs repeat as L
        val result = SVGAPathParser.parse("L 1 2 3 4")
        assertEquals(
            listOf(
                cmd('L', 1f, 2f),
                cmd('L', 3f, 4f)
            ),
            result
        )
    }

    @Test
    fun implicitRepeatCubicBezier() {
        val result = SVGAPathParser.parse("C 1 2 3 4 5 6 7 8 9 10 11 12")
        assertEquals(
            listOf(
                cmd('C', 1f, 2f, 3f, 4f, 5f, 6f),
                cmd('C', 7f, 8f, 9f, 10f, 11f, 12f)
            ),
            result
        )
    }

    // --- Separator handling ---

    @Test
    fun commaSeparators() {
        val result = SVGAPathParser.parse("M10,20L30,40")
        assertEquals(
            listOf(
                cmd('M', 10f, 20f),
                cmd('L', 30f, 40f)
            ),
            result
        )
    }

    @Test
    fun negativeNumbersAsSeparators() {
        // "10-5" means "10, -5"
        val result = SVGAPathParser.parse("M10-5")
        assertEquals(listOf(cmd('M', 10f, -5f)), result)
    }

    @Test
    fun consecutiveDecimalPoints() {
        // "1.5.3" means "1.5, 0.3"
        val result = SVGAPathParser.parse("M1.5.3")
        assertEquals(listOf(cmd('M', 1.5f, 0.3f)), result)
    }

    @Test
    fun mixedSeparators() {
        val result = SVGAPathParser.parse("M 10,20 L30-40")
        assertEquals(
            listOf(
                cmd('M', 10f, 20f),
                cmd('L', 30f, -40f)
            ),
            result
        )
    }

    @Test
    fun noSpaceBetweenCommandAndNumber() {
        val result = SVGAPathParser.parse("M10 20L30 40")
        assertEquals(
            listOf(
                cmd('M', 10f, 20f),
                cmd('L', 30f, 40f)
            ),
            result
        )
    }

    // --- Decimal numbers ---

    @Test
    fun decimalNumbers() {
        val result = SVGAPathParser.parse("M 1.5 2.7")
        assertEquals(listOf(cmd('M', 1.5f, 2.7f)), result)
    }

    @Test
    fun leadingDecimalPoint() {
        val result = SVGAPathParser.parse("M .5 .3")
        assertEquals(listOf(cmd('M', 0.5f, 0.3f)), result)
    }

    // --- Complex real-world paths ---

    @Test
    fun complexPath() {
        val result = SVGAPathParser.parse("M0,0 C0,0 0,0 0,0 L0,0 Z")
        assertEquals(
            listOf(
                cmd('M', 0f, 0f),
                cmd('C', 0f, 0f, 0f, 0f, 0f, 0f),
                cmd('L', 0f, 0f),
                cmd('Z')
            ),
            result
        )
    }

    @Test
    fun pathWithAllCommandTypes() {
        val path = "M1,2 L3,4 H5 V6 C7,8,9,10,11,12 S13,14,15,16 Q17,18,19,20 A21,22,23,0,1,24,25 Z"
        val result = SVGAPathParser.parse(path)
        assertEquals(9, result.size)
        assertEquals('M', result[0].type)
        assertEquals('L', result[1].type)
        assertEquals('H', result[2].type)
        assertEquals('V', result[3].type)
        assertEquals('C', result[4].type)
        assertEquals('S', result[5].type)
        assertEquals('Q', result[6].type)
        assertEquals('A', result[7].type)
        assertEquals('Z', result[8].type)
    }

    // --- PathCommand equals/hashCode ---

    @Test
    fun pathCommandEquality() {
        val a = PathCommand('M', floatArrayOf(1f, 2f))
        val b = PathCommand('M', floatArrayOf(1f, 2f))
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun pathCommandInequalityDifferentType() {
        val a = PathCommand('M', floatArrayOf(1f, 2f))
        val b = PathCommand('L', floatArrayOf(1f, 2f))
        assertTrue(a != b)
    }

    @Test
    fun pathCommandInequalityDifferentArgs() {
        val a = PathCommand('M', floatArrayOf(1f, 2f))
        val b = PathCommand('M', floatArrayOf(3f, 4f))
        assertTrue(a != b)
    }

    @Test
    fun pathCommandToString() {
        val cmd = PathCommand('M', floatArrayOf(1f, 2f))
        assertTrue(cmd.toString().contains("M"))
        assertTrue(cmd.toString().contains("1.0"))
    }

    // --- Edge cases ---

    @Test
    fun unknownCommandsIgnored() {
        // Unknown commands should be silently ignored per spec
        val result = SVGAPathParser.parse("M 1 2 X L 3 4")
        // X is not a valid SVG command, parser should skip it
        assertEquals(
            listOf(
                cmd('M', 1f, 2f),
                cmd('L', 3f, 4f)
            ),
            result
        )
    }

    @Test
    fun multipleConsecutiveDecimalPoints() {
        // "1.2.3.4" → 1.2, .3, .4
        val result = SVGAPathParser.parse("M1.2.3.4 5")
        // M takes 2 args: 1.2, 0.3 → then implicit L: 0.4, 5
        assertEquals(
            listOf(
                cmd('M', 1.2f, 0.3f),
                cmd('L', 0.4f, 5f)
            ),
            result
        )
    }
}
