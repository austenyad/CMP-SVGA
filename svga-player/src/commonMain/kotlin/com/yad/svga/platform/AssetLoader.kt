package com.yad.svga.platform

/**
 * Platform-specific asset loader.
 *
 * Reads raw bytes from bundled assets.
 * - Android: reads from app assets via AssetManager
 * - iOS: reads from NSBundle.mainBundle
 */
expect class AssetLoader() {
    suspend fun loadBytes(name: String): ByteArray
}
