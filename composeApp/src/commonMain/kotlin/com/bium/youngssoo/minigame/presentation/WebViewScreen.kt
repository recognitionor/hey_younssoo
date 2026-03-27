package com.bium.youngssoo.minigame.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bium.youngssoo.core.presentation.theme.AuraPrimary
import com.bium.youngssoo.minigame.data.model.GameResult
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
    onGameEnd: (GameResult) -> Unit,
    onStageCleared: () -> Unit = {},
    onSaveCustomData: (String) -> Unit = {},
    onClose: () -> Unit
) {
    var remainingTime by remember { mutableStateOf(playTime) }
    var gameScore by remember { mutableStateOf(0) }
    var showResult by remember { mutableStateOf(false) }
    var resultData by remember { mutableStateOf<GameResultData?>(null) }
    var currentStage by remember { mutableStateOf(gameInitData?.stage ?: 1) }

    // 결과 화면 표시
    if (showResult && resultData != null) {
        GameResultScreen(
            gameName = game.name,
            stage = currentStage,
            score = resultData!!.score,
            targetScore = resultData!!.targetScore,
            isCleared = resultData!!.isCleared,
            onNextStage = {
                // 다음 스테이지로
                currentStage++
                onStageCleared()
                showResult = false
                resultData = null
                gameScore = 0
            },
            onRetry = {
                // 재시도
                showResult = false
                resultData = null
                gameScore = 0
            },
            onExit = {
                onGameEnd(
                    GameResult(
                        gameId = game.id,
                        score = resultData!!.score,
                        playTime = (playTime - remainingTime) * 1000L,
                        isCompleted = resultData!!.isCleared
                    )
                )
                onClose()
            }
        )
        return
    }

    // 타이머 (시간 기반 게임인 경우) - 퍼즐버블은 자체 게임 완료 처리하므로 타이머 제거
    // LaunchedEffect는 필요시 활성화

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D15))
    ) {
        // 상단 바
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = game.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AuraPrimary
            )

            // 스테이지 표시
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Stage $currentStage",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.weight(1f))

            // 점수 표시
            Text(
                text = "$gameScore",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF00D4FF)
            )

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(onClick = {
                onGameEnd(
                    GameResult(
                        gameId = game.id,
                        score = gameScore,
                        playTime = (playTime - remainingTime) * 1000L,
                        isCompleted = false
                    )
                )
                onClose()
            }) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }

        // WebView 영역 (플랫폼별 구현)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            PlatformWebView(
                url = game.gameUrl,
                gameData = GameInitData(
                    stage = currentStage,
                    highScore = gameInitData?.highScore ?: 0,
                    customData = gameInitData?.customData ?: "{}"
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
