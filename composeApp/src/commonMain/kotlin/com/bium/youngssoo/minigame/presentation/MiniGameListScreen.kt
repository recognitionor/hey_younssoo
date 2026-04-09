package com.bium.youngssoo.minigame.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bium.youngssoo.core.presentation.theme.*
import com.bium.youngssoo.minigame.data.model.CostType
import com.bium.youngssoo.minigame.data.model.MiniGame
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MiniGameListScreen(
    viewModel: MiniGameViewModel = koinViewModel(),
    onPlayGame: (MiniGame) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showInsufficientDialog by remember { mutableStateOf(false) }
    var selectedGame by remember { mutableStateOf<MiniGame?>(null) }

    // 해금 확인 다이얼로그
    if (state.showUnlockDialog && state.pendingUnlockGame != null) {
        val game = state.pendingUnlockGame!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissUnlockDialog() },
            containerColor = Color(0xFF252540),
            title = {
                Text(
                    "${game.name} 영구 해금",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "한 번 해금하면 이후에는 무료로 플레이할 수 있어요!",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("해금 가격", color = Color.Gray)
                        Text("${game.unlockPrice} pts", color = AuraTertiary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("보유 포인트", color = Color.Gray)
                        Text(
                            "${state.totalPoints} pts",
                            color = if (state.totalPoints >= game.unlockPrice) Color.White else Color(0xFFFF6B6B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmUnlock { onPlayGame(it) } },
                    enabled = state.totalPoints >= game.unlockPrice
                ) {
                    Text(
                        "해금하기",
                        color = if (state.totalPoints >= game.unlockPrice) AuraPrimary else Color.Gray
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissUnlockDialog() }) {
                    Text("취소", color = Color.Gray)
                }
            }
        )
    }

    // 포인트 부족 다이얼로그
    if (showInsufficientDialog && selectedGame != null) {
        AlertDialog(
            onDismissRequest = { showInsufficientDialog = false },
            containerColor = Color(0xFF252540),
            title = {
                Text(
                    text = "포인트 부족",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "${selectedGame!!.name}을(를) 플레이하려면 포인트가 더 필요해요!",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "필요 포인트",
                            color = Color.Gray
                        )
                        Text(
                            text = "${selectedGame!!.costAmount} pts",
                            color = AuraTertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "보유 포인트",
                            color = Color.Gray
                        )
                        Text(
                            text = "${state.totalPoints} pts",
                            color = Color(0xFFFF6B6B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "부족한 포인트",
                            color = Color.Gray
                        )
                        Text(
                            text = "${selectedGame!!.costAmount - state.totalPoints} pts",
                            color = Color(0xFFFF6B6B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showInsufficientDialog = false }
                ) {
                    Text("확인", color = AuraPrimary)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D0D15), Color(0xFF191922))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "미니게임",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AuraPrimary
            )
            Spacer(modifier = Modifier.weight(1f))

            // 포인트 표시
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = AuraTertiary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${state.totalPoints}",
                    color = AuraTertiary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AuraPrimary)
            }
        } else if (state.games.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "게임이 없습니다",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.games) { game ->
                    val progress = state.gameProgress[game.id]
                    val currentStage = progress?.currentStage ?: 1
                    val isUnlocked = progress?.isUnlocked ?: false
                    val hasNewVersion = viewModel.hasNewVersion(game)

                    MiniGameCard(
                        game = game,
                        progress = progress,
                        currentStage = currentStage,
                        isUnlocked = isUnlocked,
                        hasNewVersion = hasNewVersion,
                        onClick = {
                            viewModel.onGameClicked(game, onPlayGame)
                        }
                    )
                }
            }
        }
        }

//        // 테스트 모드 플로팅 버튼
//        TestModeFloatingButton(
//            currentPoints = state.totalPoints,
//            onSetPoints = { points ->
//                viewModel.setPointsForTesting(points)
//            },
//            onResetProgress = {
//                viewModel.resetAllGameProgress()
//            },
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(16.dp)
//        )
    }
}

@Composable
fun MiniGameCard(
    game: MiniGame,
    progress: com.bium.youngssoo.minigame.data.local.MiniGameProgressEntity?,
    currentStage: Int = 1,
    isUnlocked: Boolean,
    hasNewVersion: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 게임 아이콘
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AuraPrimary.copy(alpha = 0.3f), AuraSecondary.copy(alpha = 0.3f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isUnlocked && game.unlockPrice > 0) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = AuraPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = game.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isUnlocked) {
                            Spacer(modifier = Modifier.width(8.dp))
                            // 현재 스테이지 뱃지
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AuraPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Stage $currentStage",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AuraPrimary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = game.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 잠금 상태 또는 비용 표시
                    if (!isUnlocked && game.unlockPrice > 0) {
                        // 잠금 상태
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${game.unlockPrice} pts로 해금",
                                fontSize = 14.sp,
                                color = AuraTertiary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        // 해금됨 - 플레이 비용 표시
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = AuraTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${game.costAmount} pts",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = AuraTertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (game.costType) {
                                    CostType.TIME -> "${game.playValue / 60}분"
                                    CostType.PLAYS -> "${game.playValue}판"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // NEW 뱃지 (오른쪽 위)
            if (hasNewVersion) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFF6B00).copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        "NEW",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 버전 표시 (오른쪽 아래)
            Text(
                text = "v${game.version}",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            )
        }
    }
}
