package com.bium.youngssoo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.bium.youngssoo.minigame.data.model.GameScreenOrientation
import platform.UIKit.UIDevice
import platform.Foundation.NSNotificationCenter
import kotlinx.datetime.Clock

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()
actual fun getPlatformContext(): Any? = null

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
}

@Composable
actual fun PlatformOrientationEffect(orientation: GameScreenOrientation) {
    DisposableEffect(orientation) {
        NSNotificationCenter.defaultCenter.postNotificationName(
            ORIENTATION_CHANGE_NOTIFICATION,
            orientation.name,
            null
        )
        onDispose { }
    }
}

actual fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()

private const val ORIENTATION_CHANGE_NOTIFICATION = "YoungssooOrientationDidChange"
