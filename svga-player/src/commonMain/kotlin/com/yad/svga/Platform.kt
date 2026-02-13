package com.yad.svga

/**
 * Platform abstraction for KMP SVGA Player.
 *
 * expect/actual declarations for platform-specific functionality:
 * - ImageBitmapDecoder: Bitmap decoding
 * - ZlibInflater: zlib decompression
 * - FileSystem: File I/O and caching
 * - HttpLoader: Network requests (DefaultNetworkLoader, or custom SVGANetworkLoader)
 */

/** Returns the current platform name. */
expect fun platformName(): String
