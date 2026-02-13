package com.yad.svga.platform

/**
 * Default platform-native HTTP downloader implementing [SVGANetworkLoader].
 *
 * - Android: uses HttpURLConnection
 * - iOS: uses NSURLSession
 *
 * This is used automatically when no custom [SVGANetworkLoader] is provided.
 */
expect class DefaultNetworkLoader() : SVGANetworkLoader {
    override suspend fun download(url: String): ByteArray
}
