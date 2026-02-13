package com.yad.svga.proto

import pbandk.decodeFromByteArray
import com.yad.svga.model.toFrameEntity
import java.io.ByteArrayOutputStream
import java.util.zip.Inflater
import kotlin.test.Test
import kotlin.test.assertTrue

class RealSvgaDecodeTest {

    private fun inflate(bytes: ByteArray): ByteArray {
        val inflater = Inflater()
        inflater.setInput(bytes)
        val out = ByteArrayOutputStream(bytes.size * 2)
        val buf = ByteArray(4096)
        while (!inflater.finished()) {
            val n = inflater.inflate(buf)
            if (n == 0 && inflater.needsInput()) break
            out.write(buf, 0, n)
        }
        inflater.end()
        return out.toByteArray()
    }

    @Test
    fun decodeAngelSvga() {
        // Unit test CWD is the module dir (svga-player/), go up to workspace root
        val candidates = listOf(
            "../../SVGAPlayer-Android/app/src/main/assets/angel.svga",
            "../SVGAPlayer-Android/app/src/main/assets/angel.svga",
            "SVGAPlayer-Android/app/src/main/assets/angel.svga"
        )
        val svgaFile = candidates.map { java.io.File(it) }.firstOrNull { it.exists() }
        if (svgaFile == null) {
            println("angel.svga not found, tried: ${candidates.map { java.io.File(it).absolutePath }}")
            return
        }

        val compressed = svgaFile.readBytes()
        println("Compressed size: ${compressed.size}")

        val inflated = inflate(compressed)
        println("Inflated size: ${inflated.size}")

        val movie = MovieEntity.decodeFromByteArray(inflated)
        println("version: ${movie.version}")
        println("params: ${movie.params}")
        println("images count: ${movie.images.size}")
        println("sprites count: ${movie.sprites.size}")
        println("audios count: ${movie.audios.size}")

        var totalFrames = 0
        var visibleFrames = 0
        var nonZeroLayout = 0

        for (sprite in movie.sprites) {
            for (frame in sprite.frames) {
                totalFrames++
                if (frame.alpha != null && frame.alpha > 0f) visibleFrames++
                val l = frame.layout
                if (l != null && ((l.width ?: 0f) > 0f || (l.height ?: 0f) > 0f)) nonZeroLayout++
            }
        }

        println("Total frames across all sprites: $totalFrames")
        println("Visible frames (alpha > 0): $visibleFrames")
        println("Non-zero layout frames: $nonZeroLayout")

        // Find first sprite with visible frame and print full details including transform
        for ((i, sprite) in movie.sprites.withIndex()) {
            val visIdx = sprite.frames.indexOfFirst { it.alpha != null && it.alpha > 0f }
            if (visIdx >= 0) {
                val f = sprite.frames[visIdx]
                println("\nFirst visible: Sprite[$i] imageKey=${sprite.imageKey}")
                println("  Frame[$visIdx]: alpha=${f.alpha}")
                println("  layout: ${f.layout}")
                println("  transform: a=${f.transform?.a}, b=${f.transform?.b}, c=${f.transform?.c}, d=${f.transform?.d}, tx=${f.transform?.tx}, ty=${f.transform?.ty}")
                println("  clipPath: ${f.clipPath}")
                println("  shapes: ${f.shapes.size}")

                // Now test ProtoConverter on this frame
                val converted = f.toFrameEntity()
                println("  CONVERTED alpha: ${converted.alpha}")
                println("  CONVERTED layout: ${converted.layout}")
                println("  CONVERTED transform: ${converted.transform}")
                break
            }
        }

        assertTrue(visibleFrames > 0, "Expected some visible frames but got 0 out of $totalFrames")
        assertTrue(nonZeroLayout > 0, "Expected some non-zero layouts but got 0 out of $totalFrames")
    }
}
