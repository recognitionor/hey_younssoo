package com.bium.youngssoo

import androidx.compose.runtime.Composable

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getPlatformContext(): Any?

@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
