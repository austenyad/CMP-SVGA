package com.example.svga.demo

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual suspend fun loadAssetBytes(name: String): ByteArray = withContext(Dispatchers.Default) {
    val baseName = name.substringBeforeLast(".")
    val ext = name.substringAfterLast(".")
    val path = NSBundle.mainBundle.pathForResource(baseName, ext)
        ?: throw IllegalStateException("Asset not found in bundle: $name")
    val data = NSData.dataWithContentsOfFile(path)
        ?: throw IllegalStateException("Failed to read asset: $name")
    val length = data.length.toInt()
    if (length == 0) return@withContext ByteArray(0)
    val bytes = ByteArray(length)
    bytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), data.bytes, data.length)
    }
    bytes
}
