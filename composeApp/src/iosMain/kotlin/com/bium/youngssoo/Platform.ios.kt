package com.bium.youngssoo

import androidx.compose.runtime.Composable
import platform.UIKit.UIDevice
import kotlinx.datetime.Clock

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()
actual fun getPlatformContext(): Any? = null

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
}

actual fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()