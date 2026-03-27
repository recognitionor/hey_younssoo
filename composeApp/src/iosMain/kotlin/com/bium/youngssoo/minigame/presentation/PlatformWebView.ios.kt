package com.bium.youngssoo.minigame.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandler
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

/**
 * iOS WKWebView 구현 (확장된 버전)
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformWebView(
    url: String,
    gameData: GameInitData?,
    onScoreUpdate: (Int) -> Unit,
    onGameComplete: (GameCompleteData) -> Unit,
    onSaveData: (String) -> Unit
) {
    val messageHandler = remember {
        GameMessageHandler(onScoreUpdate, onGameComplete, onSaveData)
    }

    val navigationDelegate = remember {
        GameNavigationDelegate(gameData)
    }

    val configuration = remember {
        WKWebViewConfiguration().apply {
            userContentController = WKUserContentController().apply {
                addScriptMessageHandler(messageHandler, "iOSBridge")
            }
        }
    }

    UIKitView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            WKWebView(frame = kotlinx.cinterop.cValue { }, configuration = configuration).apply {
                this.navigationDelegate = navigationDelegate
                navigationDelegate.webView = this
                NSURL.URLWithString(url)?.let { nsUrl ->
                    loadRequest(NSURLRequest.requestWithURL(nsUrl))
                }
            }
        },
        update = { webView ->
            NSURL.URLWithString(url)?.let { nsUrl ->
                webView.loadRequest(NSURLRequest.requestWithURL(nsUrl))
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            configuration.userContentController.removeScriptMessageHandlerForName("iOSBridge")
        }
    }
}

/**
 * 페이지 로드 완료 시 게임 데이터 전달
 */
class GameNavigationDelegate(
    private val gameData: GameInitData?
) : NSObject(), WKNavigationDelegateProtocol {
    var webView: WKWebView? = null

    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
        gameData?.let { data ->
            val initScript = """
                if (window.GameBridge && window.GameBridge.onInit) {
                    window.GameBridge.onInit(${data.toJson()});
                }
            """.trimIndent()
            webView.evaluateJavaScript(initScript, null)
        }
    }
}

/**
 * JavaScript에서 호출 가능한 메시지 핸들러
 *
 * 게임 HTML에서 다음과 같이 호출:
 * - window.webkit.messageHandlers.iOSBridge.postMessage({type: 'updateScore', score: 100})
 * - window.webkit.messageHandlers.iOSBridge.postMessage({type: 'gameComplete', score: 500, cleared: true})
 * - window.webkit.messageHandlers.iOSBridge.postMessage({type: 'saveData', data: {...}})
 */
class GameMessageHandler(
    private val onScoreUpdate: (Int) -> Unit,
    private val onGameComplete: (GameCompleteData) -> Unit,
    private val onSaveData: (String) -> Unit
) : NSObject(), WKScriptMessageHandler {

    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage
    ) {
        val body = didReceiveScriptMessage.body
        if (body is Map<*, *>) {
            val type = body["type"] as? String
            val score = (body["score"] as? Number)?.toInt() ?: 0

            when (type) {
                "updateScore" -> onScoreUpdate(score)
                "gameComplete" -> {
                    val cleared = body["cleared"] as? Boolean ?: false
                    val nextStage = (body["nextStage"] as? Number)?.toInt()
                    val customData = body["customData"] as? String
                    onGameComplete(GameCompleteData(
                        score = score,
                        cleared = cleared,
                        nextStage = nextStage,
                        customData = customData
                    ))
                }
                "saveData" -> {
                    val data = body["data"]
                    if (data is String) {
                        onSaveData(data)
                    } else if (data is Map<*, *>) {
                        // JSON으로 변환
                        onSaveData(data.toString())
                    }
                }
            }
        }
    }
}
