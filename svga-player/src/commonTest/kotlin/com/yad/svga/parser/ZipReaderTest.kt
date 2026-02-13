package com.yad.svga.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ZipReaderTest {

    @Test
    fun readEntries_withSingleStoredEntry_readsCorrectly() {
        val content = "Hello, SVGA!".encodeToByteArray()
        val zipBytes = StoredZipBuilder.build(mapOf("test.txt" to content))

        val entries = ZipReader.readEntries(zipBytes)
        assertEquals(1, entries.size)
        assertTrue(entries.containsKey("test.txt"))
        assertTrue(content.contentEquals(entries["test.txt"]!!))
    }

    @Test
    fun readEntries_withMultipleEntries_readsAll() {
        val file1 = "file1 content".encodeToByteArray()
        val file2 = "file2 content".encodeToByteArray()
        val file3 = byteArrayOf(0x01, 0x02, 0x03)

        val zipBytes = StoredZipBuilder.build(mapOf(
            "file1.txt" to file1,
            "file2.txt" to file2,
            "binary.dat" to file3
        ))

        val entries = ZipReader.readEntries(zipBytes)
        assertEquals(3, entries.size)
        assertTrue(file1.contentEquals(entries["file1.txt"]!!))
        assertTrue(file2.contentEquals(entries["file2.txt"]!!))
        assertTrue(file3.contentEquals(entries["binary.dat"]!!))
    }

    @Test
    fun readEntries_withEmptyFile_returnsEmptyContent() {
        val zipBytes = StoredZipBuilder.build(mapOf("empty.txt" to byteArrayOf()))

        val entries = ZipReader.readEntries(zipBytes)
        assertEquals(1, entries.size)
        assertEquals(0, entries["empty.txt"]!!.size)
    }

    @Test
    fun readEntries_withInvalidSignature_returnsEmpty() {
        val garbage = byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x04, 0x05)
        val entries = ZipReader.readEntries(garbage)
        assertTrue(entries.isEmpty())
    }

    @Test
    fun readEntries_withEmptyInput_returnsEmpty() {
        val entries = ZipReader.readEntries(byteArrayOf())
        assertTrue(entries.isEmpty())
    }

    @Test
    fun readEntries_withTruncatedHeader_returnsEmpty() {
        // Only the ZIP magic, not enough for a full header
        val truncated = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
        val entries = ZipReader.readEntries(truncated)
        assertTrue(entries.isEmpty())
    }

    @Test
    fun readEntries_skipsPathTraversalEntries() {
        // Build a ZIP manually with a "../evil.txt" entry
        // StoredZipBuilder won't filter, but ZipReader should skip it
        val content = "evil".encodeToByteArray()
        val zipBytes = StoredZipBuilder.build(mapOf(
            "../evil.txt" to content,
            "safe.txt" to "safe".encodeToByteArray()
        ))

        val entries = ZipReader.readEntries(zipBytes)
        assertFalse(entries.containsKey("../evil.txt"))
        assertTrue(entries.containsKey("safe.txt"))
    }

    @Test
    fun readEntries_skipsSubdirectoryEntries() {
        val content = "nested".encodeToByteArray()
        val zipBytes = StoredZipBuilder.build(mapOf(
            "subdir/file.txt" to content,
            "root.txt" to "root".encodeToByteArray()
        ))

        val entries = ZipReader.readEntries(zipBytes)
        assertFalse(entries.containsKey("subdir/file.txt"))
        assertTrue(entries.containsKey("root.txt"))
    }

    @Test
    fun readEntries_withLargeContent_readsCorrectly() {
        val largeContent = ByteArray(10000) { (it % 256).toByte() }
        val zipBytes = StoredZipBuilder.build(mapOf("large.bin" to largeContent))

        val entries = ZipReader.readEntries(zipBytes)
        assertEquals(1, entries.size)
        assertTrue(largeContent.contentEquals(entries["large.bin"]!!))
    }
}
