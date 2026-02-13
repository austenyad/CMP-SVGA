package com.yad.svga.platform

import java.io.ByteArrayOutputStream
import java.util.zip.Inflater

actual class ZlibInflater actual constructor() {

    actual fun inflate(bytes: ByteArray): ByteArray {
        val inflater = Inflater()
        try {
            inflater.setInput(bytes)
            val outputStream = ByteArrayOutputStream(bytes.size * 2)
            val buffer = ByteArray(4096)
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                if (count == 0 && inflater.needsInput()) break
                outputStream.write(buffer, 0, count)
            }
            return outputStream.toByteArray()
        } finally {
            inflater.end()
        }
    }

    actual fun inflateRaw(bytes: ByteArray): ByteArray {
        // nowrap=true means raw DEFLATE (no zlib/gzip header)
        val inflater = Inflater(true)
        try {
            inflater.setInput(bytes)
            val outputStream = ByteArrayOutputStream(bytes.size * 2)
            val buffer = ByteArray(4096)
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                if (count == 0 && inflater.needsInput()) break
                outputStream.write(buffer, 0, count)
            }
            return outputStream.toByteArray()
        } finally {
            inflater.end()
        }
    }
}
