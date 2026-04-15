package com.bium.youngssoo

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.bium.youngssoo.minigame.data.model.GameScreenOrientation

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

@Composable
actual fun PlatformOrientationEffect(orientation: GameScreenOrientation) {
    val activity = LocalContext.current.findActivity()

    DisposableEffect(activity, orientation) {
        activity?.requestedOrientation = when (orientation) {
            GameScreenOrientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            GameScreenOrientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
        onDispose { }
    }
}

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
