package com.yad.svga.parser

import androidx.compose.ui.graphics.ImageBitmap
import com.yad.svga.model.toVideoEntity
import com.yad.svga.proto.MovieEntity
import com.yad.svga.proto.MovieParams
import com.yad.svga.proto.SpriteEntity
import kotlinx.coroutines.test.runTest
import pbandk.ByteArr
import pbandk.decodeFromByteArray
import pbandk.encodeToByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SVGAParserTest {

    // ---- isZipFile tests ----

    @Test
    fun isZipFile_withValidZipMagic_returnsTrue() {
        val bytes = byteArrayOf(0x50, 0x4B, 0x03, 0x04, 0x00)
        assertTrue(SVGAParserCore.isZipFile(bytes))
    }

    @Test
    fun isZipFile_withProtobufData_returnsFalse() {
        val bytes = byteArrayOf(0x78, 0x9C.toByte(), 0x01, 0x02, 0x03)
        assertFalse(SVGAParserCore.isZipFile(bytes))
    }

    @Test
    fun isZipFile_withEmptyArray_returnsFalse() {
        assertFalse(SVGAParserCore.isZipFile(byteArrayOf()))
    }

    @Test
    fun isZipFile_withTooShortArray_returnsFalse() {
        assertFalse(SVGAParserCore.isZipFile(byteArrayOf(0x50, 0x4B, 0x03)))
    }

    @Test
    fun isZipFile_withExactly4Bytes_returnsFalse() {
        // size must be > 4, not >= 4
        assertFalse(SVGAParserCore.isZipFile(byteArrayOf(0x50, 0x4B, 0x03, 0x04)))
    }

    @Test
    fun isZipFile_withWrongMagicByte_returnsFalse() {
        assertFalse(SVGAParserCore.isZipFile(byteArrayOf(0x50, 0x4B, 0x03, 0x05, 0x00)))
    }

    @Test
    fun isZipFile_withAllZeros_returnsFalse() {
        assertFalse(SVGAParserCore.isZipFile(byteArrayOf(0, 0, 0, 0, 0)))
    }

    // ---- parseProtobuf tests ----

    @Test
    fun parseProtobuf_withValidData_returnsVideoEntity() = runTest {
        val movieEntity = MovieEntity(
            version = "2.0",
            params = MovieParams(
                viewBoxWidth = 300f,
                viewBoxHeight = 300f,
                fps = 24,
                frames = 60
            )
        )
        val protoBytes = movieEntity.encodeToByteArray()

        val core = createCore(inflateFn = { protoBytes })

        val result = core.parseProtobuf(byteArrayOf(0x01)) // dummy compressed input
        assertTrue(result.isSuccess)

        val entity = result.getOrThrow()
        assertEquals("2.0", entity.version)
        assertEquals(300f, entity.videoSize.width)
        assertEquals(300f, entity.videoSize.height)
        assertEquals(24, entity.fps)
        assertEquals(60, entity.frames)
    }

    @Test
    fun parseProtobuf_withSprites_preservesSpriteList() = runTest {
        val movieEntity = MovieEntity(
            version = "1.0",
            params = MovieParams(viewBoxWidth = 100f, viewBoxHeight = 100f, fps = 20, frames = 10),
            sprites = listOf(
                SpriteEntity(imageKey = "avatar"),
                SpriteEntity(imageKey = "badge")
            )
        )
        val protoBytes = movieEntity.encodeToByteArray()

        val core = createCore(inflateFn = { protoBytes })
        val result = core.parseProtobuf(byteArrayOf(0x01))
        assertTrue(result.isSuccess, "parseProtobuf failed: ${result.exceptionOrNull()?.message} cause: ${result.exceptionOrNull()?.cause}")

        val entity = result.getOrThrow()
        assertEquals(2, entity.spriteList.size)
        assertEquals("avatar", entity.spriteList[0].imageKey)
        assertEquals("badge", entity.spriteList[1].imageKey)
        assertEquals("1.0", entity.version)
        assertEquals(100f, entity.videoSize.width)
        assertEquals(20, entity.fps)
        assertEquals(10, entity.frames)
    }

    @Test
    fun parseProtobuf_withImagesMap_decodesCorrectly() = runTest {
        val movieEntity = MovieEntity(
            version = "2.0",
            params = MovieParams(viewBoxWidth = 200f, viewBoxHeight = 200f, fps = 30, frames = 60),
            images = mapOf(
                "avatar" to pbandk.ByteArr(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D)),
                "badge" to pbandk.ByteArr(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 0x00))
            )
        )
        val protoBytes = movieEntity.encodeToByteArray()

        // Verify round-trip: encode → decode
        val decoded = MovieEntity.decodeFromByteArray(protoBytes)
        assertEquals(2, decoded.images.size)
        assertTrue(decoded.images.containsKey("avatar"))
        assertTrue(decoded.images.containsKey("badge"))
        assertEquals(5, decoded.images["avatar"]!!.array.size)
        assertEquals(5, decoded.images["badge"]!!.array.size)
    }

    @Test
    fun parseProtobuf_withInflateFailure_returnsFailure() = runTest {
        val core = createCore(inflateFn = { throw RuntimeException("inflate failed") })

        val result = core.parseProtobuf(byteArrayOf(0x01, 0x02))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("inflate failed") == true)
    }

    @Test
    fun parseProtobuf_withCorruptProtoData_returnsFailure() = runTest {
        val core = createCore(inflateFn = { byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()) })

        val result = core.parseProtobuf(byteArrayOf(0x01))
        assertTrue(result.isFailure)
    }

    // ---- parseZip tests ----

    @Test
    fun parseZip_withMovieBinary_parsesSuccessfully() = runTest {
        val movieEntity = MovieEntity(
            version = "1.5",
            params = MovieParams(viewBoxWidth = 400f, viewBoxHeight = 400f, fps = 25, frames = 50)
        )
        val protoBytes = movieEntity.encodeToByteArray()
        val zipBytes = StoredZipBuilder.build(mapOf("movie.binary" to protoBytes))

        val core = createCore()
        val result = core.parseZip(zipBytes)
        assertTrue(result.isSuccess)

        val entity = result.getOrThrow()
        assertEquals("1.5", entity.version)
        assertEquals(400f, entity.videoSize.width)
        assertEquals(400f, entity.videoSize.height)
        assertEquals(25, entity.fps)
        assertEquals(50, entity.frames)
    }

    @Test
    fun parseZip_withoutMovieBinary_returnsFailure() = runTest {
        val zipBytes = StoredZipBuilder.build(mapOf("other.txt" to "hello".encodeToByteArray()))

        val core = createCore()
        val result = core.parseZip(zipBytes)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("movie.binary") == true)
    }

    @Test
    fun parseZip_withImageEntries_decodesImages() = runTest {
        val movieEntity = MovieEntity(
            version = "1.0",
            params = MovieParams(viewBoxWidth = 100f, viewBoxHeight = 100f, fps = 20, frames = 10)
        )
        val protoBytes = movieEntity.encodeToByteArray()
        val fakeImageData = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47) // PNG-like

        val zipBytes = StoredZipBuilder.build(mapOf(
            "movie.binary" to protoBytes,
            "avatar.png" to fakeImageData
        ))

        var decodeCalled = false
        val core = createCore(decodeBitmapFn = { bytes ->
            if (bytes.contentEquals(fakeImageData)) {
                decodeCalled = true
            }
            null // Can't create real ImageBitmap in commonTest
        })

        val result = core.parseZip(zipBytes)
        assertTrue(result.isSuccess)
        assertTrue(decodeCalled, "Bitmap decoder should have been called for image entry")
    }

    // ---- decodeFromBytes routing tests ----

    @Test
    fun decodeFromBytes_withZipMagic_routesToParseZip() = runTest {
        val movieEntity = MovieEntity(
            version = "1.0",
            params = MovieParams(viewBoxWidth = 100f, viewBoxHeight = 100f, fps = 20, frames = 10)
        )
        val protoBytes = movieEntity.encodeToByteArray()
        val zipBytes = StoredZipBuilder.build(mapOf("movie.binary" to protoBytes))

        val core = createCore()
        val result = core.decodeFromBytes(zipBytes)
        assertTrue(result.isSuccess)
        assertEquals("1.0", result.getOrThrow().version)
    }

    @Test
    fun decodeFromBytes_withoutZipMagic_routesToParseProtobuf() = runTest {
        val movieEntity = MovieEntity(
            version = "2.0",
            params = MovieParams(viewBoxWidth = 200f, viewBoxHeight = 200f, fps = 30, frames = 90)
        )
        val protoBytes = movieEntity.encodeToByteArray()

        val core = createCore(inflateFn = { protoBytes })

        // Non-ZIP bytes (doesn't start with PK\x03\x04)
        val result = core.decodeFromBytes(byteArrayOf(0x78, 0x9C.toByte(), 0x01))
        assertTrue(result.isSuccess)
        assertEquals("2.0", result.getOrThrow().version)
    }

    @Test
    fun decodeFromBytes_withEmptyBytes_returnsFailure() = runTest {
        val core = createCore(inflateFn = { throw RuntimeException("cannot inflate empty") })

        val result = core.decodeFromBytes(byteArrayOf())
        assertTrue(result.isFailure)
    }

    // ---- decodeFromURL tests (via SVGALoaderCore) ----

    @Test
    fun decodeFromURL_cacheHit_returnsCachedResult() = runTest {
        val movieEntity = MovieEntity(
            version = "1.0",
            params = MovieParams(viewBoxWidth = 100f, viewBoxHeight = 100f, fps = 20, frames = 10)
        )
        val protoBytes = movieEntity.encodeToByteArray()
        val zipBytes = StoredZipBuilder.build(mapOf("movie.binary" to protoBytes))

        val cache = FakeSVGACache()
        cache.put(cache.cacheKey("https://example.com/anim.svga"), zipBytes)

        var downloadCalled = false
        val loader = createLoader(
            downloadFn = { downloadCalled = true; zipBytes },
            cache = cache
        )

        val result = loader.decodeFromURL("https://example.com/anim.svga")
        assertTrue(result.isSuccess)
        assertEquals("1.0", result.getOrThrow().version)
        assertFalse(downloadCalled, "Should not download when cache hit")
    }

    @Test
    fun decodeFromURL_cacheMiss_downloadsAndCaches() = runTest {
        val movieEntity = MovieEntity(
            version = "2.0",
            params = MovieParams(viewBoxWidth = 200f, viewBoxHeight = 200f, fps = 30, frames = 60)
        )
        val protoBytes = movieEntity.encodeToByteArray()
        val zipBytes = StoredZipBuilder.build(mapOf("movie.binary" to protoBytes))

        val cache = FakeSVGACache()
        val loader = createLoader(
            downloadFn = { zipBytes },
            cache = cache
        )

        val result = loader.decodeFromURL("https://example.com/anim.svga")
        assertTrue(result.isSuccess)
        assertEquals("2.0", result.getOrThrow().version)

        // Verify bytes were cached
        val key = cache.cacheKey("https://example.com/anim.svga")
        assertNotNull(cache.get(key), "Downloaded bytes should be cached")
    }

    @Test
    fun decodeFromURL_cacheReadFailure_clearsAndRedownloads() = runTest {
        val movieEntity = MovieEntity(
            version = "3.0",
            params = MovieParams(viewBoxWidth = 300f, viewBoxHeight = 300f, fps = 24, frames = 48)
        )
        val protoBytes = movieEntity.encodeToByteArray()
        val zipBytes = StoredZipBuilder.build(mapOf("movie.binary" to protoBytes))

        // Cache that fails on first get, then works normally
        val failingCache = object : SVGACache {
            private val store = mutableMapOf<String, ByteArray>()
            private var getCallCount = 0

            override fun cacheKey(url: String): String = "key_${url.hashCode()}"
            override fun get(key: String): ByteArray? {
                getCallCount++
                if (getCallCount == 1) throw RuntimeException("corrupted cache")
                return store[key]
            }
            override fun put(key: String, bytes: ByteArray) { store[key] = bytes }
            override fun remove(key: String) { store.remove(key) }
            override fun clear() { store.clear() }
        }

        val loader = createLoader(
            downloadFn = { zipBytes },
            cache = failingCache
        )

        val result = loader.decodeFromURL("https://example.com/anim.svga")
        assertTrue(result.isSuccess)
        assertEquals("3.0", result.getOrThrow().version)
    }

    @Test
    fun decodeFromURL_downloadFailure_returnsFailure() = runTest {
        val loader = createLoader(
            downloadFn = { throw RuntimeException("network error") },
            cache = FakeSVGACache()
        )

        val result = loader.decodeFromURL("https://example.com/anim.svga")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("network error") == true)
    }

    // ---- decodeFromFile tests (via SVGALoaderCore) ----

    @Test
    fun decodeFromFile_withValidFile_returnsVideoEntity() = runTest {
        val movieEntity = MovieEntity(
            version = "1.0",
            params = MovieParams(viewBoxWidth = 100f, viewBoxHeight = 100f, fps = 20, frames = 10)
        )
        val protoBytes = movieEntity.encodeToByteArray()
        val zipBytes = StoredZipBuilder.build(mapOf("movie.binary" to protoBytes))

        val files = mapOf("/path/to/anim.svga" to zipBytes)
        val loader = createLoader(readFileBytesFn = { files[it] })

        val result = loader.decodeFromFile("/path/to/anim.svga")
        assertTrue(result.isSuccess)
        assertEquals("1.0", result.getOrThrow().version)
    }

    @Test
    fun decodeFromFile_fileNotFound_returnsFailure() = runTest {
        val loader = createLoader(readFileBytesFn = { null })

        val result = loader.decodeFromFile("/nonexistent/file.svga")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("File not found") == true)
    }

    @Test
    fun decodeFromFile_readThrows_returnsFailure() = runTest {
        val loader = createLoader(readFileBytesFn = { throw RuntimeException("I/O error") })

        val result = loader.decodeFromFile("/some/file.svga")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("I/O error") == true)
    }

    // ---- Helpers ----

    private fun createCore(
        inflateFn: (ByteArray) -> ByteArray = { it },
        inflateRawFn: (ByteArray) -> ByteArray = { it },
        decodeBitmapFn: (ByteArray) -> ImageBitmap? = { null }
    ): SVGAParserCore {
        return SVGAParserCore(
            inflateFn = inflateFn,
            inflateRawFn = inflateRawFn,
            decodeBitmapFn = decodeBitmapFn
        )
    }

    private fun createLoader(
        downloadFn: suspend (String) -> ByteArray = { byteArrayOf() },
        readFileBytesFn: (String) -> ByteArray? = { null },
        cache: SVGACache = FakeSVGACache()
    ): SVGALoaderCore {
        val core = createCore()
        return SVGALoaderCore(
            downloadFn = downloadFn,
            readFileBytesFn = readFileBytesFn,
            cache = cache,
            decodeFn = core::decodeFromBytes
        )
    }
}

// ---- Test Fakes ----

private class FakeSVGACache(var failOnGet: Boolean = false) : SVGACache {
    private val store = mutableMapOf<String, ByteArray>()

    override fun cacheKey(url: String): String = "key_${url.hashCode()}"
    override fun get(key: String): ByteArray? {
        if (failOnGet) throw RuntimeException("corrupted cache")
        return store[key]
    }
    override fun put(key: String, bytes: ByteArray) { store[key] = bytes }
    override fun remove(key: String) { store.remove(key) }
    override fun clear() { store.clear() }
}
