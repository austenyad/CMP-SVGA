package com.yad.svga.render

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContentScaleHelperTest {

    private fun assertClose(expected: Float, actual: Float, eps: Float = 0.001f, msg: String = "") {
        assertTrue(abs(expected - actual) < eps, "$msg expected=$expected actual=$actual")
    }

    // ── ContentScale.Fit ────────────────────────────────────────────────

    @Test
    fun fitWiderVideo() {
        // Video 200x100 into canvas 100x100 → scale by 0.5, centered vertically
        val r = computeScaleResult(Size(100f, 100f), Size(200f, 100f), ContentScale.Fit)
        assertClose(0.5f, r.scaleX, msg = "scaleX")
        assertClose(0.5f, r.scaleY, msg = "scaleY")
        assertClose(0f, r.offsetX, msg = "offsetX")
        assertClose(25f, r.offsetY, msg = "offsetY")
    }

    @Test
    fun fitTallerVideo() {
        // Video 100x200 into canvas 100x100 → scale by 0.5, centered horizontally
        val r = computeScaleResult(Size(100f, 100f), Size(100f, 200f), ContentScale.Fit)
        assertClose(0.5f, r.scaleX, msg = "scaleX")
        assertClose(0.5f, r.scaleY, msg = "scaleY")
        assertClose(25f, r.offsetX, msg = "offsetX")
        assertClose(0f, r.offsetY, msg = "offsetY")
    }

    @Test
    fun fitExactSize() {
        val r = computeScaleResult(Size(100f, 100f), Size(100f, 100f), ContentScale.Fit)
        assertClose(1f, r.scaleX)
        assertClose(1f, r.scaleY)
        assertClose(0f, r.offsetX)
        assertClose(0f, r.offsetY)
    }

    // ── ContentScale.Crop ───────────────────────────────────────────────

    @Test
    fun cropWiderVideo() {
        // Video 200x100 into canvas 100x100 → scale by 1.0 (height-limited), clip width
        val r = computeScaleResult(Size(100f, 100f), Size(200f, 100f), ContentScale.Crop)
        assertClose(1f, r.scaleX, msg = "scaleX")
        assertClose(1f, r.scaleY, msg = "scaleY")
        assertClose(-50f, r.offsetX, msg = "offsetX")
        assertClose(0f, r.offsetY, msg = "offsetY")
    }

    @Test
    fun cropTallerVideo() {
        // Video 100x200 into canvas 100x100 → scale by 1.0 (width-limited), clip height
        val r = computeScaleResult(Size(100f, 100f), Size(100f, 200f), ContentScale.Crop)
        assertClose(1f, r.scaleX, msg = "scaleX")
        assertClose(1f, r.scaleY, msg = "scaleY")
        assertClose(0f, r.offsetX, msg = "offsetX")
        assertClose(-50f, r.offsetY, msg = "offsetY")
    }

    // ── ContentScale.Fill ───────────────────────────────────────────────

    @Test
    fun fillStretchesToCanvas() {
        // Video 200x100 into canvas 100x200 → scaleX=0.5, scaleY=2.0
        val r = computeScaleResult(Size(100f, 200f), Size(200f, 100f), ContentScale.FillBounds)
        assertClose(0.5f, r.scaleX, msg = "scaleX")
        assertClose(2f, r.scaleY, msg = "scaleY")
        assertClose(0f, r.offsetX, msg = "offsetX")
        assertClose(0f, r.offsetY, msg = "offsetY")
    }

    // ── ContentScale.None ───────────────────────────────────────────────

    @Test
    fun noneNoScaling() {
        val r = computeScaleResult(Size(100f, 100f), Size(200f, 200f), ContentScale.None)
        assertClose(1f, r.scaleX)
        assertClose(1f, r.scaleY)
        assertClose(-50f, r.offsetX, msg = "offsetX centered")
        assertClose(-50f, r.offsetY, msg = "offsetY centered")
    }

    // ── Edge cases ──────────────────────────────────────────────────────

    @Test
    fun zeroVideoSizeReturnIdentity() {
        val r = computeScaleResult(Size(100f, 100f), Size(0f, 0f), ContentScale.Fit)
        assertEquals(1f, r.scaleX)
        assertEquals(1f, r.scaleY)
        assertEquals(0f, r.offsetX)
        assertEquals(0f, r.offsetY)
    }

    @Test
    fun zeroCanvasSizeReturnIdentity() {
        val r = computeScaleResult(Size(0f, 0f), Size(100f, 100f), ContentScale.Fit)
        assertEquals(1f, r.scaleX)
        assertEquals(1f, r.scaleY)
        assertEquals(0f, r.offsetX)
        assertEquals(0f, r.offsetY)
    }
}
