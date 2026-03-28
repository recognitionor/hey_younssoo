package com.bium.youngssoo

import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

private lateinit var appContext: Context

fun initPlatformContext(context: Context) {
    appContext = context
}

actual fun getPlatform(): Platform = AndroidPlatform()
actual fun getPlatformContext(): Any? = appContext

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler {
        println("BackHandler")
        onBack()
    }
}

actual fun currentTimeMillis(): Long = System.currentTimeMillis()