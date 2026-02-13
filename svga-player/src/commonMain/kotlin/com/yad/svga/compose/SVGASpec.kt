package com.yad.svga.compose

/**
 * Describes the source of an SVGA animation, analogous to Lottie's LottieCompositionSpec.
 */
sealed class SVGASpec {
    /** Load from platform assets (Android assets / iOS bundle). */
    data class Asset(val name: String) : SVGASpec()

    /** Load from a remote URL (with caching). */
    data class Url(val url: String) : SVGASpec()

    /** Load from a local file path. */
    data class File(val path: String) : SVGASpec()

    /** Load from raw bytes already in memory. */
    data class Bytes(val data: ByteArray) : SVGASpec() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Bytes) return false
            return data.contentEquals(other.data)
        }
        override fun hashCode(): Int = data.contentHashCode()
    }
}
