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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

// 캐시 처리 로직이 바뀔 때 이 값을 올리면 기존 캐시 자동 무효화
private const val CACHE_VERSION = 4

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun PlatformWebView(
    url: String,
    gameData: GameInitData?,
    onScoreUpdate: (Int) -> Unit,
    onGameComplete: (GameCompleteData) -> Unit,
    onSaveData: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    var isLoading by remember { mutableStateOf(true) }
    var loadingProgress by remember { mutableStateOf(0) }
    // cachedHtml: 로컬 캐시에서 읽은 HTML 문자열 (null이면 아직 준비 안 됨)
    var cachedHtml by remember(url) { mutableStateOf<String?>(null) }

    val gameId = remember(url) { url.trimEnd('/').substringAfterLast('/') }
    val gameCacheDir = remember(gameId) { File(context.filesDir, "game_cache/$gameId") }

    // 항상 ETag 확인 후 로드 (서버 변경 시 즉시 반영)
    LaunchedEffect(url) {
        withContext(Dispatchers.IO) {
            downloadAndCacheGame(url, gameId, gameCacheDir)
        }
        val htmlFile = File(gameCacheDir, "index.html")
        cachedHtml = if (htmlFile.exists()) {
            htmlFile.readText(Charsets.UTF_8)
        } else {
            null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    WebView.setWebContentsDebuggingEnabled(true)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.javaScriptCanOpenWindowsAutomatically = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }
                    setBackgroundColor(android.graphics.Color.parseColor("#1a1a2e"))
                    overScrollMode = android.view.View.OVER_SCROLL_NEVER
                    setOnTouchListener { v, event ->
                        v.parent?.requestDisallowInterceptTouchEvent(true)
                        false
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            loadingProgress = newProgress
                        }
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            Log.d("WebViewConsole", "${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                            return true
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                        }
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                            mainHandler.post {
                                // WebView 터치 가로채기 방지: 버튼에 touch-action none 강제 적용
                                evaluateJavascript("""
                                    (function() {
                                        var style = document.createElement('style');
                                        style.innerHTML = '* { touch-action: none !important; -webkit-tap-highlight-color: transparent; }';
                                        document.head.appendChild(style);
                                    })();
                                """.trimIndent(), null)
                                gameData?.let { data ->
                                    val initScript = """
                                        if (window.GameBridge && window.GameBridge.onInit) {
                                            window.GameBridge.onInit(${data.toJson()});
                                        } else {
                                            setTimeout(function() {
                                                if (window.GameBridge && window.GameBridge.onInit) {
                                                    window.GameBridge.onInit(${data.toJson()});
                                                }
                                            }, 500);
                                        }
                                    """.trimIndent()
                                    evaluateJavascript(initScript, null)
                                }
                            }
                        }
                        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                            super.onReceivedError(view, request, error)
                            Log.e("PlatformWebView", "Error: ${error?.description}, URL: ${request?.url}")
                            isLoading = false
                        }
                    }

                    addJavascriptInterface(
                        GameJSInterface(onScoreUpdate, onGameComplete, onSaveData),
                        "AndroidBridge"
                    )
                }
            },
            update = { webView ->
                if (webView.url == null) {
                    val baseUrl = url.trimEnd('/') + "/"
                    if (cachedHtml != null) {
                        webView.loadDataWithBaseURL(baseUrl, cachedHtml!!, "text/html", "UTF-8", null)
                    } else {
                        webView.loadUrl(url)
                    }
                }
            }
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFF1a1a2e)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF00D4FF))
            }
            if (loadingProgress in 1..99) {
                LinearProgressIndicator(
                    progress = { loadingProgress / 100f },
                    modifier = Modifier.fillMaxWidth().height(3.dp).align(Alignment.TopCenter),
                    color = Color(0xFF00D4FF),
                    trackColor = Color(0xFF1a1a2e)
                )
            }
        }
    }
}

private suspend fun downloadAndCacheGame(remoteUrl: String, gameId: String, cacheDir: File) {
    try {
        val etagFile = File(cacheDir, ".etag")
        val versionFile = File(cacheDir, ".version")
        val htmlFile = File(cacheDir, "index.html")

        // 캐시 버전 불일치 시 무효화
        val cachedVersion = versionFile.takeIf { it.exists() }?.readText()?.trim()?.toIntOrNull()
        if (cachedVersion != CACHE_VERSION) {
            etagFile.delete()
            htmlFile.delete()
        }

        val etag = etagFile.takeIf { it.exists() }?.readText()

        val connection = URL(remoteUrl).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 15_000
        if (etag != null) connection.setRequestProperty("If-None-Match", etag)

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
            connection.disconnect()
            return
        }
        if (responseCode != HttpURLConnection.HTTP_OK) {
            connection.disconnect()
            return
        }

        val html = connection.inputStream.bufferedReader(Charsets.UTF_8).readText()
        val newEtag = connection.getHeaderField("ETag")
        connection.disconnect()

        cacheDir.mkdirs()
        htmlFile.writeText(injectTouchFix(html), Charsets.UTF_8)
        if (newEtag != null) etagFile.writeText(newEtag)
        versionFile.writeText(CACHE_VERSION.toString())

        Log.d("GameCache", "Downloaded & cached $gameId (${html.length} chars)")

        // HTML에서 참조된 에셋 파일도 다운로드
        val baseUrl = remoteUrl.trimEnd('/') + "/"
        extractRelativeAssetUrls(html).forEach { relativePath ->
            downloadAssetFile(baseUrl + relativePath, File(cacheDir, relativePath))
        }
    } catch (e: Exception) {
        Log.w("GameCache", "Cache failed for $gameId: ${e.message}")
    }
}

/**
 * WebView에서 pointerdown 딜레이 문제 해결:
 * addEventListener를 패치하여 pointerdown 핸들러를 touchstart에도 등록.
 * touchstart는 브라우저 제스처 판별 없이 즉시 발생.
 */
private fun injectTouchFix(html: String): String {
    val script = """
        <script>
        (function() {
            var _add = EventTarget.prototype.addEventListener;
            EventTarget.prototype.addEventListener = function(type, handler, options) {
                if (type === 'pointerdown') {
                    var el = this;
                    _add.call(el, 'touchstart', function(e) {
                        e.preventDefault();
                        handler.call(el, e);
                    }, { passive: false });
                    _add.call(el, type, function(e) {
                        if (e.pointerType === 'touch') return;
                        handler.call(el, e);
                    }, options);
                } else {
                    _add.call(this, type, handler, options);
                }
            };
        })();
        </script>
    """.trimIndent()
    return html.replace("<head>", "<head>\n$script", ignoreCase = true)
}

private fun extractRelativeAssetUrls(html: String): List<String> {
    val assetExtensions = setOf("png", "jpg", "jpeg", "gif", "svg", "webp", "js", "css", "woff", "woff2")
    val pattern = Regex("""(?:src|href)=["']([^"']+)["']""")
    return pattern.findAll(html)
        .map { it.groupValues[1] }
        .filter { url ->
            !url.startsWith("http") && !url.startsWith("//") &&
            !url.startsWith("data:") && !url.startsWith("#") &&
            url.substringAfterLast('.').lowercase() in assetExtensions
        }
        .distinct()
        .toList()
}

private fun downloadAssetFile(url: String, destFile: File) {
    if (destFile.exists()) return
    try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 15_000
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            connection.disconnect()
            return
        }
        destFile.parentFile?.mkdirs()
        connection.inputStream.use { input ->
            destFile.outputStream().use { output -> input.copyTo(output) }
        }
        connection.disconnect()
        Log.d("GameCache", "Asset cached: ${destFile.name}")
    } catch (e: Exception) {
        Log.w("GameCache", "Asset download failed: ${destFile.name} - ${e.message}")
    }
}

class GameJSInterface(
    private val onScoreUpdate: (Int) -> Unit,
    private val onGameComplete: (GameCompleteData) -> Unit,
    private val onSaveData: (String) -> Unit
) {
    @JavascriptInterface
    fun updateScore(score: Int) { onScoreUpdate(score) }

    @JavascriptInterface
    fun gameComplete(resultJson: String) {
        Log.d("GameJSInterface", "gameComplete: $resultJson")
        try {
            val data = GameCompleteData.fromJson(resultJson)
            onGameComplete(data)
        } catch (e: Exception) {
            Log.e("GameJSInterface", "Parse error: ${e.message}")
            onGameComplete(GameCompleteData(score = resultJson.toIntOrNull() ?: 0, cleared = true))
        }
    }

    @JavascriptInterface
    fun saveData(dataJson: String) { onSaveData(dataJson) }

    @JavascriptInterface
    fun log(message: String) { Log.d("GameJS", message) }
}
