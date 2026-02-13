package com.example.svga.demo

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private lateinit var appInstance: Application

fun initApp(app: Application) {
    appInstance = app
}

actual suspend fun loadAssetBytes(name: String): ByteArray = withContext(Dispatchers.IO) {
    appInstance.assets.open(name).use { it.readBytes() }
}
