package com.yad.svga.parser

/**
 * Minimal ZIP file reader that works with ByteArray directly.
 * Supports reading uncompressed (STORED) entries from ZIP archives.
 * DEFLATE-compressed entries require a platform-specific raw inflater.
 *
 * This avoids platform-specific ZIP libraries for KMP commonMain compatibility.
 * ZIP format reference: https://pkware.cachefly.net/webdocs/casestudies/APPNOTE.TXT
 */
internal object ZipReader {

    private const val LOCAL_FILE_HEADER_SIGNATURE = 0x04034B50
    private const val COMPRESSION_STORED = 0
    private const val COMPRESSION_DEFLATE = 8

    /**
     * Read all entries from a ZIP byte array.
     * Returns a map of entry name → entry data (decompressed).
     *
     * @param zipBytes The raw ZIP file bytes
     * @param rawInflater Optional function to inflate raw DEFLATE data.
     *   If null, DEFLATE-compressed entries are skipped.
     */
    fun readEntries(
        zipBytes: ByteArray,
        rawInflater: ((ByteArray) -> ByteArray)? = null
    ): Map<String, ByteArray> {
        val entries = mutableMapOf<String, ByteArray>()
        var offset = 0

        while (offset + 30 <= zipBytes.size) {
            val signature = readInt32LE(zipBytes, offset)
            if (signature != LOCAL_FILE_HEADER_SIGNATURE) break

            val compressionMethod = readInt16LE(zipBytes, offset + 8)
            val compressedSize = readInt32LE(zipBytes, offset + 18).toLong() and 0xFFFFFFFFL
            val fileNameLength = readInt16LE(zipBytes, offset + 26)
            val extraFieldLength = readInt16LE(zipBytes, offset + 28)

            val fileNameStart = offset + 30
            val fileName = zipBytes.decodeToString(fileNameStart, fileNameStart + fileNameLength)

            val dataStart = fileNameStart + fileNameLength + extraFieldLength
            val dataEnd = dataStart + compressedSize.toInt()

            if (dataEnd > zipBytes.size) break

            // Skip path traversal and subdirectory entries
            if (!fileName.contains("../") && !fileName.contains("/") && fileName.isNotEmpty()) {
                val entryData = when (compressionMethod) {
                    COMPRESSION_STORED -> {
                        zipBytes.copyOfRange(dataStart, dataEnd)
                    }
                    COMPRESSION_DEFLATE -> {
                        val compressed = zipBytes.copyOfRange(dataStart, dataEnd)
                        rawInflater?.invoke(compressed)
                    }
                    else -> null
                }
                if (entryData != null) {
                    entries[fileName] = entryData
                }
            }

            offset = dataEnd
        }

        return entries
    }

    /** Read a 16-bit little-endian unsigned integer. */
    private fun readInt16LE(data: ByteArray, offset: Int): Int {
        return (data[offset].toInt() and 0xFF) or
            ((data[offset + 1].toInt() and 0xFF) shl 8)
    }

    /** Read a 32-bit little-endian integer. */
    private fun readInt32LE(data: ByteArray, offset: Int): Int {
        return (data[offset].toInt() and 0xFF) or
            ((data[offset + 1].toInt() and 0xFF) shl 8) or
            ((data[offset + 2].toInt() and 0xFF) shl 16) or
            ((data[offset + 3].toInt() and 0xFF) shl 24)
    }
}
