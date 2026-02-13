package com.yad.svga.parser

/**
 * Cache interface for SVGA parsed files.
 * Full implementation in Task 5.3.
 */
interface SVGACache {
    /**
     * Generate a cache key from a URL string.
     */
    fun cacheKey(url: String): String

    /**
     * Read cached bytes for the given key, or null if not cached / corrupted.
     */
    fun get(key: String): ByteArray?

    /**
     * Write bytes to cache with the given key.
     */
    fun put(key: String, bytes: ByteArray)

    /**
     * Remove a cache entry.
     */
    fun remove(key: String)

    /**
     * Clear all cached entries.
     */
    fun clear()
}
