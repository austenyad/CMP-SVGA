package com.yad.svga.render

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SVGADynamicEntityTest {

    // ── SVGATextStyle 默认值 ─────────────────────────────────────────

    @Test
    fun textStyleDefaultValues() {
        val style = SVGATextStyle()
        assertEquals(Color.Black, style.color)
        assertEquals(14f, style.fontSize)
    }

    @Test
    fun textStyleCustomValues() {
        val style = SVGATextStyle(color = Color.Red, fontSize = 24f)
        assertEquals(Color.Red, style.color)
        assertEquals(24f, style.fontSize)
    }

    // ── setDynamicText / getDynamicText ──────────────────────────────

    @Test
    fun setDynamicTextStoresTextAndStyle() {
        val entity = SVGADynamicEntity()
        val style = SVGATextStyle(color = Color.White, fontSize = 18f)
        entity.setDynamicText("Hello", style, forKey = "nickname")

        val result = entity.getDynamicText("nickname")
        assertNotNull(result)
        assertEquals("Hello", result.first)
        assertEquals(style, result.second)
    }

    @Test
    fun getDynamicTextReturnsNullForUnsetKey() {
        val entity = SVGADynamicEntity()
        assertNull(entity.getDynamicText("nonexistent"))
    }

    @Test
    fun setDynamicTextOverwritesPreviousValue() {
        val entity = SVGADynamicEntity()
        val style1 = SVGATextStyle(color = Color.Red, fontSize = 12f)
        val style2 = SVGATextStyle(color = Color.Blue, fontSize = 20f)

        entity.setDynamicText("First", style1, forKey = "key")
        entity.setDynamicText("Second", style2, forKey = "key")

        val result = entity.getDynamicText("key")
        assertNotNull(result)
        assertEquals("Second", result.first)
        assertEquals(style2, result.second)
    }

    @Test
    fun setDynamicTextMultipleKeys() {
        val entity = SVGADynamicEntity()
        val style = SVGATextStyle()

        entity.setDynamicText("Alice", style, forKey = "name")
        entity.setDynamicText("100", style, forKey = "score")

        assertNotNull(entity.getDynamicText("name"))
        assertNotNull(entity.getDynamicText("score"))
        assertEquals("Alice", entity.getDynamicText("name")!!.first)
        assertEquals("100", entity.getDynamicText("score")!!.first)
    }

    // ── clearDynamicObjects 清除文本 ─────────────────────────────────

    @Test
    fun clearDynamicObjectsClearsTexts() {
        val entity = SVGADynamicEntity()
        entity.setDynamicText("Hello", SVGATextStyle(), forKey = "key1")
        entity.setDynamicText("World", SVGATextStyle(), forKey = "key2")

        entity.clearDynamicObjects()

        assertNull(entity.getDynamicText("key1"))
        assertNull(entity.getDynamicText("key2"))
    }

    @Test
    fun clearDynamicObjectsClearsAllTypes() {
        val entity = SVGADynamicEntity()
        entity.setDynamicText("text", SVGATextStyle(), forKey = "t")
        entity.setHidden(true, forKey = "h")
        entity.setDynamicDrawer({ _, _ -> }, forKey = "d")

        entity.clearDynamicObjects()

        assertNull(entity.getDynamicText("t"))
        assertFalse(entity.isHidden("h"))
        assertNull(entity.getDynamicDrawer("d"))
        assertNull(entity.getDynamicImage("img"))
    }

    // ── 既有功能回归 ─────────────────────────────────────────────────

    @Test
    fun setHiddenAndIsHidden() {
        val entity = SVGADynamicEntity()
        assertFalse(entity.isHidden("key"))

        entity.setHidden(true, forKey = "key")
        assertTrue(entity.isHidden("key"))

        entity.setHidden(false, forKey = "key")
        assertFalse(entity.isHidden("key"))
    }

    @Test
    fun setDynamicDrawerAndGetDynamicDrawer() {
        val entity = SVGADynamicEntity()
        assertNull(entity.getDynamicDrawer("key"))

        val drawer: (androidx.compose.ui.graphics.drawscope.DrawScope, Int) -> Unit = { _, _ -> }
        entity.setDynamicDrawer(drawer, forKey = "key")
        assertNotNull(entity.getDynamicDrawer("key"))
    }
}
