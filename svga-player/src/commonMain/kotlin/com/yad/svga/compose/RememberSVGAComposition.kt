package com.yad.svga.compose

import androidx.compose.runtime.*
import com.yad.svga.model.SVGAVideoEntity
import com.yad.svga.parser.SVGACacheImpl
import com.yad.svga.parser.SVGAParser
import com.yad.svga.platform.AssetLoader
import com.yad.svga.platform.DefaultNetworkLoader
import com.yad.svga.platform.FileSystem
import com.yad.svga.platform.ImageBitmapDecoder
import com.yad.svga.platform.SVGANetworkLoader
import com.yad.svga.platform.ZlibInflater

/**
 * SVGA composition loading result, analogous to Lottie's LottieCompositionResult.
 */
@Stable
class SVGACompositionResult internal constructor() {
    /** The loaded video entity, null while loading or on error. */
    var value: SVGAVideoEntity? by mutableStateOf(null)
        internal set

    /** Whether the composition is currently loading. */
    var isLoading: Boolean by mutableStateOf(true)
        internal set

    /** Error message if loading failed, null otherwise. */
    var error: String? by mutableStateOf(null)
        internal set

    /** Whether loading completed successfully. */
    val isSuccess: Boolean get() = value != null && error == null

    /** Whether loading failed. */
    val isFailure: Boolean get() = error != null
}

/**
 * Remember and load an SVGA composition from the given [spec].
 *
 * Analogous to Lottie's `rememberLottieComposition`. Internally creates an [SVGAParser]
 * with default platform dependencies and handles async loading.
 *
 * The composition is reloaded when [spec] changes.
 *
 * @param spec The source specification for the SVGA animation.
 * @param networkLoader Optional custom network loader. Pass your own OkHttp/Ktor/etc.
 *   implementation, or leave null to use the platform default (HttpURLConnection / NSURLSession).
 * @return [SVGACompositionResult] with loading state and the parsed entity.
 */
@Composable
fun rememberSVGAComposition(
    spec: SVGASpec,
    networkLoader: SVGANetworkLoader? = null
): SVGACompositionResult {
    val result = remember { SVGACompositionResult() }

    val parser = remember(networkLoader) {
        SVGAParser(
            networkLoader = networkLoader ?: DefaultNetworkLoader(),
            fileSystem = FileSystem(),
            bitmapDecoder = ImageBitmapDecoder(),
            zlibInflater = ZlibInflater(),
            cache = SVGACacheImpl()
        )
    }

    val assetLoader = remember { AssetLoader() }

    LaunchedEffect(spec) {
        result.isLoading = true
        result.error = null
        result.value = null

        val loadResult: Result<SVGAVideoEntity> = when (spec) {
            is SVGASpec.Asset -> {
                try {
                    val bytes = assetLoader.loadBytes(spec.name)
                    parser.decodeFromBytes(bytes)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
            is SVGASpec.Url -> parser.decodeFromURL(spec.url)
            is SVGASpec.File -> parser.decodeFromFile(spec.path)
            is SVGASpec.Bytes -> parser.decodeFromBytes(spec.data)
        }

        loadResult
            .onSuccess { result.value = it }
            .onFailure { result.error = it.message ?: "Unknown error" }

        result.isLoading = false
    }

    return result
}
