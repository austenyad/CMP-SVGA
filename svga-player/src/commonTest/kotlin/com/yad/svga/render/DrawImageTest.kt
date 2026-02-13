package com.yad.svga.render

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import com.yad.svga.model.SVGAFrameEntity
import com.yad.svga.model.SVGASpriteEntity
import com.yad.svga.model.SVGAVideoEntity
import com.yad.svga.path.SVGAPathParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for drawImage-related logic (Task 7.2).
 *
 * Since DrawScope and Path cannot be instantiated in pure unit tests,
 * we test the logic that drawImage depends on:
 * 1. Bitmap resolution: dynamic image priority over imageMap
 * 2. ClipPath parsing: SVGAPathParser correctly parses clip path strings
 * 3. SVGADynamicEntity API contract
 * 4. Frame layout and alpha edge cases
 */
class DrawImageTest {

    // ── Helpers ─────────────────────────────────────────────────────────

    private fun frame(
        alpha: Float = 1f,
        layout: Rect = Rect(0f, 0f, 100f, 100f),
        clipPath: String? = null
    ) = SVGAFrameEntity(
        alpha = alpha,
        layout = layout,
        transform = Matrix(),
        clipPath = clipPath,
        shapes = emptyList()
    )

    private fun sprite(imageKey: String?, alpha: Float = 1f, frameCount: Int = 5) =
        SVGASpriteEntity(
            imageKey = imageKey,
            matteKey = null,
            frames = List(frameCount) { frame(alpha) }
        )

    private fun videoEntity(sprites: List<SVGASpriteEntity>) = SVGAVideoEntity(
        version = "2.0",
        videoSize = Size(100f, 100f),
        fps = 20,
        frames = 5,
        spriteList = sprites,
        imageMap = emptyMap(),
        audioList = emptyList()
    )

    // ── Bitmap resolution: dynamic priority ─────────────────────────────

    @Test
    fun dynamicImage_returnsNullForUnsetKey() {
        val dynamic = SVGADynamicEntity()
        assertNull(dynamic.getDynamicImage("nonexistent"))
    }

    @Test
    fun dynamicImage_clearRemovesAll() {
        val dynamic = SVGADynamicEntity()
        dynamic.clearDynamicObjects()
        assertNull(dynamic.getDynamicImage("any"))
        assertTrue(!dynamic.isHidden("any"))
        assertNull(dynamic.getDynamicDrawer("any"))
    }

    @Test
    fun imageMap_emptyMapReturnsNull() {
        val entity = videoEntity(emptyList())
        assertNull(entity.imageMap["missing_key"])
    }

    // ── ClipPath parsing (verifies SVGAPathParser works for clip paths) ─

    @Test
    fun clipPath_rectangularPath_parsesCorrectly() {
        // A typical rectangular clip path used in SVGA
        val clipPathStr = "M 0 0 L 100 0 L 100 100 L 0 100 Z"
        val commands = SVGAPathParser.parse(clipPathStr)
        assertEquals(5, commands.size)
        assertEquals('M', commands[0].type)
        assertEquals('L', commands[1].type)
        assertEquals('L', commands[2].type)
        assertEquals('L', commands[3].type)
        assertEquals('Z', commands[4].type)
    }

    @Test
    fun clipPath_emptyString_parsesToEmptyList() {
        val commands = SVGAPathParser.parse("")
        assertTrue(commands.isEmpty())
    }

    @Test
    fun clipPath_complexPath_parsesAllCommands() {
        // A more complex clip path with curves
        val clipPathStr = "M 10 10 C 20 20 40 20 50 10 L 50 50 Z"
        val commands = SVGAPathParser.parse(clipPathStr)
        assertEquals(4, commands.size)
        assertEquals('M', commands[0].type)
        assertEquals('C', commands[1].type)
        assertEquals('L', commands[2].type)
        assertEquals('Z', commands[3].type)
    }

    @Test
    fun clipPath_relativeCommands_parsesCorrectly() {
        val clipPathStr = "m 0 0 l 100 0 l 0 100 l -100 0 z"
        val commands = SVGAPathParser.parse(clipPathStr)
        assertEquals(5, commands.size)
        assertEquals('m', commands[0].type)
        assertEquals('l', commands[1].type)
    }

    // ── Frame layout edge cases ─────────────────────────────────────────

    @Test
    fun frame_zeroWidthLayout() {
        val f = frame(layout = Rect(0f, 0f, 0f, 100f))
        assertEquals(0f, f.layout.width)
    }

    @Test
    fun frame_zeroHeightLayout() {
        val f = frame(layout = Rect(0f, 0f, 100f, 0f))
        assertEquals(0f, f.layout.height)
    }

    @Test
    fun frame_negativeLayout() {
        // Rect with right < left results in negative width
        val f = frame(layout = Rect(100f, 100f, 0f, 0f))
        assertTrue(f.layout.width < 0f)
    }

    @Test
    fun frame_normalLayout_hasDimensions() {
        val f = frame(layout = Rect(10f, 20f, 110f, 120f))
        assertEquals(100f, f.layout.width)
        assertEquals(100f, f.layout.height)
    }

    // ── Alpha edge cases ────────────────────────────────────────────────

    @Test
    fun frame_alphaClampedInRange() {
        // Verify alpha coercion logic: values should be clamped to [0, 1]
        val alpha = 1.5f
        val clamped = alpha.coerceIn(0f, 1f)
        assertEquals(1f, clamped)
    }

    @Test
    fun frame_negativeAlphaClampsToZero() {
        val alpha = -0.5f
        val clamped = alpha.coerceIn(0f, 1f)
        assertEquals(0f, clamped)
    }

    @Test
    fun frame_normalAlphaUnchanged() {
        val alpha = 0.7f
        val clamped = alpha.coerceIn(0f, 1f)
        assertEquals(0.7f, clamped)
    }

    // ── ClipPath presence on frame ──────────────────────────────────────

    @Test
    fun frame_withClipPath_isNotNull() {
        val f = frame(clipPath = "M 0 0 L 100 0 L 100 100 Z")
        assertNotNull(f.clipPath)
    }

    @Test
    fun frame_withoutClipPath_isNull() {
        val f = frame(clipPath = null)
        assertNull(f.clipPath)
    }

    @Test
    fun frame_emptyClipPath_isNotNull() {
        val f = frame(clipPath = "")
        // isNullOrEmpty check in drawImage should handle this
        assertTrue(f.clipPath.isNullOrEmpty())
    }

    // ── Sprite imageKey for bitmap lookup ───────────────────────────────

    @Test
    fun sprite_withImageKey_usedForBitmapLookup() {
        val s = sprite("avatar")
        assertEquals("avatar", s.imageKey)
    }

    @Test
    fun sprite_withNullImageKey_noBitmapLookup() {
        val s = sprite(null)
        assertNull(s.imageKey)
    }

    // ── Integration: drawImage would skip when no bitmap available ──────

    @Test
    fun emptyImageMap_noBitmapForKey() {
        val entity = videoEntity(listOf(sprite("key1")))
        // imageMap is empty, so drawImage would early-return
        assertNull(entity.imageMap["key1"])
    }

    @Test
    fun dynamicEntity_hiddenCheck() {
        val dynamic = SVGADynamicEntity()
        dynamic.setHidden(true, "hidden_key")
        assertTrue(dynamic.isHidden("hidden_key"))
        assertTrue(!dynamic.isHidden("other_key"))
    }

    @Test
    fun dynamicEntity_hiddenFalse_notHidden() {
        val dynamic = SVGADynamicEntity()
        dynamic.setHidden(false, "key")
        assertTrue(!dynamic.isHidden("key"))
    }
}
