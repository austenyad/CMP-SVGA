package com.yad.svga.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import com.yad.svga.model.SVGAVideoEntity
import com.yad.svga.platform.SVGANetworkLoader
import com.yad.svga.render.FillMode
import com.yad.svga.render.SVGAAnimationState
import com.yad.svga.render.SVGADynamicEntity
import com.yad.svga.render.SVGARenderer
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.isActive

/**
 * Composable that plays an SVGA animation.
 *
 * Renders [videoEntity] frame-by-frame on a Compose Canvas, driven by
 * [androidx.compose.ui.platform.LocalDensity]-synced frame callbacks
 * (`withFrameNanos`).
 *
 * Lifecycle:
 * - Entering Composition: starts playback if [autoPlay] is true (Req 6.2).
 * - Leaving Composition: stops playback and releases resources (Req 6.3).
 *
 * @param videoEntity   The parsed SVGA animation data.
 * @param modifier      Compose modifier for sizing / layout.
 * @param dynamicEntity Optional dynamic element replacements (images, text, drawers).
 * @param loops         Number of loops. 0 = infinite.
 * @param autoPlay      Whether to start playing immediately on enter.
 * @param fillMode      What to show after playback finishes.
 * @param contentScale  How to fit the video into the canvas bounds.
 * @param range         Optional sub-range of frames to play.
 * @param onStep        Called every frame with (frameIndex, progress 0..1).
 * @param onFinished    Called when finite-loop playback completes.
 * @param onRepeat      Called at the start of each new loop iteration.
 * @param onPause       Called when playback is paused.
 */
@Composable
fun SVGAImage(
    videoEntity: SVGAVideoEntity,
    modifier: Modifier = Modifier,
    dynamicEntity: SVGADynamicEntity = SVGADynamicEntity(),
    loops: Int = 0,
    autoPlay: Boolean = true,
    fillMode: FillMode = FillMode.Forward,
    contentScale: ContentScale = ContentScale.Fit,
    range: IntRange? = null,
    onStep: ((frame: Int, percentage: Double) -> Unit)? = null,
    onFinished: (() -> Unit)? = null,
    onRepeat: (() -> Unit)? = null,
    onPause: (() -> Unit)? = null
) {
    // Remember animation state; recreated when key parameters change.
    val animationState = remember(videoEntity, loops, fillMode, range) {
        SVGAAnimationState(
            videoEntity = videoEntity,
            loops = loops,
            autoPlay = autoPlay,
            fillMode = fillMode,
            range = range
        )
    }

    // Wire up callbacks (updated every recomposition without recreating state).
    animationState.onStep = onStep
    animationState.onFinished = onFinished
    animationState.onRepeat = onRepeat
    animationState.onPause = onPause

    // Remember renderer; recreated when the video or dynamic entity changes.
    val renderer = remember(videoEntity, dynamicEntity) {
        SVGARenderer(videoEntity, dynamicEntity)
    }

    // Req 6.2 – auto-play on enter Composition; drive frame loop via withFrameNanos.
    LaunchedEffect(animationState) {
        if (autoPlay) {
            animationState.play()
        }

        var lastFrameTimeNanos = 0L
        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                if (lastFrameTimeNanos != 0L) {
                    val deltaNanos = frameTimeNanos - lastFrameTimeNanos
                    animationState.advanceFrame(deltaNanos)
                }
                lastFrameTimeNanos = frameTimeNanos
            }
        }
    }

    // Req 6.3 – stop playback when leaving Composition.
    DisposableEffect(animationState) {
        onDispose {
            animationState.stop()
        }
    }

    // Draw the current frame.
    // Like the original SVGAImageView (which extends ImageView), the caller must
    // provide sizing via modifier (e.g. fillMaxWidth, size, aspectRatio).
    // Without an explicit size the Canvas will be 0x0, matching the original
    // library's wrap_content behaviour (SVGADrawable has no intrinsic size).
    val currentFrame = animationState.currentFrame
    Canvas(modifier = modifier.clipToBounds()) {
        if (currentFrame >= 0) {
            renderer.drawFrame(this, currentFrame, contentScale)
        }
    }
}

/**
 * Creates and remembers an [SVGAAnimationState] that can be used to control
 * playback externally (play / pause / stop / stepToFrame, etc.).
 *
 * Pass the returned state to the [SVGAImage] overload that accepts a
 * [SVGAAnimationState] parameter.
 *
 * The state is recreated when [videoEntity], [loops], [fillMode], or [range]
 * change. [autoPlay] is only read on initial creation.
 *
 * @param videoEntity The parsed SVGA animation data.
 * @param loops       Number of loops. 0 = infinite.
 * @param autoPlay    Whether to start playing immediately (read on creation only).
 * @param fillMode    What to show after playback finishes.
 * @param range       Optional sub-range of frames to play.
 */
@Composable
fun rememberSVGAState(
    videoEntity: SVGAVideoEntity,
    loops: Int = 0,
    autoPlay: Boolean = true,
    fillMode: FillMode = FillMode.Forward,
    range: IntRange? = null
): SVGAAnimationState {
    return remember(videoEntity, loops, fillMode, range) {
        SVGAAnimationState(
            videoEntity = videoEntity,
            loops = loops,
            autoPlay = autoPlay,
            fillMode = fillMode,
            range = range
        )
    }
}

/**
 * Overload of [SVGAImage] that accepts an externally-provided [SVGAAnimationState],
 * allowing the caller to control playback via [SVGAAnimationState.play],
 * [SVGAAnimationState.pause], [SVGAAnimationState.stepToFrame], etc.
 *
 * Obtain a state instance with [rememberSVGAState].
 *
 * Lifecycle behaviour is identical to the primary overload:
 * - Entering Composition: starts the frame loop (auto-plays if [state.autoPlay]).
 * - Leaving Composition: stops playback.
 *
 * @param state         Externally-created animation state.
 * @param modifier      Compose modifier for sizing / layout.
 * @param dynamicEntity Optional dynamic element replacements.
 * @param contentScale  How to fit the video into the canvas bounds.
 * @param onStep        Called every frame with (frameIndex, progress 0..1).
 * @param onFinished    Called when finite-loop playback completes.
 * @param onRepeat      Called at the start of each new loop iteration.
 * @param onPause       Called when playback is paused.
 */
@Composable
fun SVGAImage(
    state: SVGAAnimationState,
    modifier: Modifier = Modifier,
    dynamicEntity: SVGADynamicEntity = SVGADynamicEntity(),
    contentScale: ContentScale = ContentScale.Fit,
    onStep: ((frame: Int, percentage: Double) -> Unit)? = null,
    onFinished: (() -> Unit)? = null,
    onRepeat: (() -> Unit)? = null,
    onPause: (() -> Unit)? = null
) {
    // Wire up callbacks (updated every recomposition without recreating state).
    state.onStep = onStep
    state.onFinished = onFinished
    state.onRepeat = onRepeat
    state.onPause = onPause

    val renderer = remember(state.videoEntity, dynamicEntity) {
        SVGARenderer(state.videoEntity, dynamicEntity)
    }

    // Drive frame loop via withFrameNanos; auto-play if configured.
    LaunchedEffect(state) {
        if (state.autoPlay) {
            state.play()
        }

        var lastFrameTimeNanos = 0L
        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                if (lastFrameTimeNanos != 0L) {
                    val deltaNanos = frameTimeNanos - lastFrameTimeNanos
                    state.advanceFrame(deltaNanos)
                }
                lastFrameTimeNanos = frameTimeNanos
            }
        }
    }

    // Stop playback when leaving Composition.
    DisposableEffect(state) {
        onDispose {
            state.stop()
        }
    }

    // Draw the current frame.
    val currentFrame = state.currentFrame
    Canvas(modifier = modifier.clipToBounds()) {
        if (currentFrame >= 0) {
            renderer.drawFrame(this, currentFrame, contentScale)
        }
    }
}



/**
 * Simplified SVGAImage that loads and plays an SVGA animation from a [SVGASpec].
 *
 * This is the simplest way to display an SVGA animation, analogous to Lottie's
 * one-liner API. Handles loading, parsing, and playback internally.
 *
 * Usage:
 * ```kotlin
 * SVGAImage(
 *     spec = SVGASpec.Asset("angel.svga"),
 *     modifier = Modifier.fillMaxSize()
 * )
 * ```
 *
 * For more control (external playback, dynamic replacement), use the overloads
 * that accept [SVGAVideoEntity] or [SVGAAnimationState] directly.
 *
 * @param spec          The source of the SVGA animation.
 * @param modifier      Compose modifier for sizing / layout.
 * @param networkLoader Optional custom network loader (OkHttp, Ktor, etc.).
 *   Pass null to use the platform default (HttpURLConnection / NSURLSession).
 * @param dynamicEntity Optional dynamic element replacements.
 * @param loops         Number of loops. 0 = infinite.
 * @param autoPlay      Whether to start playing immediately.
 * @param fillMode      What to show after playback finishes.
 * @param contentScale  How to fit the video into the canvas bounds.
 * @param range         Optional sub-range of frames to play.
 * @param loading       Optional composable shown while loading. Pass null to show nothing.
 * @param failure       Optional composable shown on error. Pass null to show nothing.
 * @param onStep        Called every frame with (frameIndex, progress 0..1).
 * @param onFinished    Called when finite-loop playback completes.
 * @param onRepeat      Called at the start of each new loop iteration.
 * @param onPause       Called when playback is paused.
 */
@Composable
fun SVGAImage(
    spec: SVGASpec,
    modifier: Modifier = Modifier,
    networkLoader: SVGANetworkLoader? = null,
    dynamicEntity: SVGADynamicEntity = SVGADynamicEntity(),
    loops: Int = 0,
    autoPlay: Boolean = true,
    fillMode: FillMode = FillMode.Forward,
    contentScale: ContentScale = ContentScale.Fit,
    range: IntRange? = null,
    loading: (@Composable () -> Unit)? = null,
    failure: (@Composable (error: String) -> Unit)? = null,
    onStep: ((frame: Int, percentage: Double) -> Unit)? = null,
    onFinished: (() -> Unit)? = null,
    onRepeat: (() -> Unit)? = null,
    onPause: (() -> Unit)? = null
) {
    val composition = rememberSVGAComposition(spec, networkLoader)

    when {
        composition.isLoading -> {
            if (loading != null) {
                Box(modifier = modifier, contentAlignment = Alignment.Center) {
                    loading()
                }
            }
        }
        composition.isFailure -> {
            if (failure != null) {
                Box(modifier = modifier, contentAlignment = Alignment.Center) {
                    failure(composition.error ?: "Unknown error")
                }
            }
        }
        composition.value != null -> {
            SVGAImage(
                videoEntity = composition.value!!,
                modifier = modifier,
                dynamicEntity = dynamicEntity,
                loops = loops,
                autoPlay = autoPlay,
                fillMode = fillMode,
                contentScale = contentScale,
                range = range,
                onStep = onStep,
                onFinished = onFinished,
                onRepeat = onRepeat,
                onPause = onPause
            )
        }
    }
}
