# CMP-SVGA

A Compose Multiplatform SVGA animation player library. Play [SVGA](http://svga.io/) animations natively on Android and iOS using a single codebase.

[中文文档](README_CN.md)

## Supported Platforms

| Platform | Status |
|----------|--------|
| Android  | ✅      |
| iOS      | ✅      |

## What is CMP-SVGA?

CMP-SVGA is a cross-platform SVGA animation player built with Kotlin Multiplatform and Compose Multiplatform. It parses and renders SVGA files frame-by-frame on a Compose Canvas, supporting both Protobuf binary and ZIP formats.

Key features:

- Lottie-style simple API — one line to play an animation
- Load from assets, URL, file path, or raw bytes
- Pluggable network loader — use OkHttp, Ktor, or any custom HTTP client
- Built-in file-based caching for network animations
- Dynamic content replacement (images, text, custom drawing)
- External playback control (play, pause, stop, step to frame)
- ContentScale support (Fit, Crop, Fill, etc.)
