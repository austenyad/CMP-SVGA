package com.yad.svga.render

import androidx.compose.ui.geometry.Size
import com.yad.svga.model.SVGAAudioEntity
import com.yad.svga.model.SVGAVideoEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SVGAAnimationStateTest {

    /** 创建一个简单的测试用 SVGAVideoEntity */
    private fun makeEntity(fps: Int = 20, frames: Int = 10): SVGAVideoEntity =
        SVGAVideoEntity(
            version = "2.0",
            videoSize = Size(100f, 100f),
            fps = fps,
            frames = frames,
            spriteList = emptyList(),
            imageMap = emptyMap(),
            audioList = emptyList()
        )

    /** 一帧的纳秒数 */
    private fun frameDurationNanos(fps: Int): Long = 1_000_000_000L / fps

    // ── play / pause / stop 状态转换 ──────────────────────────────────

    @Test
    fun initialStateIsNotPlaying() {
        val state = SVGAAnimationState(makeEntity())
        assertFalse(state.isPlaying)
        assertEquals(0, state.currentFrame)
    }

    @Test
    fun playSetsIsPlayingTrue() {
        val state = SVGAAnimationState(makeEntity())
        state.play()
        assertTrue(state.isPlaying)
    }

    @Test
    fun pauseSetsIsPlayingFalse() {
        val state = SVGAAnimationState(makeEntity())
        state.play()
        state.pause()
        assertFalse(state.isPlaying)
    }

    @Test
    fun pauseKeepsCurrentFrame() {
        val state = SVGAAnimationState(makeEntity(fps = 20, frames = 10))
        state.play()
        // Advance 3 frames
        val dur = frameDurationNanos(20)
        state.advanceFrame(dur * 3)
        val frameBeforePause = state.currentFrame
        state.pause()
        assertEquals(frameBeforePause, state.currentFrame)
    }

    @Test
    fun stopWithFillModeForwardGoesToEndFrame() {
        val state = SVGAAnimationState(makeEntity(frames = 10), fillMode = FillMode.Forward)
        state.play()
        state.stop()
        assertFalse(state.isPlaying)
        assertEquals(9, state.currentFrame) // endFrame = frames - 1
    }

    @Test
    fun stopWithFillModeBackwardGoesToStartFrame() {
        val state = SVGAAnimationState(makeEntity(frames = 10), fillMode = FillMode.Backward)
        state.play()
        state.advanceFrame(frameDurationNanos(20) * 5)
        state.stop()
        assertFalse(state.isPlaying)
        assertEquals(0, state.currentFrame)
    }

    @Test
    fun stopWithFillModeClearSetsFrameToMinusOne() {
        val state = SVGAAnimationState(makeEntity(frames = 10), fillMode = FillMode.Clear)
        state.play()
        state.stop()
        assertFalse(state.isPlaying)
        assertEquals(-1, state.currentFrame)
    }

    // ── advanceFrame 帧推进 ──────────────────────────────────────────

    @Test
    fun advanceFrameByExactlyOneFrameDuration() {
        val fps = 20
        val state = SVGAAnimationState(makeEntity(fps = fps, frames = 10), loops = 0)
        state.play()
        state.advanceFrame(frameDurationNanos(fps))
        assertEquals(1, state.currentFrame)
    }

    @Test
    fun advanceFrameByMultipleFrames() {
        val fps = 20
        val state = SVGAAnimationState(makeEntity(fps = fps, frames = 10), loops = 0)
        state.play()
        state.advanceFrame(frameDurationNanos(fps) * 5)
        assertEquals(5, state.currentFrame)
    }

    @Test
    fun advanceFrameAccumulatesPartialTime() {
        val fps = 20
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(makeEntity(fps = fps, frames = 10), loops = 0)
        state.play()
        // Advance by half a frame — should not move
        state.advanceFrame(dur / 2)
        assertEquals(0, state.currentFrame)
        // Advance by another half + a bit — should move to frame 1
        state.advanceFrame(dur / 2)
        assertEquals(1, state.currentFrame)
    }

    @Test
    fun advanceFrameDoesNothingWhenNotPlaying() {
        val state = SVGAAnimationState(makeEntity(fps = 20, frames = 10))
        // Not playing, so advanceFrame should be a no-op
        state.advanceFrame(frameDurationNanos(20) * 5)
        assertEquals(0, state.currentFrame)
    }

    // ── 无限循环 ─────────────────────────────────────────────────────

    @Test
    fun infiniteLoopWrapsAround() {
        val fps = 20
        val frames = 5
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(makeEntity(fps = fps, frames = frames), loops = 0)
        state.play()
        // Advance through all frames (0→1→2→3→4, then wrap to 0)
        // Frame 0 is the starting frame. We need 5 advances to reach endFrame and wrap.
        state.advanceFrame(dur * 5)
        // After 5 frame advances from frame 0:
        // 0→1, 1→2, 2→3, 3→4, 4→wrap→0
        assertEquals(0, state.currentFrame)
        assertTrue(state.isPlaying) // still playing (infinite)
    }

    // ── 有限循环 ─────────────────────────────────────────────────────

    @Test
    fun finiteLoopStopsAfterSpecifiedLoops() {
        val fps = 20
        val frames = 5 // frames 0..4
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(
            makeEntity(fps = fps, frames = frames),
            loops = 2,
            fillMode = FillMode.Forward
        )
        state.play()
        // 1st loop: 5 advances (0→1→2→3→4, then at endFrame completedLoops=1, wrap to 0)
        // 2nd loop: 5 advances (0→1→2→3→4, then at endFrame completedLoops=2, stop)
        state.advanceFrame(dur * 10)
        assertFalse(state.isPlaying)
        assertEquals(2, state.completedLoops)
        assertEquals(4, state.currentFrame) // FillMode.Forward → endFrame
    }

    @Test
    fun singleLoopStopsAfterOneLoop() {
        val fps = 10
        val frames = 3 // frames 0..2
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(
            makeEntity(fps = fps, frames = frames),
            loops = 1,
            fillMode = FillMode.Forward
        )
        state.play()
        // Advance enough to complete 1 loop: 0→1→2, then at endFrame → completedLoops=1 → stop
        state.advanceFrame(dur * 3)
        assertFalse(state.isPlaying)
        assertEquals(1, state.completedLoops)
        assertEquals(2, state.currentFrame)
    }

    // ── FillMode 完成后行为 ──────────────────────────────────────────

    @Test
    fun fillModeForwardOnCompletion() {
        val fps = 10
        val frames = 3
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(
            makeEntity(fps = fps, frames = frames),
            loops = 1,
            fillMode = FillMode.Forward
        )
        state.play()
        state.advanceFrame(dur * 10) // more than enough
        assertEquals(2, state.currentFrame) // endFrame
    }

    @Test
    fun fillModeBackwardOnCompletion() {
        val fps = 10
        val frames = 3
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(
            makeEntity(fps = fps, frames = frames),
            loops = 1,
            fillMode = FillMode.Backward
        )
        state.play()
        state.advanceFrame(dur * 10)
        assertEquals(0, state.currentFrame) // startFrame
    }

    @Test
    fun fillModeClearOnCompletion() {
        val fps = 10
        val frames = 3
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(
            makeEntity(fps = fps, frames = frames),
            loops = 1,
            fillMode = FillMode.Clear
        )
        state.play()
        state.advanceFrame(dur * 10)
        assertEquals(-1, state.currentFrame)
    }

    // ── 播放范围约束 ─────────────────────────────────────────────────

    @Test
    fun rangeConstrainsStartAndEndFrame() {
        val state = SVGAAnimationState(
            makeEntity(fps = 20, frames = 20),
            range = 5..10
        )
        assertEquals(5, state.startFrame)
        assertEquals(10, state.endFrame)
        assertEquals(5, state.currentFrame) // starts at range.first
    }

    @Test
    fun rangePlaybackStaysWithinBounds() {
        val fps = 20
        val frames = 20
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(
            makeEntity(fps = fps, frames = frames),
            loops = 0,
            range = 5..10
        )
        state.play()
        // Advance through the range: 5→6→7→8→9→10, then wrap to 5
        for (i in 0 until 20) {
            state.advanceFrame(dur)
            assertTrue(
                state.currentFrame in 5..10,
                "Frame ${state.currentFrame} out of range 5..10 at step $i"
            )
        }
    }

    @Test
    fun rangeWithFiniteLoops() {
        val fps = 20
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(
            makeEntity(fps = fps, frames = 20),
            loops = 1,
            fillMode = FillMode.Forward,
            range = 3..7
        )
        state.play()
        // Range has 5 frames: 3,4,5,6,7. One loop = 5 frame advances.
        state.advanceFrame(dur * 10) // more than enough
        assertFalse(state.isPlaying)
        assertEquals(7, state.currentFrame) // endFrame of range
    }

    // ── progress 计算 ────────────────────────────────────────────────

    @Test
    fun progressAtStartIsZero() {
        val state = SVGAAnimationState(makeEntity(fps = 20, frames = 10))
        assertEquals(0.0, state.progress, 0.001)
    }

    @Test
    fun progressAtEndIsOne() {
        val fps = 20
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(
            makeEntity(fps = fps, frames = 10),
            loops = 1,
            fillMode = FillMode.Forward
        )
        state.play()
        state.advanceFrame(dur * 20) // complete
        assertEquals(1.0, state.progress, 0.001)
    }

    @Test
    fun progressMidway() {
        val fps = 20
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(makeEntity(fps = fps, frames = 10), loops = 0)
        state.play()
        // Advance to frame 4 (out of 0..9)
        state.advanceFrame(dur * 4)
        // progress = (4 - 0) / (9 - 0) ≈ 0.444
        assertEquals(4.0 / 9.0, state.progress, 0.001)
    }

    @Test
    fun progressWithRange() {
        val fps = 20
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(
            makeEntity(fps = fps, frames = 20),
            loops = 0,
            range = 5..10
        )
        state.play()
        // Advance 3 frames: 5→6→7→8
        state.advanceFrame(dur * 3)
        // progress = (8 - 5) / (10 - 5) = 3/5 = 0.6
        assertEquals(0.6, state.progress, 0.001)
    }

    @Test
    fun progressWhenClearIsZero() {
        val state = SVGAAnimationState(
            makeEntity(fps = 20, frames = 10),
            fillMode = FillMode.Clear
        )
        state.play()
        state.stop()
        assertEquals(-1, state.currentFrame)
        assertEquals(0.0, state.progress, 0.001)
    }

    // ── play after completion 重新播放 ───────────────────────────────

    @Test
    fun playAfterCompletionResetsAndPlays() {
        val fps = 20
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(
            makeEntity(fps = fps, frames = 5),
            loops = 1,
            fillMode = FillMode.Forward
        )
        state.play()
        state.advanceFrame(dur * 10) // complete
        assertFalse(state.isPlaying)
        assertEquals(4, state.currentFrame)

        // Play again
        state.play()
        assertTrue(state.isPlaying)
        assertEquals(0, state.currentFrame) // reset to startFrame
        assertEquals(0, state.completedLoops)
    }

    // ── 单帧动画 ─────────────────────────────────────────────────────

    @Test
    fun singleFrameAnimationProgress() {
        val state = SVGAAnimationState(makeEntity(fps = 20, frames = 1))
        // frames=1 → startFrame=0, endFrame=0, span=0 → progress=1.0
        assertEquals(1.0, state.progress, 0.001)
    }

    // ── stepToFrame ────────────────────────────────────────────────

    @Test
    fun stepToFrameWithValidIndex() {
        val state = SVGAAnimationState(makeEntity(fps = 20, frames = 10))
        state.stepToFrame(5)
        assertEquals(5, state.currentFrame)
    }

    @Test
    fun stepToFrameClampsAboveEndFrame() {
        val state = SVGAAnimationState(makeEntity(fps = 20, frames = 10))
        state.stepToFrame(100)
        assertEquals(9, state.currentFrame) // endFrame = 9
    }

    @Test
    fun stepToFrameClampsBelowStartFrame() {
        val state = SVGAAnimationState(makeEntity(fps = 20, frames = 10))
        state.stepToFrame(-5)
        assertEquals(0, state.currentFrame) // startFrame = 0
    }

    @Test
    fun stepToFrameClampsToRange() {
        val state = SVGAAnimationState(
            makeEntity(fps = 20, frames = 20),
            range = 5..10
        )
        state.stepToFrame(2) // below range start
        assertEquals(5, state.currentFrame)
        state.stepToFrame(15) // above range end
        assertEquals(10, state.currentFrame)
        state.stepToFrame(7) // within range
        assertEquals(7, state.currentFrame)
    }

    @Test
    fun stepToFrameDoesNotChangeIsPlaying() {
        val state = SVGAAnimationState(makeEntity(fps = 20, frames = 10))
        // Not playing → stepToFrame → still not playing
        assertFalse(state.isPlaying)
        state.stepToFrame(5)
        assertFalse(state.isPlaying)

        // Playing → stepToFrame → still playing
        state.play()
        assertTrue(state.isPlaying)
        state.stepToFrame(3)
        assertTrue(state.isPlaying)
    }

    @Test
    fun stepToFrameResetsAccumulatedTime() {
        val fps = 20
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(makeEntity(fps = fps, frames = 10), loops = 0)
        state.play()
        // Accumulate half a frame
        state.advanceFrame(dur / 2)
        assertEquals(0, state.currentFrame)
        // Jump to frame 5 — should reset accumulated time
        state.stepToFrame(5)
        assertEquals(5, state.currentFrame)
        // Advance by half a frame again — should NOT move (accumulated was reset)
        state.advanceFrame(dur / 2)
        assertEquals(5, state.currentFrame)
    }

    // ── stepToPercentage ─────────────────────────────────────────────

    @Test
    fun stepToPercentageAtZero() {
        val state = SVGAAnimationState(makeEntity(fps = 20, frames = 10))
        state.stepToPercentage(0.0)
        assertEquals(0, state.currentFrame) // startFrame
    }

    @Test
    fun stepToPercentageAtFiftyPercent() {
        val state = SVGAAnimationState(makeEntity(fps = 20, frames = 10))
        // 50% of range 0..9 → 0 + (0.5 * 9).toInt() = 4
        state.stepToPercentage(0.5)
        assertEquals(4, state.currentFrame)
    }

    @Test
    fun stepToPercentageAtHundredPercent() {
        val state = SVGAAnimationState(makeEntity(fps = 20, frames = 10))
        // 100% of range 0..9 → 0 + (1.0 * 9).toInt() = 9
        state.stepToPercentage(1.0)
        assertEquals(9, state.currentFrame) // endFrame
    }

    @Test
    fun stepToPercentageClampsNegative() {
        val state = SVGAAnimationState(makeEntity(fps = 20, frames = 10))
        state.stepToPercentage(-0.5)
        assertEquals(0, state.currentFrame) // clamped to 0%
    }

    @Test
    fun stepToPercentageClampsAboveOne() {
        val state = SVGAAnimationState(makeEntity(fps = 20, frames = 10))
        state.stepToPercentage(1.5)
        assertEquals(9, state.currentFrame) // clamped to 100%
    }

    @Test
    fun stepToPercentageWithRange() {
        val state = SVGAAnimationState(
            makeEntity(fps = 20, frames = 20),
            range = 5..15
        )
        // 50% of range 5..15 → 5 + (0.5 * 10).toInt() = 5 + 5 = 10
        state.stepToPercentage(0.5)
        assertEquals(10, state.currentFrame)
    }

    // ── single frame animation ───────────────────────────────────────

    @Test
    fun singleFrameFiniteLoopCompletes() {
        val fps = 20
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(
            makeEntity(fps = fps, frames = 1),
            loops = 1,
            fillMode = FillMode.Forward
        )
        state.play()
        state.advanceFrame(dur)
        assertFalse(state.isPlaying)
        assertEquals(0, state.currentFrame)
    }

    // ── 播放回调 onStep / onFinished / onRepeat / onPause ───────────

    @Test
    fun onStepCalledWithCorrectFrameAndProgress() {
        val fps = 20
        val frames = 5
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(makeEntity(fps = fps, frames = frames), loops = 0)

        val steps = mutableListOf<Pair<Int, Double>>()
        state.onStep = { frame, percentage -> steps.add(frame to percentage) }

        state.play()
        // Advance 3 frames: 0→1, 1→2, 2→3
        state.advanceFrame(dur * 3)

        assertEquals(3, steps.size)
        assertEquals(1, steps[0].first)
        assertEquals(2, steps[1].first)
        assertEquals(3, steps[2].first)
        // progress for frame 3 in range 0..4 = 3/4 = 0.75
        assertEquals(0.75, steps[2].second, 0.001)
    }

    @Test
    fun onFinishedCalledWhenFiniteLoopCompletes() {
        val fps = 10
        val frames = 3 // 0..2
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(
            makeEntity(fps = fps, frames = frames),
            loops = 1,
            fillMode = FillMode.Forward
        )

        var finishedCount = 0
        state.onFinished = { finishedCount++ }

        state.play()
        state.advanceFrame(dur * 10) // more than enough
        assertEquals(1, finishedCount)
    }

    @Test
    fun onFinishedNotCalledForInfiniteLoop() {
        val fps = 20
        val frames = 3
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(makeEntity(fps = fps, frames = frames), loops = 0)

        var finishedCount = 0
        state.onFinished = { finishedCount++ }

        state.play()
        state.advanceFrame(dur * 20) // many frames, infinite loop
        assertEquals(0, finishedCount)
    }

    @Test
    fun onRepeatCalledOnLoopWrap() {
        val fps = 20
        val frames = 3 // 0..2
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(makeEntity(fps = fps, frames = frames), loops = 0)

        var repeatCount = 0
        state.onRepeat = { repeatCount++ }

        state.play()
        // Advance 6 frames: 0→1→2, wrap→0→1→2, wrap→0
        // First wrap after frame 2 reached, second wrap after frame 2 reached again
        state.advanceFrame(dur * 6)
        assertEquals(2, repeatCount)
    }

    @Test
    fun onRepeatNotCalledForFiniteLoopOnCompletion() {
        val fps = 20
        val frames = 3 // 0..2
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(
            makeEntity(fps = fps, frames = frames),
            loops = 1,
            fillMode = FillMode.Forward
        )

        var repeatCount = 0
        state.onRepeat = { repeatCount++ }

        state.play()
        state.advanceFrame(dur * 10)
        // Single loop completes without wrapping — onRepeat should NOT be called
        assertEquals(0, repeatCount)
    }

    @Test
    fun onPauseCalledOnPause() {
        val state = SVGAAnimationState(makeEntity())

        var pauseCount = 0
        state.onPause = { pauseCount++ }

        state.play()
        state.pause()
        assertEquals(1, pauseCount)
    }

    @Test
    fun callbacksNotCalledWhenNull() {
        val fps = 20
        val frames = 3
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(
            makeEntity(fps = fps, frames = frames),
            loops = 1,
            fillMode = FillMode.Forward
        )
        // All callbacks are null by default — should not throw
        state.play()
        state.advanceFrame(dur * 10)
        state.pause()
        // If we get here without exception, null safety works
        assertFalse(state.isPlaying)
    }

    @Test
    fun onStepCalledOnFinishedFrame() {
        val fps = 10
        val frames = 3 // 0..2
        val dur = frameDurationNanos(fps)
        val state = SVGAAnimationState(
            makeEntity(fps = fps, frames = frames),
            loops = 1,
            fillMode = FillMode.Forward
        )

        val steps = mutableListOf<Int>()
        var finishedFrame: Int? = null
        state.onStep = { frame, _ -> steps.add(frame) }
        state.onFinished = { finishedFrame = state.currentFrame }

        state.play()
        state.advanceFrame(dur * 10)

        // onStep should have been called including the final frame
        assertTrue(steps.isNotEmpty())
        // onFinished should fire after the final onStep
        assertEquals(2, finishedFrame) // endFrame with FillMode.Forward
    }
}
