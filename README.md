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

## Usage

### Basic — Play from Assets

```kotlin
SVGAImage(
    spec = SVGASpec.Asset("angel.svga"),
    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
)
```

### Play from URL

```kotlin
SVGAImage(
    spec = SVGASpec.Url("https://example.com/animation.svga"),
    modifier = Modifier.size(300.dp),
    loading = { CircularProgressIndicator() },
    failure = { error -> Text("Failed: $error") }
)
```

### Play from File Path

```kotlin
SVGAImage(
    spec = SVGASpec.File("/path/to/animation.svga"),
    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
)
```

### Playback Parameters

```kotlin
SVGAImage(
    spec = SVGASpec.Asset("angel.svga"),
    modifier = Modifier.size(200.dp),
    loops = 3,                              // 0 = infinite
    autoPlay = true,
    contentScale = ContentScale.Fit,
    range = 0..30,                          // play only frames 0-30
    onFinished = { /* playback complete */ },
    onStep = { frame, progress -> /* per-frame callback */ }
)
```

### External Playback Control

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
        Button(onClick = { state.play() }) { Text("Play") }
        Button(onClick = { state.pause() }) { Text("Pause") }
        Button(onClick = { state.stop() }) { Text("Stop") }
    }
}
```

### Dynamic Content Replacement

```kotlin
val dynamicEntity = remember { SVGADynamicEntity() }

// Replace an image layer
dynamicEntity.setDynamicImage(myBitmap, forKey = "avatar")

// Replace text on a layer
dynamicEntity.setDynamicText(
    "Hello",
    SVGATextStyle(fontSize = 14f, fillColor = Color.White),
    forKey = "username"
)

// Hide a layer
dynamicEntity.setHidden(true, forKey = "badge")

SVGAImage(
    spec = SVGASpec.Asset("template.svga"),
    modifier = Modifier.size(300.dp),
    dynamicEntity = dynamicEntity
)
```

### Custom Network Loader

The library uses platform-native HTTP by default (HttpURLConnection on Android, NSURLSession on iOS). You can plug in your own:

```kotlin
// OkHttp example
class OkHttpSVGALoader(private val client: OkHttpClient) : SVGANetworkLoader {
    override suspend fun download(url: String): ByteArray {
        val request = Request.Builder().url(url).build()
        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { it.body!!.bytes() }
        }
    }
}

// Use it
SVGAImage(
    spec = SVGASpec.Url("https://example.com/anim.svga"),
    modifier = Modifier.size(200.dp),
    networkLoader = OkHttpSVGALoader(okHttpClient)
)
```
