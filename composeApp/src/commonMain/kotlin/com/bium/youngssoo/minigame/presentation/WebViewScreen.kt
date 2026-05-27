package com.bium.youngssoo.minigame.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bium.youngssoo.PlatformOrientationEffect
import com.bium.youngssoo.core.presentation.theme.AuraPrimary
import com.bium.youngssoo.minigame.data.model.GameResult
import com.bium.youngssoo.minigame.data.model.GameScreenOrientation
import com.bium.youngssoo.minigame.data.model.MiniGame

/**
 * 게임 결과 데이터
 */
data class GameResultData(
    val score: Int,
    val targetScore: Int,
    val isCleared: Boolean
)

/**
 * WebView를 통해 게임을 표시하는 화면
 */
@Composable
fun WebViewGameScreen(
    game: MiniGame,
    gameInitData: GameInitData? = null,
    playTime: Int,  // 플레이 가능 시간 (초) 또는 판수
    totalPoints: Int = 0,
    onGameEnd: (GameResult) -> Unit,
    onStageCleared: () -> Unit = {},
    onSaveCustomData: (String) -> Unit = {},
    onPlayAgain: (() -> Boolean)? = null,
    onClose: () -> Unit
) {
    var remainingTime by remember { mutableStateOf(playTime) }
    var gameScore by remember { mutableStateOf(0) }
    var showResult by remember { mutableStateOf(false) }
    var resultData by remember { mutableStateOf<GameResultData?>(null) }
    var currentStage by remember { mutableStateOf(gameInitData?.stage ?: 1) }
    var webViewKey by remember { mutableStateOf(0) }
    val screenOrientation = if (!showResult && game.screenOrientation == GameScreenOrientation.LANDSCAPE) {
        GameScreenOrientation.LANDSCAPE
    } else {
        GameScreenOrientation.PORTRAIT
    }

    PlatformOrientationEffect(orientation = screenOrientation)

    // 결과 화면 표시
    if (showResult && resultData != null) {
        val captured = resultData!!
        GameResultScreen(
            gameName = game.name,
            stage = currentStage,
            score = captured.score,
            targetScore = captured.targetScore,
            isCleared = captured.isCleared,
            currentPoints = totalPoints,
            replayCost = game.costAmount,
            onPlayAgain = if (onPlayAgain != null) ({
                if (onPlayAgain()) {
                    showResult = false
                    resultData = null
                    gameScore = 0
                    remainingTime = playTime
                    webViewKey++
                } else {
                    onGameEnd(GameResult(game.id, captured.score, 0L, captured.isCleared))
                    onClose()
                }
            }) else null,
            onExit = {
                if (captured.isCleared) {
                    onStageCleared()
                }
                onGameEnd(
                    GameResult(
                        gameId = game.id,
                        score = captured.score,
                        playTime = (playTime - remainingTime) * 1000L,
                        isCompleted = captured.isCleared
                    )
                )
                onClose()
            }
        )
        return
    }

    // 타이머 (시간 기반 게임인 경우) - 퍼즐버블은 자체 게임 완료 처리하므로 타이머 제거
    // LaunchedEffect는 필요시 활성화

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D15))
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val isLandscapeLayout = maxWidth > maxHeight
        val horizontalPadding = if (isLandscapeLayout) 12.dp else 16.dp
        val verticalPadding = if (isLandscapeLayout) 8.dp else 14.dp
        val titleFontSize = if (isLandscapeLayout) 14.sp else 16.sp
        val metaFontSize = if (isLandscapeLayout) 12.sp else 14.sp
        val closeButtonSize = if (isLandscapeLayout) 32.dp else 40.dp
        val closeIconSize = if (isLandscapeLayout) 18.dp else 22.dp

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${game.name} · Stage $currentStage",
                    modifier = Modifier.weight(1f),
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = AuraPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(if (isLandscapeLayout) 8.dp else 12.dp))

                Text(
                    text = "$gameScore",
                    fontSize = metaFontSize,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF00D4FF)
                )

                Spacer(modifier = Modifier.width(if (isLandscapeLayout) 8.dp else 12.dp))

                Box(
                    modifier = Modifier
                        .size(closeButtonSize)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable {
                            onGameEnd(
                                GameResult(
                                    gameId = game.id,
                                    score = gameScore,
                                    playTime = (playTime - remainingTime) * 1000L,
                                    isCompleted = false
                                )
                            )
                            onClose()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(closeIconSize)
                    )
                }
            }

            // WebView 영역 (플랫폼별 구현)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                key(webViewKey) {
                    PlatformWebView(
                        url = game.gameUrl,
                        gameData = GameInitData(
                            stage = currentStage,
                            highScore = gameInitData?.highScore ?: 0,
                            customData = gameInitData?.customData ?: "{}",
                            version = game.version
                        ),
                        onScoreUpdate = { score -> gameScore = score },
                        onGameComplete = { completeData ->
                            gameScore = completeData.score

                            // 목표 점수 계산 (스테이지 기반)
                            val targetScore = 100 * currentStage + 50 * (currentStage - 1)

                            resultData = GameResultData(
                                score = completeData.score,
                                targetScore = targetScore,
                                isCleared = completeData.cleared
                            )
                            showResult = true

                            completeData.customData?.let { onSaveCustomData(it) }
                        },
                        onSaveData = { data ->
                            onSaveCustomData(data)
                        }
                    )
                }
            }
        }
    }
}

/**
 * 플랫폼별 WebView 구현을 위한 expect 함수
 */
@Composable
expect fun PlatformWebView(
    url: String,
    gameData: GameInitData? = null,
    onScoreUpdate: (Int) -> Unit,
    onGameComplete: (GameCompleteData) -> Unit,
    onSaveData: (String) -> Unit
)
