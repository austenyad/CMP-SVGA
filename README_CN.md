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

## 使用方式

### 基础用法 — 从 Assets 播放

```kotlin
SVGAImage(
    spec = SVGASpec.Asset("angel.svga"),
    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
)
```

### 从网络 URL 播放

```kotlin
SVGAImage(
    spec = SVGASpec.Url("https://example.com/animation.svga"),
    modifier = Modifier.size(300.dp),
    loading = { CircularProgressIndicator() },
    failure = { error -> Text("加载失败: $error") }
)
```

### 从文件路径播放

```kotlin
SVGAImage(
    spec = SVGASpec.File("/path/to/animation.svga"),
    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
)
```

### 播放参数控制

```kotlin
SVGAImage(
    spec = SVGASpec.Asset("angel.svga"),
    modifier = Modifier.size(200.dp),
    loops = 3,                              // 0 = 无限循环
    autoPlay = true,
    contentScale = ContentScale.Fit,
    range = 0..30,                          // 只播放第 0-30 帧
    onFinished = { /* 播放完成 */ },
    onStep = { frame, progress -> /* 每帧回调 */ }
)
```

### 外部播放控制

```kotlin
val composition = rememberSVGAComposition(SVGASpec.Asset("angel.svga"))

if (composition.value != null) {
    val state = rememberSVGAState(
        videoEntity = composition.value!!,
        loops = 0,
        autoPlay = false
    )

    SVGAImage(
        state = state,
        modifier = Modifier.size(200.dp)
    )

    Row {
        Button(onClick = { state.play() }) { Text("播放") }
        Button(onClick = { state.pause() }) { Text("暂停") }
        Button(onClick = { state.stop() }) { Text("停止") }
    }
}
```

### 动态内容替换

```kotlin
val dynamicEntity = remember { SVGADynamicEntity() }

// 替换图片图层
dynamicEntity.setDynamicImage(myBitmap, forKey = "avatar")

// 替换文字图层
dynamicEntity.setDynamicText(
    "你好",
    SVGATextStyle(fontSize = 14f, fillColor = Color.White),
    forKey = "username"
)

// 隐藏某个图层
dynamicEntity.setHidden(true, forKey = "badge")

SVGAImage(
    spec = SVGASpec.Asset("template.svga"),
    modifier = Modifier.size(300.dp),
    dynamicEntity = dynamicEntity
)
```

### 自定义网络加载器

库默认使用平台原生 HTTP（Android 用 HttpURLConnection，iOS 用 NSURLSession）。你可以替换为项目中已有的网络框架，只需实现 `SVGANetworkLoader` 接口，它只有一个方法：`suspend fun download(url: String): ByteArray`。

#### Android 端 — 使用项目已有的 OkHttp

```kotlin
// androidMain
class AndroidSVGANetworkLoader(
    private val client: OkHttpClient  // 项目里已有的 OkHttpClient 实例
) : SVGANetworkLoader {
    override suspend fun download(url: String): ByteArray {
        val request = Request.Builder().url(url).build()
        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { it.body!!.bytes() }
        }
    }
}
```

#### iOS 端 — 使用 NSURLSession 或其他网络库

```kotlin
// iosMain
class IOSSVGANetworkLoader : SVGANetworkLoader {
    override suspend fun download(url: String): ByteArray {
        return suspendCancellableCoroutine { continuation ->
            val nsUrl = NSURL(string = url)
            val request = NSURLRequest(URL = nsUrl)
            NSURLSession.sharedSession.dataTaskWithRequest(request) { data, _, error ->
                if (error != null) {
                    continuation.resumeWithException(Exception(error.localizedDescription))
                } else if (data != null) {
                    continuation.resume(data.toByteArray())
                } else {
                    continuation.resumeWithException(Exception("Empty response"))
                }
            }.resume()
        }
    }
}
```

#### 方式一：在调用处直接传入

```kotlin
// Android 端
SVGAImage(
    spec = SVGASpec.Url("https://example.com/anim.svga"),
    modifier = Modifier.size(200.dp),
    networkLoader = AndroidSVGANetworkLoader(myOkHttpClient)
)

// iOS 端
SVGAImage(
    spec = SVGASpec.Url("https://example.com/anim.svga"),
    modifier = Modifier.size(200.dp),
    networkLoader = IOSSVGANetworkLoader()
)
```

#### 方式二：用 expect/actual 封装，commonMain 无需关心平台差异

```kotlin
// commonMain
expect fun createPlatformNetworkLoader(): SVGANetworkLoader

// androidMain
actual fun createPlatformNetworkLoader(): SVGANetworkLoader {
    return AndroidSVGANetworkLoader(AppModule.okHttpClient)
}

// iosMain
actual fun createPlatformNetworkLoader(): SVGANetworkLoader {
    return IOSSVGANetworkLoader()
}

// commonMain 中使用
SVGAImage(
    spec = SVGASpec.Url("https://example.com/anim.svga"),
    modifier = Modifier.size(200.dp),
    networkLoader = createPlatformNetworkLoader()
)
```
