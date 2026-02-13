package com.yad.svga.parser

import androidx.compose.ui.graphics.ImageBitmap
import com.yad.svga.model.SVGAVideoEntity
import com.yad.svga.model.toVideoEntity
import com.yad.svga.platform.FileSystem
import com.yad.svga.platform.SVGANetworkLoader
import com.yad.svga.platform.DefaultNetworkLoader
import com.yad.svga.platform.ImageBitmapDecoder
import com.yad.svga.platform.ZlibInflater
import com.yad.svga.proto.MovieEntity
import pbandk.decodeFromByteArray

/**
 * SVGA file parser supporting both Protobuf binary (zlib compressed) and ZIP formats.
 *
 * - Protobuf format: zlib inflate → pbandk decode → SVGAVideoEntity
 * - ZIP format: extract ZIP → read movie.binary + image resources → SVGAVideoEntity
 */
class SVGAParser(
    private val networkLoader: SVGANetworkLoader = DefaultNetworkLoader(),
    private val fileSystem: FileSystem,
    private val bitmapDecoder: ImageBitmapDecoder,
    private val zlibInflater: ZlibInflater,
    private val cache: SVGACache
) {
    // Core logic delegates to SVGAParserCore for testability
    private val core = SVGAParserCore(
        inflateFn = zlibInflater::inflate,
        inflateRawFn = zlibInflater::inflateRaw,
        decodeBitmapFn = bitmapDecoder::decode
    )

    // Loader logic delegates to SVGALoaderCore for testability
    private val loader = SVGALoaderCore(
        downloadFn = networkLoader::download,
        readFileBytesFn = fileSystem::readBytes,
        cache = cache,
        decodeFn = core::decodeFromBytes
    )

    /**
     * Decode SVGA from a URL.
     * Checks cache first; on cache miss, downloads via [SVGANetworkLoader] and caches the bytes.
     * On cache read failure, clears the corrupted entry and re-downloads.
     */
    suspend fun decodeFromURL(url: String): Result<SVGAVideoEntity> = loader.decodeFromURL(url)

    /**
     * Decode SVGA from a local file path.
     * Reads bytes via FileSystem, then delegates to decodeFromBytes.
     */
    suspend fun decodeFromFile(path: String): Result<SVGAVideoEntity> = loader.decodeFromFile(path)

    /**
     * Decode SVGA from raw bytes. Detects format (ZIP vs Protobuf) and routes accordingly.
     */
    suspend fun decodeFromBytes(bytes: ByteArray): Result<SVGAVideoEntity> = core.decodeFromBytes(bytes)

    /**
     * Check if the byte array starts with the ZIP magic number (PK\x03\x04).
     */
    internal fun isZipFile(bytes: ByteArray): Boolean = SVGAParserCore.isZipFile(bytes)

    internal suspend fun parseProtobuf(bytes: ByteArray): Result<SVGAVideoEntity> = core.parseProtobuf(bytes)

    internal suspend fun parseZip(bytes: ByteArray): Result<SVGAVideoEntity> = core.parseZip(bytes)
}

/**
 * Loader logic extracted for cross-platform testability.
 * Handles URL (with caching) and file-based loading, delegating actual parsing to [decodeFn].
 */
internal class SVGALoaderCore(
    private val downloadFn: suspend (String) -> ByteArray,
    private val readFileBytesFn: (String) -> ByteArray?,
    private val cache: SVGACache,
    private val decodeFn: suspend (ByteArray) -> Result<SVGAVideoEntity>
) {
    suspend fun decodeFromURL(url: String): Result<SVGAVideoEntity> {
        return try {
            val key = cache.cacheKey(url)

            // Try cache first
            val cachedBytes = try {
                cache.get(key)
            } catch (_: Exception) {
                // Cache read failed — clear corrupted entry
                cache.remove(key)
                null
            }

            if (cachedBytes != null) {
                return decodeFn(cachedBytes)
            }

            // Cache miss — download
            val bytes = downloadFn(url)
            cache.put(key, bytes)
            decodeFn(bytes)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to decode SVGA from URL: ${e.message}", e))
        }
    }

    suspend fun decodeFromFile(path: String): Result<SVGAVideoEntity> {
        return try {
            val bytes = readFileBytesFn(path)
                ?: return Result.failure(Exception("File not found or unreadable: $path"))
            decodeFn(bytes)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to decode SVGA from file: ${e.message}", e))
        }
    }
}

/**
 * Core parsing logic extracted for cross-platform testability.
 * Accepts functional parameters instead of expect classes.
 */
internal class SVGAParserCore(
    private val inflateFn: (ByteArray) -> ByteArray,
    private val inflateRawFn: (ByteArray) -> ByteArray,
    private val decodeBitmapFn: (ByteArray) -> ImageBitmap?
) {
    companion object {
        /**
         * Check if the byte array starts with the ZIP magic number (PK\x03\x04).
         */
        fun isZipFile(bytes: ByteArray): Boolean {
            return bytes.size > 4 &&
                bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte() &&
                bytes[2] == 0x03.toByte() && bytes[3] == 0x04.toByte()
        }
    }

    suspend fun decodeFromBytes(bytes: ByteArray): Result<SVGAVideoEntity> {
        return try {
            if (isZipFile(bytes)) {
                parseZip(bytes)
            } else {
                parseProtobuf(bytes)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Parse zlib-compressed Protobuf binary format.
     */
    suspend fun parseProtobuf(bytes: ByteArray): Result<SVGAVideoEntity> {
        return try {
            val inflatedBytes = inflateFn(bytes)
            val movieEntity = MovieEntity.decodeFromByteArray(inflatedBytes)
            val imageMap = decodeImages(movieEntity)
            Result.success(movieEntity.toVideoEntity(imageMap))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to parse Protobuf SVGA: ${e.message}", e))
        }
    }

    /**
     * Parse ZIP format SVGA file.
     */
    suspend fun parseZip(bytes: ByteArray): Result<SVGAVideoEntity> {
        return try {
            val entries = ZipReader.readEntries(bytes, rawInflater = inflateRawFn)

            val movieBinaryData = entries["movie.binary"]
                ?: return Result.failure(Exception("ZIP does not contain movie.binary"))

            val movieEntity = MovieEntity.decodeFromByteArray(movieBinaryData)

            val imageMap = mutableMapOf<String, ImageBitmap>()

            // Images from ZIP entries
            for ((name, data) in entries) {
                if (name == "movie.binary" || name == "movie.spec") continue
                if (name.contains("../") || name.contains("/")) continue

                val imageKey = name.substringBeforeLast(".")
                try {
                    decodeBitmapFn(data)?.let { bitmap ->
                        imageMap[imageKey] = bitmap
                    }
                } catch (_: Exception) {
                    // Skip failed image decodes
                }
            }

            // Fall back to proto embedded images for missing keys
            for ((key, byteArr) in movieEntity.images) {
                if (imageMap.containsKey(key)) continue
                val imageBytes = byteArr.array
                if (imageBytes.size < 4) continue
                // Skip audio data (ID3 tag: 0x49 0x44 0x33)
                if (imageBytes[0].toInt() == 0x49 &&
                    imageBytes[1].toInt() == 0x44 &&
                    imageBytes[2].toInt() == 0x33
                ) continue
                try {
                    decodeBitmapFn(imageBytes)?.let { bitmap ->
                        imageMap[key] = bitmap
                    }
                } catch (_: Exception) {
                    // Skip failed image decodes
                }
            }

            Result.success(movieEntity.toVideoEntity(imageMap))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to parse ZIP SVGA: ${e.message}", e))
        }
    }

    private fun decodeImages(movieEntity: MovieEntity): Map<String, ImageBitmap> {
        val imageMap = mutableMapOf<String, ImageBitmap>()
        for ((key, byteArr) in movieEntity.images) {
            val imageBytes = byteArr.array
            if (imageBytes.size < 4) continue
            if (imageBytes[0].toInt() == 0x49 &&
                imageBytes[1].toInt() == 0x44 &&
                imageBytes[2].toInt() == 0x33
            ) continue
            try {
                decodeBitmapFn(imageBytes)?.let { bitmap ->
                    imageMap[key] = bitmap
                }
            } catch (_: Exception) {
                // Skip failed image decodes
            }
        }
        return imageMap
    }
}
