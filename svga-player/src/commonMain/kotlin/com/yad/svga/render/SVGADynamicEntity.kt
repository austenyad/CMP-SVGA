package com.yad.svga.render

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Dynamic element replacement container for SVGA animations.
 * Supports runtime replacement of images, text, visibility, and custom drawing.
 */
class SVGADynamicEntity {

    private val dynamicImages = mutableMapOf<String, ImageBitmap>()
    private val dynamicTexts = mutableMapOf<String, Pair<String, SVGATextStyle>>()
    private val dynamicHidden = mutableMapOf<String, Boolean>()
    private val dynamicDrawers = mutableMapOf<String, (DrawScope, Int) -> Unit>()

    fun setDynamicImage(image: ImageBitmap, forKey: String) {
        dynamicImages[forKey] = image
    }

    fun setDynamicText(text: String, textStyle: SVGATextStyle, forKey: String) {
        dynamicTexts[forKey] = text to textStyle
    }

    fun setHidden(hidden: Boolean, forKey: String) {
        dynamicHidden[forKey] = hidden
    }

    fun setDynamicDrawer(drawer: (DrawScope, Int) -> Unit, forKey: String) {
        dynamicDrawers[forKey] = drawer
    }

    fun clearDynamicObjects() {
        dynamicImages.clear()
        dynamicTexts.clear()
        dynamicHidden.clear()
        dynamicDrawers.clear()
    }

    fun getDynamicImage(forKey: String): ImageBitmap? = dynamicImages[forKey]

    fun getDynamicText(forKey: String): Pair<String, SVGATextStyle>? = dynamicTexts[forKey]

    fun isHidden(forKey: String): Boolean = dynamicHidden[forKey] == true

    fun getDynamicDrawer(forKey: String): ((DrawScope, Int) -> Unit)? = dynamicDrawers[forKey]
}
