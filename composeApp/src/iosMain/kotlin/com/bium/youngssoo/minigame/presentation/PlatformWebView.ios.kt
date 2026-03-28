package com.bium.youngssoo.minigame.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.memcpy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithBytes
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformWebView(
    url: String,
    gameData: GameInitData?,
    onScoreUpdate: (Int) -> Unit,
    onGameComplete: (GameCompleteData) -> Unit,
    onSaveData: (String) -> Unit
) {
    val gameId = remember(url) {
        url.trimEnd('/').substringAfterLast('/')
    }
    val gameCacheDir = remember(gameId) { getGameCacheDir(gameId) }
    val remoteBaseUrl = remember(url) { url.trimEnd('/') + "/" }

    val httpClient: HttpClient = koinInject()
    var isReady by remember(url) { mutableStateOf(false) }
    var htmlContent by remember(url) { mutableStateOf<String?>(null) }

    LaunchedEffect(url) {
        // 항상 ETag 체크 후 로드 (서버 변경 시 즉시 반영)
        withContext(Dispatchers.Default) {
            downloadAndCacheGameIOS(httpClient, url, gameId, gameCacheDir)
        }
        val htmlPath = "$gameCacheDir/index.html"
        htmlContent = if (NSFileManager.defaultManager.fileExistsAtPath(htmlPath)) {
            readFileAsString(htmlPath)?.let { injectIOSTouchFix(it) }
        } else {
            null
        }
        isReady = true
    }

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

    if (isReady) {
        UIKitView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                WKWebView(
                    frame = kotlinx.cinterop.cValue { },
                    configuration = configuration
                ).apply {
                    this.navigationDelegate = navigationDelegate
                    navigationDelegate.webView = this
                    val html = htmlContent
                    if (html != null) {
                        loadHTMLString(html, baseURL = NSURL.URLWithString(remoteBaseUrl))
                    } else {
                        NSURL.URLWithString(url)?.let { nsUrl ->
                            loadRequest(NSURLRequest.requestWithURL(nsUrl))
                        }
                    }
                }
            },
            update = { _ -> }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            configuration.userContentController.removeScriptMessageHandlerForName("iOSBridge")
        }
    }
}

// MARK: - 캐시 경로

@OptIn(ExperimentalForeignApi::class)
private fun getGameCacheDir(gameId: String): String {
    val docs = NSFileManager.defaultManager.URLsForDirectory(
        NSDocumentDirectory, NSUserDomainMask
    ).firstOrNull() as? NSURL ?: return ""
    val dir = "${docs.path}/game_cache/$gameId"
    NSFileManager.defaultManager.createDirectoryAtPath(
        dir, withIntermediateDirectories = true, attributes = null, error = null
    )
    return dir
}

// MARK: - 다운로드

private suspend fun downloadAndCacheGameIOS(
    httpClient: HttpClient,
    remoteUrl: String,
    gameId: String,
    cacheDir: String
) {
    try {
        val etagPath = "$cacheDir/.etag"
        val htmlPath = "$cacheDir/index.html"

        val storedEtag = readFileAsString(etagPath)

        val response = httpClient.get(remoteUrl) {
            if (!storedEtag.isNullOrEmpty()) {
                header("If-None-Match", storedEtag)
            }
        }

        when (response.status.value) {
            304 -> return // Not Modified → 캐시 그대로
            200 -> {
                val html = response.body<String>()
                val baseUrl = remoteUrl.trimEnd('/') + "/"
                val rewritten = rewriteRelativeUrls(html, baseUrl)
                rewritten.toNSData()?.writeToFile(htmlPath, atomically = true)

                val newEtag = response.headers["ETag"]
                if (newEtag != null) {
                    newEtag.toNSData()?.writeToFile(etagPath, atomically = true)
                }
            }
            else -> return // 에러 → 기존 캐시 유지
        }
    } catch (e: Exception) {
        // 다운로드 실패 시 무시
    }
}

private fun rewriteRelativeUrls(html: String, baseUrl: String): String {
    return html.replace("<head>", "<head>\n<base href=\"$baseUrl\">", ignoreCase = true)
}

private fun injectIOSTouchFix(html: String): String {
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

@OptIn(ExperimentalForeignApi::class)
private fun readFileAsString(path: String): String? {
    val data = NSData.dataWithContentsOfFile(path) ?: return null
    return data.toKotlinString()
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toKotlinString(): String? {
    if (length == 0UL) return ""
    val bytes = ByteArray(length.toInt())
    bytes.usePinned { pinned ->
        platform.posix.memcpy(pinned.addressOf(0), this.bytes, length)
    }
    return bytes.decodeToString()
}

@OptIn(ExperimentalForeignApi::class)
private fun String.toNSData(): NSData? {
    val bytes = encodeToByteArray()
    return bytes.usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
    }
}

// MARK: - Navigation Delegate

class GameNavigationDelegate(
    private val gameData: GameInitData?
) : NSObject(), WKNavigationDelegateProtocol {
    var webView: WKWebView? = null

    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
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
            webView.evaluateJavaScript(initScript, null)
        }
    }
}

// MARK: - Message Handler

class GameMessageHandler(
    private val onScoreUpdate: (Int) -> Unit,
    private val onGameComplete: (GameCompleteData) -> Unit,
    private val onSaveData: (String) -> Unit
) : NSObject(), WKScriptMessageHandlerProtocol {

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
                    onGameComplete(GameCompleteData(score, cleared, nextStage, customData))
                }
                "saveData" -> {
                    val data = body["data"]
                    if (data is String) onSaveData(data)
                }
            }
        }
    }
}
