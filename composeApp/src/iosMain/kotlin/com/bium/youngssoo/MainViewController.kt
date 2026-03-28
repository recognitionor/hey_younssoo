package com.bium.youngssoo

import androidx.compose.ui.window.ComposeUIViewController
import com.bium.youngssoo.di.initKoin
import kotlinx.cinterop.ExperimentalForeignApi

object ComposeAppBackHandler {
    var onBack: () -> Unit = {}
}

@OptIn(ExperimentalForeignApi::class)
fun MainViewController() = ComposeUIViewController(configure = {
    initKoin()
    ComposeAppBackHandler.onBack = {
        println("back key pressed")
    }
}) { App() }

