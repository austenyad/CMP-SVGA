package com.yad.svga.platform

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Platform-specific bitmap decoder.
 *
 * Decodes raw image bytes (PNG, JPEG, WebP, etc.) into Compose [ImageBitmap].
 * - Android: uses BitmapFactory
 * - iOS: uses UIImage / CGImage
 */
expect class ImageBitmapDecoder() {
    fun decode(bytes: ByteArray): ImageBitmap?
    fun decode(bytes: ByteArray, targetWidth: Int, targetHeight: Int): ImageBitmap?
}
