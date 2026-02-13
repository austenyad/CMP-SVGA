package com.example.svga.demo

/**
 * Load a bundled SVGA asset by name. Returns raw bytes.
 * - Android: reads from assets/
 * - iOS: reads from Bundle.main
 */
expect suspend fun loadAssetBytes(name: String): ByteArray
