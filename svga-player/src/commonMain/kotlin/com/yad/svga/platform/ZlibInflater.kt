package com.yad.svga.platform

/**
 * Platform-specific zlib decompression.
 *
 * Inflates zlib-compressed byte arrays used in SVGA Protobuf binary format.
 * - Android: uses java.util.zip.Inflater
 * - iOS: uses zlib via cinterop
 */
expect class ZlibInflater() {
    fun inflate(bytes: ByteArray): ByteArray

    /**
     * Inflate raw DEFLATE data (no zlib/gzip header).
     * Used for decompressing ZIP entry data.
     */
    fun inflateRaw(bytes: ByteArray): ByteArray
}
