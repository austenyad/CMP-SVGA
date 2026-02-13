package com.yad.svga.platform

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionDataTask
import platform.Foundation.dataTaskWithURL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class DefaultNetworkLoader actual constructor() : SVGANetworkLoader {

    actual override suspend fun download(url: String): ByteArray =
        suspendCancellableCoroutine { continuation ->
            val nsUrl = NSURL.URLWithString(url)
                ?: run {
                    continuation.resumeWithException(
                        RuntimeException("Invalid URL: $url")
                    )
                    return@suspendCancellableCoroutine
                }

            val task: NSURLSessionDataTask = NSURLSession.sharedSession.dataTaskWithURL(nsUrl) {
                    data: NSData?, response: platform.Foundation.NSURLResponse?, error: NSError? ->

                if (error != null) {
                    continuation.resumeWithException(
                        RuntimeException("Download failed: ${error.localizedDescription}")
                    )
                    return@dataTaskWithURL
                }

                val httpResponse = response as? NSHTTPURLResponse
                if (httpResponse != null && httpResponse.statusCode !in 200..299) {
                    continuation.resumeWithException(
                        RuntimeException("HTTP ${httpResponse.statusCode}")
                    )
                    return@dataTaskWithURL
                }

                if (data == null || data.length.toInt() == 0) {
                    continuation.resumeWithException(
                        RuntimeException("Empty response from $url")
                    )
                    return@dataTaskWithURL
                }

                continuation.resume(data.toByteArray())
            }

            continuation.invokeOnCancellation {
                task.cancel()
            }

            task.resume()
        }
}
