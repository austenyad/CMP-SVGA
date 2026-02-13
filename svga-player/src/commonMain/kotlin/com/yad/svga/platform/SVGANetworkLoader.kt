package com.yad.svga.platform

/**
 * Abstraction for downloading raw bytes from a URL.
 *
 * The library ships with a [DefaultNetworkLoader] that uses platform-native APIs
 * (HttpURLConnection on Android, NSURLSession on iOS). To use your own networking
 * stack (OkHttp, Ktor, Retrofit, AFNetworking, etc.), implement this interface
 * and pass it to [SVGAParser] or [rememberSVGAComposition].
 *
 * Example — OkHttp:
 * ```kotlin
 * class OkHttpSVGALoader(private val client: OkHttpClient) : SVGANetworkLoader {
 *     override suspend fun download(url: String): ByteArray {
 *         val request = Request.Builder().url(url).build()
 *         return withContext(Dispatchers.IO) {
 *             client.newCall(request).execute().use { it.body!!.bytes() }
 *         }
 *     }
 * }
 * ```
 *
 * Example — Ktor:
 * ```kotlin
 * class KtorSVGALoader(private val client: HttpClient) : SVGANetworkLoader {
 *     override suspend fun download(url: String): ByteArray {
 *         return client.get(url).body()
 *     }
 * }
 * ```
 */
interface SVGANetworkLoader {
    /**
     * Download the content at [url] and return the raw bytes.
     *
     * Implementations should:
     * - Throw on non-2xx HTTP status codes
     * - Support cancellation via coroutine cancellation
     * - Handle timeouts appropriately
     */
    suspend fun download(url: String): ByteArray
}
