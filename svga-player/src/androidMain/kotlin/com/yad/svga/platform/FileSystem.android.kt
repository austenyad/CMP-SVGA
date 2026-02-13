package com.yad.svga.platform

import java.io.File

actual class FileSystem actual constructor() {

    actual fun cacheDir(): String {
        // Use java.io.tmpdir as fallback since we don't have Android Context here.
        // In a real app, this would be set via Context.cacheDir.
        return System.getProperty("java.io.tmpdir") ?: "/tmp"
    }

    actual fun readBytes(path: String): ByteArray? {
        return try {
            val file = File(path)
            if (file.exists()) file.readBytes() else null
        } catch (_: Exception) {
            null
        }
    }

    actual fun writeBytes(path: String, bytes: ByteArray) {
        try {
            File(path).writeBytes(bytes)
        } catch (_: Exception) {
            // Silently ignore write failures
        }
    }

    actual fun exists(path: String): Boolean {
        return File(path).exists()
    }

    actual fun delete(path: String) {
        try {
            val file = File(path)
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        } catch (_: Exception) {
            // Silently ignore delete failures
        }
    }

    actual fun mkdirs(path: String) {
        try {
            File(path).mkdirs()
        } catch (_: Exception) {
            // Silently ignore mkdir failures
        }
    }
}
