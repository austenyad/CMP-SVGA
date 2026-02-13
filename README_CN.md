# CMP-SVGA

基于 Compose Multiplatform 的 SVGA 动画播放库。使用一套代码在 Android 和 iOS 上原生播放 [SVGA](http://svga.io/) 动画。

[English](README.md)

## 支持平台

| 平台    | 状态 |
|---------|------|
| Android | ✅   |
| iOS     | ✅   |

## CMP-SVGA 是什么？

CMP-SVGA 是一个基于 Kotlin Multiplatform + Compose Multiplatform 构建的跨平台 SVGA 动画播放器。它在 Compose Canvas 上逐帧解析和渲染 SVGA 文件，支持 Protobuf 二进制和 ZIP 两种格式。

主要特性：

- 类 Lottie 的简洁 API，一行代码播放动画
- 支持从 assets、URL、文件路径、原始字节加载
- 可扩展的网络加载器，支持 OkHttp、Ktor 或任意自定义 HTTP 客户端
- 内置基于文件的网络动画缓存
- 动态内容替换（图片、文字、自定义绘制）
- 外部播放控制（播放、暂停、停止、跳转到指定帧）
- 支持 ContentScale（Fit、Crop、Fill 等）
