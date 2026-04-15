package com.bium.youngssoo

import androidx.compose.runtime.Composable
import com.bium.youngssoo.minigame.data.model.GameScreenOrientation

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getPlatformContext(): Any?

@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)

@Composable
expect fun PlatformOrientationEffect(orientation: GameScreenOrientation)

expect fun currentTimeMillis(): Long
