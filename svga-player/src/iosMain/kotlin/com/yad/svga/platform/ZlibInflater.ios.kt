package com.yad.svga.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit2
import platform.zlib.z_stream

private const val MAX_WBITS = 15
// Accept both zlib and gzip formats
private const val AUTO_DETECT_WBITS = MAX_WBITS + 32
// Raw DEFLATE (no zlib/gzip header)
private const val RAW_DEFLATE_WBITS = -MAX_WBITS

@OptIn(ExperimentalForeignApi::class)
actual class ZlibInflater actual constructor() {

    actual fun inflate(bytes: ByteArray): ByteArray {
        if (bytes.isEmpty()) return ByteArray(0)

        val chunks = mutableListOf<ByteArray>()

        memScoped {
            val stream = alloc<z_stream>()

            bytes.usePinned { pinnedInput ->
                stream.next_in = pinnedInput.addressOf(0).reinterpret()
                stream.avail_in = bytes.size.toUInt()

                val ret = inflateInit2(stream.ptr, AUTO_DETECT_WBITS)
                if (ret != Z_OK) {
                    throw RuntimeException("zlib inflateInit2 failed: $ret")
                }

                try {
                    val bufferSize = 4096
                    val buffer = ByteArray(bufferSize)

                    do {
                        buffer.usePinned { pinnedOutput ->
                            stream.next_out = pinnedOutput.addressOf(0).reinterpret()
                            stream.avail_out = bufferSize.toUInt()

                            val inflateRet = inflate(stream.ptr, 0)
                            if (inflateRet != Z_OK && inflateRet != Z_STREAM_END) {
                                throw RuntimeException("zlib inflate failed: $inflateRet")
                            }

                            val produced = bufferSize - stream.avail_out.toInt()
                            if (produced > 0) {
                                chunks.add(buffer.copyOfRange(0, produced))
                            }

                            if (inflateRet == Z_STREAM_END) return@memScoped
                        }
                    } while (stream.avail_out == 0u)
                } finally {
                    inflateEnd(stream.ptr)
                }
            }
        }

        // Combine all chunks into a single ByteArray
        val totalSize = chunks.sumOf { it.size }
        val result = ByteArray(totalSize)
        var offset = 0
        for (chunk in chunks) {
            chunk.copyInto(result, offset)
            offset += chunk.size
        }
        return result
    }

    actual fun inflateRaw(bytes: ByteArray): ByteArray {
        if (bytes.isEmpty()) return ByteArray(0)

        val chunks = mutableListOf<ByteArray>()

        memScoped {
            val stream = alloc<z_stream>()

            bytes.usePinned { pinnedInput ->
                stream.next_in = pinnedInput.addressOf(0).reinterpret()
                stream.avail_in = bytes.size.toUInt()

                val ret = inflateInit2(stream.ptr, RAW_DEFLATE_WBITS)
                if (ret != Z_OK) {
                    throw RuntimeException("zlib inflateInit2 (raw) failed: $ret")
                }

                try {
                    val bufferSize = 4096
                    val buffer = ByteArray(bufferSize)

                    do {
                        buffer.usePinned { pinnedOutput ->
                            stream.next_out = pinnedOutput.addressOf(0).reinterpret()
                            stream.avail_out = bufferSize.toUInt()

                            val inflateRet = inflate(stream.ptr, 0)
                            if (inflateRet != Z_OK && inflateRet != Z_STREAM_END) {
                                throw RuntimeException("zlib inflate (raw) failed: $inflateRet")
                            }

                            val produced = bufferSize - stream.avail_out.toInt()
                            if (produced > 0) {
                                chunks.add(buffer.copyOfRange(0, produced))
                            }

                            if (inflateRet == Z_STREAM_END) return@memScoped
                        }
                    } while (stream.avail_out == 0u)
                } finally {
                    inflateEnd(stream.ptr)
                }
            }
        }

        val totalSize = chunks.sumOf { it.size }
        val result = ByteArray(totalSize)
        var offset = 0
        for (chunk in chunks) {
            chunk.copyInto(result, offset)
            offset += chunk.size
        }
        return result
    }
}
