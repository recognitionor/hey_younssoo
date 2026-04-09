package com.bium.youngssoo.game.vocab.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bium.youngssoo.core.presentation.theme.*
import com.bium.youngssoo.game.vocab.domain.model.QuestionType
import org.jetbrains.compose.resources.stringResource
import youngsso.composeapp.generated.resources.*

@Composable
fun VocabScreen(viewModel: VocabGameViewModel, onNavigateBack: () -> Unit) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        androidx.compose.ui.graphics.Color(0xFF0D0D15),
                        androidx.compose.ui.graphics.Color(0xFF191922)
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!state.isPlaying && !state.isGameOver) {
            VocabStartScreen(onStart = { viewModel.startGame() }, onBack = onNavigateBack)
        } else if (state.isGameOver) {
            VocabGameOverScreen(
                score = state.sessionScore,
                onRestart = { viewModel.startGame() },
                onNavigateBack = onNavigateBack
            )
        } else {
            VocabPlayScreen(state = state, viewModel = viewModel, onStopGame = { viewModel.stopGame() })
        }
    }
}

@Composable
fun VocabStartScreen(onStart: () -> Unit, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AuraPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Lobby", color = AuraPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(AuraPrimaryContainer.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(AuraPrimary, AuraSecondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = AuraOnPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text(
                stringResource(Res.string.vocab_title),
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = AuraPrimary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "교육부지정 초등생 영단어",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 점수 안내
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("점수 시스템", fontWeight = FontWeight.Bold, color = AuraPrimary, fontSize = 14.sp)
                ScoreRuleRow("기본 점수", "+10", MaterialTheme.colorScheme.onSurface)
                ScoreRuleRow("2초 이내 답변", "+10 스피드!", AuraTertiary)
                ScoreRuleRow("4초 이내 답변", "+5 스피드", AuraTertiary)
                ScoreRuleRow("2연속 정답", "+3 콤보", AuraSecondary)
                ScoreRuleRow("3연속 정답", "+5 콤보", AuraSecondary)
                ScoreRuleRow("5연속 이상", "+12 콤보🔥", AuraSecondary)
            }

            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = AuraPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(0.85f).height(64.dp)
            ) {
                Text(stringResource(Res.string.vocab_btn_start), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AuraOnPrimary)
            }
        }
    }
}

@Composable
private fun ScoreRuleRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun VocabGameOverScreen(score: Int, onRestart: () -> Unit, onNavigateBack: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(Res.string.math_cleared),
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = AuraTertiary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.math_total_gold, score),
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onRestart,
            colors = ButtonDefaults.buttonColors(containerColor = AuraSecondary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.8f).height(60.dp)
        ) {
            Text(stringResource(Res.string.math_btn_again), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AuraOnSecondary)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onNavigateBack,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.8f).height(60.dp)
        ) {
            Text("메인으로", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VocabPlayScreen(state: VocabGameState, viewModel: VocabGameViewModel, onStopGame: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── HUD: 라운드 / 콤보 / 점수 ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.math_wave_format, state.currentRound, state.totalRounds),
                fontWeight = FontWeight.Bold,
                color = AuraSecondary,
                fontSize = 18.sp
            )

            // 콤보 배지
            AnimatedVisibility(
                visible = state.comboCount >= 2,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                val comboColor = when {
                    state.comboCount >= 5 -> Color(0xFFFF6B35)
                    state.comboCount >= 3 -> AuraSecondary
                    else -> AuraPrimary
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(comboColor.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${state.comboCount}콤보 🔥",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = comboColor
                    )
                }
            }

            Text(
                text = stringResource(Res.string.math_pts_format, state.sessionScore),
                fontWeight = FontWeight.Bold,
                color = AuraTertiary,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── 스피드 보너스 게이지 ──
        val isAnswered = state.selectedOption != null
        val speedBonusLabel = when {
            isAnswered -> ""
            state.timeElapsedMillis <= 2000L -> "+10 스피드!"
            state.timeElapsedMillis <= 4000L -> "+5 스피드"
            state.timeElapsedMillis <= 6000L -> "+2 스피드"
            else -> ""
        }
        val speedColor = when {
            state.timeElapsedMillis <= 2000L -> AuraTertiary
            state.timeElapsedMillis <= 4000L -> AuraSecondary
            state.timeElapsedMillis <= 6000L -> AuraPrimary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        // 게이지: 6초 기준으로 줄어듦
        val gaugeProgress = if (isAnswered) 0f
        else (1f - (state.timeElapsedMillis / 6000f)).coerceIn(0f, 1f)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LinearProgressIndicator(
                progress = gaugeProgress,
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = speedColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = speedBonusLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = speedColor,
                modifier = Modifier.width(80.dp),
                textAlign = TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val problem = state.currentProblem
        if (problem != null) {
            Text(
                text = if (problem.targetType == QuestionType.ENG_TO_KOR)
                    stringResource(Res.string.vocab_q_eng_to_kor)
                else stringResource(Res.string.vocab_q_kor_to_eng),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            val displayWord = if (problem.targetType == QuestionType.ENG_TO_KOR)
                problem.targetWord.english
            else problem.targetWord.korean

            AnimatedContent(
                targetState = displayWord,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                    slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "word_animation"
            ) { word ->
                Text(
                    text = word,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 점수 내역 팝업 ──
            Box(modifier = Modifier.height(72.dp), contentAlignment = Alignment.Center) {
                if (state.lastPointsBreakdown != null) {
                    val bd = state.lastPointsBreakdown
                    val popupScale = remember { Animatable(0.5f) }
                    LaunchedEffect(bd) {
                        popupScale.animateTo(1.1f, tween(200))
                        popupScale.animateTo(1f, tween(100))
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.scale(popupScale.value)
                    ) {
                        // 합계 크게
                        Text(
                            text = "+${bd.total}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = AuraTertiary
                        )
                        // 내역 작게
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("+${bd.base} 기본", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (bd.speedBonus > 0) {
                                Text(
                                    "+${bd.speedBonus} 스피드⚡",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraTertiary
                                )
                            }
                            if (bd.comboBonus > 0) {
                                Text(
                                    "+${bd.comboBonus} 콤보🔥",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraSecondary
                                )
                            }
                        }
                    }
                } else if (state.isCorrectLastAnswer == false) {
                    Text(
                        "❌ 오답!  콤보 초기화",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraError
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── 선택지 버튼 ──
            val shakeOffset = remember { Animatable(0f) }
            LaunchedEffect(state.selectedOption != null && state.isCorrectLastAnswer == false) {
                if (state.selectedOption != null && state.isCorrectLastAnswer == false) {
                    repeat(4) {
                        shakeOffset.animateTo(8f, animationSpec = tween(50))
                        shakeOffset.animateTo(-8f, animationSpec = tween(50))
                    }
                    shakeOffset.animateTo(0f)
                }
            }

            problem.options.forEach { option ->
                val isSelected = state.selectedOption == option

                val scale by animateFloatAsState(
                    targetValue = when {
                        isSelected && state.isCorrectLastAnswer == true -> 1.12f
                        isSelected -> 1.05f
                        else -> 1.0f
                    },
                    animationSpec = tween(300)
                )
                val alpha by animateFloatAsState(
                    targetValue = if (isAnswered && !isSelected) 0.3f else 1.0f,
                    animationSpec = tween(300)
                )
                val bgColor = if (isSelected) {
                    if (state.isCorrectLastAnswer == true) AuraTertiary else AuraError
                } else MaterialTheme.colorScheme.surfaceVariant

                Button(
                    onClick = { viewModel.submitAnswer(option) },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(60.dp)
                        .padding(vertical = 4.dp)
                        .scale(scale)
                        .alpha(alpha)
                        .graphicsLayer(translationX = if (isSelected) shakeOffset.value else 0f),
                    colors = ButtonDefaults.buttonColors(containerColor = bgColor),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isAnswered
                ) {
                    Text(
                        option,
                        fontSize = 18.sp,
                        color = when {
                            isSelected && state.isCorrectLastAnswer == true -> AuraTertiary
                            isSelected && state.isCorrectLastAnswer == false -> AuraError
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = onStopGame) {
            Text(stringResource(Res.string.math_btn_retreat), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
