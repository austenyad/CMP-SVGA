package com.yad.svga.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

actual class ImageBitmapDecoder actual constructor() {

    actual fun decode(bytes: ByteArray): ImageBitmap? {
        return try {
            Image.makeFromEncoded(bytes).toComposeImageBitmap()
        } catch (_: Exception) {
            null
        }
    }

    actual fun decode(bytes: ByteArray, targetWidth: Int, targetHeight: Int): ImageBitmap? {
        // Skia handles efficient decoding internally; decode full image then let
        // the renderer scale as needed. For a more memory-efficient path we could
        // use Skia's codec API, but this is sufficient for SVGA sprite sheets.
        return decode(bytes)
    }
}
