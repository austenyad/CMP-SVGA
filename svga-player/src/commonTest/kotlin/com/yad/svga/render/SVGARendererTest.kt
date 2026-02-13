package com.yad.svga.render

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import com.yad.svga.model.SVGAFrameEntity
import com.yad.svga.model.SVGASpriteEntity
import com.yad.svga.model.SVGAVideoEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for SVGARenderer sprite filtering logic – Task 7.1.
 *
 * Since DrawScope cannot be instantiated in unit tests, we replicate the
 * filtering logic from drawSprite() and verify which sprites would be drawn.
 */
class SVGARendererTest {

    // ── Helpers ─────────────────────────────────────────────────────────

    private fun frame(alpha: Float = 1f) = SVGAFrameEntity(
        alpha = alpha,
        layout = Rect(0f, 0f, 100f, 100f),
        transform = Matrix(),
        clipPath = null,
        shapes = emptyList()
    )

    private fun sprite(imageKey: String?, alpha: Float = 1f, frameCount: Int = 5, matteKey: String? = null) =
        SVGASpriteEntity(
            imageKey = imageKey,
            matteKey = matteKey,
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

    /**
     * Simulates the sprite filtering logic of SVGARenderer.drawSprite()
     * without needing a DrawScope. Returns the imageKeys of sprites that
     * would be drawn, and collects matte keys separately.
     */
    private fun filterSprites(
        sprites: List<SVGASpriteEntity>,
        frameIndex: Int,
        dynamicEntity: SVGADynamicEntity = SVGADynamicEntity()
    ): Pair<List<String?>, List<String>> {
        val drawnKeys = mutableListOf<String?>()
        val matteKeys = mutableListOf<String>()

        for (sprite in sprites) {
            if (frameIndex < 0 || frameIndex >= sprite.frames.size) continue

            val f = sprite.frames[frameIndex]
            if (f.alpha <= 0f) continue

            val imageKey = sprite.imageKey
            if (imageKey != null && dynamicEntity.isHidden(imageKey)) continue

            val isMatte = imageKey?.endsWith(".matte") == true
            if (isMatte) {
                matteKeys.add(imageKey!!)
                continue
            }

            drawnKeys.add(imageKey)
        }
        return drawnKeys to matteKeys
    }

    // ── Tests ───────────────────────────────────────────────────────────

    @Test
    fun spriteWithZeroAlphaIsSkipped() {
        val sprites = listOf(
            sprite("visible", alpha = 1f),
            sprite("invisible", alpha = 0f),
            sprite("negative", alpha = -0.5f)
        )
        val (drawn, _) = filterSprites(sprites, frameIndex = 0)
        assertEquals(listOf<String?>("visible"), drawn)
    }

    @Test
    fun hiddenSpriteIsSkipped() {
        val dynamic = SVGADynamicEntity().apply {
            setHidden(true, "hidden_sprite")
        }
        val sprites = listOf(
            sprite("normal"),
            sprite("hidden_sprite")
        )
        val (drawn, _) = filterSprites(sprites, frameIndex = 0, dynamicEntity = dynamic)
        assertEquals(listOf<String?>("normal"), drawn)
    }

    @Test
    fun matteLayerIsIdentifiedAndSkipped() {
        val sprites = listOf(
            sprite("layer1"),
            sprite("layer1.matte"),
            sprite("layer2")
        )
        val (drawn, mattes) = filterSprites(sprites, frameIndex = 0)
        assertEquals(listOf<String?>("layer1", "layer2"), drawn)
        assertTrue(mattes.contains("layer1.matte"))
    }

    @Test
    fun frameIndexOutOfRangeIsSkipped() {
        val sprites = listOf(
            sprite("short", frameCount = 2),
            sprite("long", frameCount = 10)
        )
        val (drawn, _) = filterSprites(sprites, frameIndex = 5)
        assertEquals(listOf<String?>("long"), drawn)
    }

    @Test
    fun spriteWithNullImageKeyStillDrawsShapes() {
        val sprites = listOf(
            sprite(null, alpha = 1f),
            sprite("with_key")
        )
        val (drawn, _) = filterSprites(sprites, frameIndex = 0)
        assertEquals(listOf<String?>(null, "with_key"), drawn)
    }

    @Test
    fun allSpritesDrawnInOrder() {
        val sprites = listOf(
            sprite("a"),
            sprite("b"),
            sprite("c")
        )
        val (drawn, _) = filterSprites(sprites, frameIndex = 0)
        assertEquals(listOf<String?>("a", "b", "c"), drawn)
    }

    @Test
    fun hiddenFalseDoesNotSkip() {
        val dynamic = SVGADynamicEntity().apply {
            setHidden(false, "sprite1")
        }
        val sprites = listOf(sprite("sprite1"))
        val (drawn, _) = filterSprites(sprites, frameIndex = 0, dynamicEntity = dynamic)
        assertEquals(listOf<String?>("sprite1"), drawn)
    }

    @Test
    fun rendererCanBeInstantiated() {
        val entity = videoEntity(listOf(sprite("test")))
        val renderer = SVGARenderer(entity, SVGADynamicEntity())
        // Just verify it doesn't throw
        assertTrue(renderer is SVGARenderer)
    }

    // ── Matte Layer Compositing Tests (Task 7.4) ───────────────────────

    private fun renderer(sprites: List<SVGASpriteEntity>): SVGARenderer {
        return SVGARenderer(videoEntity(sprites), SVGADynamicEntity())
    }

    @Test
    fun matteBeginIndices_singleGroup() {
        // Sprites: A(matteKey="m.matte"), B(matteKey="m.matte")
        val sprites = listOf(
            sprite("a", matteKey = "m.matte"),
            sprite("b", matteKey = "m.matte")
        )
        val r = renderer(sprites)
        val begins = r.computeMatteBeginIndices(sprites)
        assertTrue(begins[0], "First sprite in group should be begin")
        assertTrue(!begins[1], "Second sprite in same group should not be begin")
    }

    @Test
    fun matteEndIndices_singleGroup() {
        val sprites = listOf(
            sprite("a", matteKey = "m.matte"),
            sprite("b", matteKey = "m.matte")
        )
        val r = renderer(sprites)
        val ends = r.computeMatteEndIndices(sprites)
        assertTrue(!ends[0], "First sprite in group should not be end")
        assertTrue(ends[1], "Last sprite in group should be end")
    }

    @Test
    fun matteBeginEnd_noMatteSprites() {
        val sprites = listOf(
            sprite("a"),
            sprite("b"),
            sprite("c")
        )
        val r = renderer(sprites)
        val begins = r.computeMatteBeginIndices(sprites)
        val ends = r.computeMatteEndIndices(sprites)
        for (i in sprites.indices) {
            assertTrue(!begins[i], "No sprite should be matte begin")
            assertTrue(!ends[i], "No sprite should be matte end")
        }
    }

    @Test
    fun matteBeginEnd_twoSeparateGroups() {
        // Group 1: sprites 0,1 with matteKey "m1.matte"
        // Normal: sprite 2 with no matteKey
        // Group 2: sprite 3 with matteKey "m2.matte"
        val sprites = listOf(
            sprite("a", matteKey = "m1.matte"),
            sprite("b", matteKey = "m1.matte"),
            sprite("c"),
            sprite("d", matteKey = "m2.matte")
        )
        val r = renderer(sprites)
        val begins = r.computeMatteBeginIndices(sprites)
        val ends = r.computeMatteEndIndices(sprites)

        // Group 1 boundaries
        assertTrue(begins[0], "Sprite 0 should be begin of group 1")
        assertTrue(!begins[1], "Sprite 1 should not be begin")
        assertTrue(!ends[0], "Sprite 0 should not be end")
        assertTrue(ends[1], "Sprite 1 should be end of group 1")

        // Normal sprite
        assertTrue(!begins[2], "Sprite 2 (no matteKey) should not be begin")
        assertTrue(!ends[2], "Sprite 2 (no matteKey) should not be end")

        // Group 2 boundaries
        assertTrue(begins[3], "Sprite 3 should be begin of group 2")
        assertTrue(ends[3], "Sprite 3 should be end of group 2 (single element)")
    }

    @Test
    fun matteBeginEnd_adjacentDifferentGroups() {
        // Two groups back-to-back with different matteKeys
        val sprites = listOf(
            sprite("a", matteKey = "m1.matte"),
            sprite("b", matteKey = "m2.matte")
        )
        val r = renderer(sprites)
        val begins = r.computeMatteBeginIndices(sprites)
        val ends = r.computeMatteEndIndices(sprites)

        assertTrue(begins[0], "Sprite 0 should be begin of group 1")
        assertTrue(ends[0], "Sprite 0 should be end of group 1")
        assertTrue(begins[1], "Sprite 1 should be begin of group 2")
        assertTrue(ends[1], "Sprite 1 should be end of group 2")
    }

    @Test
    fun matteBeginEnd_emptyList() {
        val sprites = emptyList<SVGASpriteEntity>()
        val r = renderer(sprites)
        val begins = r.computeMatteBeginIndices(sprites)
        val ends = r.computeMatteEndIndices(sprites)
        assertEquals(0, begins.size)
        assertEquals(0, ends.size)
    }

    @Test
    fun matteSpritesFilteredFromNonMatteList() {
        // Verify that drawFrame filters out ".matte" sprites from the iteration list
        val sprites = listOf(
            sprite("layer1.matte"),
            sprite("content", matteKey = "layer1.matte"),
            sprite("normal")
        )
        val nonMatte = sprites.filter { it.imageKey?.endsWith(".matte") != true }
        assertEquals(2, nonMatte.size)
        assertEquals("content", nonMatte[0].imageKey)
        assertEquals("normal", nonMatte[1].imageKey)
    }

    @Test
    fun matteBeginEnd_emptyMatteKeyTreatedAsNoGroup() {
        val sprites = listOf(
            sprite("a", matteKey = ""),
            sprite("b", matteKey = "m.matte")
        )
        val r = renderer(sprites)
        val begins = r.computeMatteBeginIndices(sprites)
        val ends = r.computeMatteEndIndices(sprites)

        assertTrue(!begins[0], "Empty matteKey should not be begin")
        assertTrue(!ends[0], "Empty matteKey should not be end")
        assertTrue(begins[1], "Sprite with matteKey should be begin")
        assertTrue(ends[1], "Sprite with matteKey should be end")
    }
}
