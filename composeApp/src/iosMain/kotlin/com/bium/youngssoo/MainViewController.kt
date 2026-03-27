package com.bium.youngssoo

import androidx.compose.ui.window.ComposeUIViewController
import com.bium.youngssoo.di.initKoin
import kotlinx.cinterop.ExperimentalForeignApi
import com.bium.youngssoo.swiftinterop.KaKaoLoginSDK
import com.bium.youngssoo.swiftinterop.NaverLoginSDK

object ComposeAppBackHandler {
    var onBack: () -> Unit = {}
}

@OptIn(ExperimentalForeignApi::class)
fun MainViewController() = ComposeUIViewController(configure = {
    initKoin()
    KaKaoLoginSDK.initSDK()
    NaverLoginSDK.initSDK()
    ComposeAppBackHandler.onBack = {
        println("back key pressed")
    }
}) { App() }

