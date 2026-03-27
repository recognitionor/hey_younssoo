package com.bium.youngssoo.minigame.presentation

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Android WebView 구현 (확장된 버전)
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun PlatformWebView(
    url: String,
    gameData: GameInitData?,
    onScoreUpdate: (Int) -> Unit,
    onGameComplete: (GameCompleteData) -> Unit,
    onSaveData: (String) -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    Log.d("PlatformWebView", "Loading URL: $url")
    Log.d("PlatformWebView", "GameData: $gameData")

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    // 디버깅 활성화
                    WebView.setWebContentsDebuggingEnabled(true)

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.javaScriptCanOpenWindowsAutomatically = true

                    // Mixed content 허용 (Android 5.0+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }

                    // 배경색 설정
                    setBackgroundColor(android.graphics.Color.parseColor("#1a1a2e"))

                    // WebChromeClient - 콘솔 로그 출력
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            Log.d("WebViewConsole", "${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                            return true
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            Log.d("PlatformWebView", "Page started: $url")
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            Log.d("PlatformWebView", "Page finished: $url")
                            isLoading = false

                            // 페이지 로드 완료 시 게임 데이터 전달
                            gameData?.let { data ->
                                val initScript = """
                                    console.log('Sending init data to game');
                                    if (window.GameBridge && window.GameBridge.onInit) {
                                        window.GameBridge.onInit(${data.toJson()});
                                    } else {
                                        console.log('GameBridge not found, retrying...');
                                        setTimeout(function() {
                                            if (window.GameBridge && window.GameBridge.onInit) {
                                                window.GameBridge.onInit(${data.toJson()});
                                            }
                                        }, 500);
                                    }
                                """.trimIndent()
                                mainHandler.post {
                                    evaluateJavascript(initScript, null)
                                }
                            }
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            Log.e("PlatformWebView", "Error: ${error?.description}, URL: ${request?.url}")
                            isLoading = false
                        }
                    }

                    // JavaScript Interface 추가
                    addJavascriptInterface(
                        GameJSInterface(onScoreUpdate, onGameComplete, onSaveData),
                        "AndroidBridge"
                    )

                    loadUrl(url)
                }
            },
            update = { webView ->
                // URL 변경 시에만 reload
            }
        )

        // 로딩 인디케이터
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1a1a2e)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF00D4FF))
            }
        }
    }
}

/**
 * JavaScript에서 호출 가능한 인터페이스
 *
 * 게임 HTML에서 다음과 같이 호출:
 * - AndroidBridge.updateScore(100)
 * - AndroidBridge.gameComplete(JSON.stringify({score: 500, cleared: true, nextStage: 2}))
 * - AndroidBridge.saveData(JSON.stringify({...}))
 */
class GameJSInterface(
    private val onScoreUpdate: (Int) -> Unit,
    private val onGameComplete: (GameCompleteData) -> Unit,
    private val onSaveData: (String) -> Unit
) {
    @JavascriptInterface
    fun updateScore(score: Int) {
        onScoreUpdate(score)
    }

    @JavascriptInterface
    fun gameComplete(resultJson: String) {
        Log.d("GameJSInterface", "gameComplete received: $resultJson")
        try {
            val data = GameCompleteData.fromJson(resultJson)
            Log.d("GameJSInterface", "Parsed data: score=${data.score}, cleared=${data.cleared}")
            onGameComplete(data)
        } catch (e: Exception) {
            Log.e("GameJSInterface", "Parse error: ${e.message}, raw: $resultJson")
            // Fallback: score만 있는 경우
            val score = resultJson.toIntOrNull() ?: 0
            onGameComplete(GameCompleteData(score = score, cleared = true))
        }
    }

    @JavascriptInterface
    fun saveData(dataJson: String) {
        onSaveData(dataJson)
    }

    @JavascriptInterface
    fun log(message: String) {
        println("GameJS: $message")
    }
}
