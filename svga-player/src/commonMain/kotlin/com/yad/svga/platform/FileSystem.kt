package com.yad.svga.platform

/**
 * Platform-specific file system access for caching.
 *
 * Provides basic file I/O operations needed by SVGACache.
 * - Android: uses Android File API / Context.cacheDir
 * - iOS: uses NSFileManager / NSCachesDirectory
 */
expect class FileSystem() {
    fun cacheDir(): String
    fun readBytes(path: String): ByteArray?
    fun writeBytes(path: String, bytes: ByteArray)
    fun exists(path: String): Boolean
    fun delete(path: String)
    fun mkdirs(path: String)
}
