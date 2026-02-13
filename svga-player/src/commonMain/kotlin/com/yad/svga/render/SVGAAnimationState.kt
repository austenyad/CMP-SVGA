package com.yad.svga.render

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yad.svga.model.SVGAVideoEntity

/**
 * 动画播放完成后的最终显示状态。
 */
enum class FillMode {
    /** 停在最后一帧 */
    Forward,
    /** 回到第一帧 */
    Backward,
    /** 清空画布（currentFrame = -1） */
    Clear
}

/**
 * SVGA 动画状态机，管理播放/暂停/停止、帧索引递增、循环计数、FillMode 和播放范围。
 *
 * 核心方法 [advanceFrame] 由外部动画循环（withFrameNanos）调用，
 * 根据 deltaNanos 累积时间并推进帧索引。
 */
class SVGAAnimationState(
    val videoEntity: SVGAVideoEntity,
    val loops: Int = 0,
    val autoPlay: Boolean = true,
    val fillMode: FillMode = FillMode.Forward,
    val range: IntRange? = null
) {
    /** 播放范围起始帧 */
    val startFrame: Int = range?.first ?: 0

    /** 播放范围结束帧 */
    val endFrame: Int = range?.last ?: (videoEntity.frames - 1)

    /** 当前帧索引（Compose 可观察）。-1 表示画布应被清空。 */
    var currentFrame: Int by mutableIntStateOf(startFrame)
        private set

    /** 是否正在播放（Compose 可观察） */
    var isPlaying: Boolean by mutableStateOf(false)
        private set

    /** 已完成的循环次数 */
    var completedLoops: Int = 0
        private set

    /** 累积的纳秒时间，用于计算何时推进到下一帧 */
    private var accumulatedNanos: Long = 0L

    /** 动画是否已完成（有限循环播放结束） */
    private var isCompleted: Boolean = false

    /** 每帧持续时间（纳秒） */
    private val frameDurationNanos: Long
        get() = if (videoEntity.fps > 0) 1_000_000_000L / videoEntity.fps else 0L

    /** 播放范围内的总帧数 */
    private val totalFramesInRange: Int
        get() = endFrame - startFrame + 1

    /**
     * 播放进度 0.0 ~ 1.0。
     * 当 startFrame == endFrame 时返回 1.0（单帧动画）。
     * 当 currentFrame == -1（Clear 状态）时返回 0.0。
     */
    val progress: Double
        get() {
            if (currentFrame < 0) return 0.0
            val span = endFrame - startFrame
            if (span <= 0) return 1.0
            return (currentFrame - startFrame).toDouble() / span.toDouble()
        }

    /**
     * 开始播放。如果动画已完成，重置状态后重新播放。
     */
    fun play() {
        if (isCompleted) {
            // 重新播放：重置所有状态
            completedLoops = 0
            currentFrame = startFrame
            accumulatedNanos = 0L
            isCompleted = false
        }
        isPlaying = true
    }

    /**
     * 暂停播放，保持当前帧不变。
     */
    fun pause() {
        isPlaying = false
        onPause?.invoke()
    }

    /**
     * 停止播放，根据 FillMode 决定最终帧位置。
     */
    fun stop() {
        isPlaying = false
        accumulatedNanos = 0L
        completedLoops = 0
        isCompleted = false
        when (fillMode) {
            FillMode.Forward -> currentFrame = endFrame
            FillMode.Backward -> currentFrame = startFrame
            FillMode.Clear -> currentFrame = -1
        }
    }

    /**
     * 跳转到指定帧。帧索引会被 clamp 到 [startFrame, endFrame] 范围。
     * 不改变 isPlaying 状态（可在播放或暂停时调用）。
     */
    fun stepToFrame(frame: Int) {
        currentFrame = frame.coerceIn(startFrame, endFrame)
        accumulatedNanos = 0L
    }

    /**
     * 跳转到指定百分比位置。百分比会被 clamp 到 [0.0, 1.0]。
     */
    fun stepToPercentage(percentage: Double) {
        val clamped = percentage.coerceIn(0.0, 1.0)
        val targetFrame = startFrame + (clamped * (endFrame - startFrame)).toInt()
        stepToFrame(targetFrame)
    }

    // ── 回调 stubs（Task 8.3 实现） ──

    var onStep: ((frame: Int, percentage: Double) -> Unit)? = null
    var onFinished: (() -> Unit)? = null
    var onRepeat: (() -> Unit)? = null
    var onPause: (() -> Unit)? = null

    /**
     * 核心帧推进方法，由动画循环（withFrameNanos）调用。
     *
     * 累积 [deltaNanos] 时间，每当累积时间超过一帧的持续时间就推进一帧。
     * 处理循环计数和动画完成逻辑。
     */
    fun advanceFrame(deltaNanos: Long) {
        if (!isPlaying || isCompleted) return

        val frameDur = frameDurationNanos
        if (frameDur <= 0L) return

        accumulatedNanos += deltaNanos

        while (accumulatedNanos >= frameDur && isPlaying && !isCompleted) {
            accumulatedNanos -= frameDur

            if (currentFrame >= endFrame) {
                // 当前循环结束
                completedLoops++
                if (loops > 0 && completedLoops >= loops) {
                    // 有限循环已完成
                    applyFillMode()
                    isPlaying = false
                    isCompleted = true
                    accumulatedNanos = 0L
                    onStep?.invoke(currentFrame, progress)
                    onFinished?.invoke()
                    return
                }
                // 继续下一个循环
                currentFrame = startFrame
                onRepeat?.invoke()
            } else {
                currentFrame++
            }
            onStep?.invoke(currentFrame, progress)
        }
    }

    /**
     * 根据 FillMode 设置动画完成后的最终帧。
     */
    private fun applyFillMode() {
        when (fillMode) {
            FillMode.Forward -> currentFrame = endFrame
            FillMode.Backward -> currentFrame = startFrame
            FillMode.Clear -> currentFrame = -1
        }
    }
}
