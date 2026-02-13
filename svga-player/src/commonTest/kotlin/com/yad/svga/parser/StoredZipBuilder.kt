package com.yad.svga.parser

/**
 * Test utility to build minimal ZIP files with STORED (uncompressed) entries.
 * Produces valid ZIP byte arrays for testing ZipReader and SVGAParser.
 */
internal object StoredZipBuilder {

    /**
     * Build a ZIP byte array containing the given entries (all STORED, no compression).
     */
    fun build(entries: Map<String, ByteArray>): ByteArray {
        val buffer = mutableListOf<Byte>()
        val centralDirectory = mutableListOf<Byte>()
        var offset = 0

        for ((name, data) in entries) {
            val nameBytes = name.encodeToByteArray()
            val localHeader = buildLocalFileHeader(nameBytes, data)
            buffer.addAll(localHeader.toList())

            // Central directory entry
            val cdEntry = buildCentralDirectoryEntry(nameBytes, data, offset)
            centralDirectory.addAll(cdEntry.toList())

            offset += localHeader.size
        }

        val cdOffset = offset
        val cdSize = centralDirectory.size
        buffer.addAll(centralDirectory)

        // End of central directory record
        val eocd = buildEndOfCentralDirectory(entries.size, cdSize, cdOffset)
        buffer.addAll(eocd.toList())

        return buffer.toByteArray()
    }

    private fun buildLocalFileHeader(nameBytes: ByteArray, data: ByteArray): ByteArray {
        val header = mutableListOf<Byte>()
        // Signature: PK\x03\x04
        header.addAll(writeInt32LE(0x04034B50))
        // Version needed: 20
        header.addAll(writeInt16LE(20))
        // General purpose bit flag: 0
        header.addAll(writeInt16LE(0))
        // Compression method: 0 (STORED)
        header.addAll(writeInt16LE(0))
        // Last mod file time: 0
        header.addAll(writeInt16LE(0))
        // Last mod file date: 0
        header.addAll(writeInt16LE(0))
        // CRC-32: 0 (not validated in our reader)
        header.addAll(writeInt32LE(0))
        // Compressed size
        header.addAll(writeInt32LE(data.size))
        // Uncompressed size
        header.addAll(writeInt32LE(data.size))
        // File name length
        header.addAll(writeInt16LE(nameBytes.size))
        // Extra field length: 0
        header.addAll(writeInt16LE(0))
        // File name
        header.addAll(nameBytes.toList())
        // File data
        header.addAll(data.toList())
        return header.toByteArray()
    }

    private fun buildCentralDirectoryEntry(nameBytes: ByteArray, data: ByteArray, localHeaderOffset: Int): ByteArray {
        val entry = mutableListOf<Byte>()
        // Signature: PK\x01\x02
        entry.addAll(writeInt32LE(0x02014B50))
        // Version made by: 20
        entry.addAll(writeInt16LE(20))
        // Version needed: 20
        entry.addAll(writeInt16LE(20))
        // General purpose bit flag: 0
        entry.addAll(writeInt16LE(0))
        // Compression method: 0 (STORED)
        entry.addAll(writeInt16LE(0))
        // Last mod file time: 0
        entry.addAll(writeInt16LE(0))
        // Last mod file date: 0
        entry.addAll(writeInt16LE(0))
        // CRC-32: 0
        entry.addAll(writeInt32LE(0))
        // Compressed size
        entry.addAll(writeInt32LE(data.size))
        // Uncompressed size
        entry.addAll(writeInt32LE(data.size))
        // File name length
        entry.addAll(writeInt16LE(nameBytes.size))
        // Extra field length: 0
        entry.addAll(writeInt16LE(0))
        // File comment length: 0
        entry.addAll(writeInt16LE(0))
        // Disk number start: 0
        entry.addAll(writeInt16LE(0))
        // Internal file attributes: 0
        entry.addAll(writeInt16LE(0))
        // External file attributes: 0
        entry.addAll(writeInt32LE(0))
        // Relative offset of local header
        entry.addAll(writeInt32LE(localHeaderOffset))
        // File name
        entry.addAll(nameBytes.toList())
        return entry.toByteArray()
    }

    private fun buildEndOfCentralDirectory(entryCount: Int, cdSize: Int, cdOffset: Int): ByteArray {
        val eocd = mutableListOf<Byte>()
        // Signature: PK\x05\x06
        eocd.addAll(writeInt32LE(0x06054B50))
        // Number of this disk: 0
        eocd.addAll(writeInt16LE(0))
        // Disk where central directory starts: 0
        eocd.addAll(writeInt16LE(0))
        // Number of central directory records on this disk
        eocd.addAll(writeInt16LE(entryCount))
        // Total number of central directory records
        eocd.addAll(writeInt16LE(entryCount))
        // Size of central directory
        eocd.addAll(writeInt32LE(cdSize))
        // Offset of start of central directory
        eocd.addAll(writeInt32LE(cdOffset))
        // Comment length: 0
        eocd.addAll(writeInt16LE(0))
        return eocd.toByteArray()
    }

    private fun writeInt16LE(value: Int): List<Byte> {
        return listOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte()
        )
    }

    private fun writeInt32LE(value: Int): List<Byte> {
        return listOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }
}
