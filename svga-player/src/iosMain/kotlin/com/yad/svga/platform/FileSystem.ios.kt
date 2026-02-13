package com.yad.svga.platform

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class FileSystem actual constructor() {

    private val fileManager = NSFileManager.defaultManager

    actual fun cacheDir(): String {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSCachesDirectory,
            NSUserDomainMask,
            true
        )
        return (paths.firstOrNull() as? String) ?: "/tmp"
    }

    actual fun readBytes(path: String): ByteArray? {
        return try {
            val data = NSData.create(contentsOfFile = path) ?: return null
            data.toByteArray()
        } catch (_: Exception) {
            null
        }
    }

    actual fun writeBytes(path: String, bytes: ByteArray) {
        try {
            val data = bytes.toNSData()
            data.writeToFile(path, atomically = true)
        } catch (_: Exception) {
            // Silently ignore write failures
        }
    }

    actual fun exists(path: String): Boolean {
        return fileManager.fileExistsAtPath(path)
    }

    actual fun delete(path: String) {
        try {
            fileManager.removeItemAtPath(path, error = null)
        } catch (_: Exception) {
            // Silently ignore delete failures
        }
    }

    actual fun mkdirs(path: String) {
        try {
            fileManager.createDirectoryAtPath(
                path = path,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        } catch (_: Exception) {
            // Silently ignore mkdir failures
        }
    }
}
