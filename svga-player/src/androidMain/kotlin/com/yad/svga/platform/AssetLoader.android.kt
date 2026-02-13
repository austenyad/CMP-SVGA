package com.yad.svga.platform

import android.annotation.SuppressLint
import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class AssetLoader {

    actual suspend fun loadBytes(name: String): ByteArray = withContext(Dispatchers.IO) {
        val context = appContext
            ?: throw IllegalStateException(
                "AssetLoader not initialized. Call AssetLoader.init(context) first."
            )
        context.assets.open(name).use { it.readBytes() }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var appContext: Application? = null

        /**
         * Must be called once (e.g. in Application.onCreate) before using SVGASpec.Asset.
         */
        @JvmStatic
        fun init(application: Application) {
            appContext = application
        }
    }
}
