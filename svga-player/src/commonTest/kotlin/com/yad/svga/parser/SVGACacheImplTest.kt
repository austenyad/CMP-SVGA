package com.yad.svga.parser

import com.yad.svga.platform.FileSystem
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SVGACacheImplTest {

    // ---- cacheKey tests ----

    @Test
    fun cacheKey_sameUrl_returnsSameKey() {
        val cache = SVGACacheImpl()
        val key1 = cache.cacheKey("https://example.com/anim.svga")
        val key2 = cache.cacheKey("https://example.com/anim.svga")
        assertEquals(key1, key2)
    }

    @Test
    fun cacheKey_differentUrls_returnDifferentKeys() {
        val cache = SVGACacheImpl()
        val key1 = cache.cacheKey("https://example.com/a.svga")
        val key2 = cache.cacheKey("https://example.com/b.svga")
        assertNotEquals(key1, key2)
    }

    @Test
    fun cacheKey_returnsValidMd5Hex() {
        val cache = SVGACacheImpl()
        val key = cache.cacheKey("https://example.com/test.svga")
        // MD5 hex is 32 lowercase hex characters
        assertEquals(32, key.length)
        assertTrue(key.all { it in '0'..'9' || it in 'a'..'f' })
    }

    @Test
    fun cacheKey_emptyUrl_returnsValidKey() {
        val cache = SVGACacheImpl()
        val key = cache.cacheKey("")
        assertEquals(32, key.length)
    }

    // ---- put / get round-trip tests ----

    @Test
    fun putAndGet_roundTrip_returnsOriginalBytes() {
        val fs = FileSystem()
        val cache = SVGACacheImpl(fs)
        val key = cache.cacheKey("https://example.com/anim.svga")
        val data = byteArrayOf(1, 2, 3, 4, 5)

        cache.put(key, data)
        val result = cache.get(key)

        assertNotNull(result)
        assertContentEquals(data, result)

        // Cleanup
        cache.clear()
    }

    @Test
    fun get_nonExistentKey_returnsNull() {
        val cache = SVGACacheImpl()
        val result = cache.get("nonexistent_key_12345678901234")
        assertNull(result)
    }

    @Test
    fun put_overwritesExistingEntry() {
        val fs = FileSystem()
        val cache = SVGACacheImpl(fs)
        val key = cache.cacheKey("https://example.com/anim.svga")

        cache.put(key, byteArrayOf(1, 2, 3))
        cache.put(key, byteArrayOf(4, 5, 6))

        val result = cache.get(key)
        assertNotNull(result)
        assertContentEquals(byteArrayOf(4, 5, 6), result)

        cache.clear()
    }

    // ---- remove tests ----

    @Test
    fun remove_existingEntry_makesGetReturnNull() {
        val fs = FileSystem()
        val cache = SVGACacheImpl(fs)
        val key = cache.cacheKey("https://example.com/anim.svga")

        cache.put(key, byteArrayOf(1, 2, 3))
        cache.remove(key)

        assertNull(cache.get(key))

        cache.clear()
    }

    @Test
    fun remove_nonExistentKey_doesNotThrow() {
        val cache = SVGACacheImpl()
        // Should not throw
        cache.remove("nonexistent_key_12345678901234")
    }

    // ---- clear tests ----

    @Test
    fun clear_removesAllEntries() {
        val fs = FileSystem()
        val cache = SVGACacheImpl(fs)

        val key1 = cache.cacheKey("https://example.com/a.svga")
        val key2 = cache.cacheKey("https://example.com/b.svga")

        cache.put(key1, byteArrayOf(1, 2))
        cache.put(key2, byteArrayOf(3, 4))

        cache.clear()

        assertNull(cache.get(key1))
        assertNull(cache.get(key2))
    }

    @Test
    fun clear_onEmptyCache_doesNotThrow() {
        val cache = SVGACacheImpl()
        // Should not throw
        cache.clear()
    }

    // ---- corrupted cache / edge cases ----

    @Test
    fun get_afterClear_returnsNull() {
        val fs = FileSystem()
        val cache = SVGACacheImpl(fs)
        val key = cache.cacheKey("https://example.com/anim.svga")

        cache.put(key, byteArrayOf(10, 20, 30))
        assertNotNull(cache.get(key))

        cache.clear()
        // After clear, the cache dir is deleted, so get should return null
        assertNull(cache.get(key))
    }

    @Test
    fun put_afterClear_worksNormally() {
        val fs = FileSystem()
        val cache = SVGACacheImpl(fs)
        val key = cache.cacheKey("https://example.com/anim.svga")

        cache.put(key, byteArrayOf(1, 2))
        cache.clear()

        // Put again after clear — should recreate the directory
        cache.put(key, byteArrayOf(3, 4))
        val result = cache.get(key)
        assertNotNull(result)
        assertContentEquals(byteArrayOf(3, 4), result)

        cache.clear()
    }
}
