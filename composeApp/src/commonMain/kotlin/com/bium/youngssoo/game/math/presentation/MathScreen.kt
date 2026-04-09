package com.bium.youngssoo.game.math.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import kotlin.math.roundToInt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bium.youngssoo.core.presentation.theme.*
import com.bium.youngssoo.game.math.domain.model.RewardTier
import org.jetbrains.compose.resources.stringResource
import youngsso.composeapp.generated.resources.*

@Composable
fun MathScreen(viewModel: MathGameViewModel, onNavigateBack: () -> Unit) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D0D15), Color(0xFF191922))
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!state.isPlaying && !state.isGameOver) {
            StartScreen(
                score = state.sessionScore,
                onStart = { viewModel.startGame() },
                onBack = onNavigateBack
            )
        } else if (state.isGameOver) {
            GameOverScreen(
                score = state.sessionScore,
                onRestart = { viewModel.startGame() },
                onNavigateBack = onNavigateBack
            )
        } else {
            GamePlayScreen(
                state = state,
                onClearInput = { viewModel.clearInput() },
                onSubmitAnswer = { viewModel.submitAnswer() },
                onAppendInput = { viewModel.appendInput(it) },
                onStopGame = { viewModel.stopGame() }
            )
        }
    }
}

@Composable
fun StartScreen(score: Int, onStart: () -> Unit, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
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
                            Brush.linearGradient(listOf(AuraPrimary, AuraSecondary))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AuraOnPrimary, modifier = Modifier.size(48.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = stringResource(Res.string.math_title),
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = AuraPrimary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.math_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(64.dp))
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = AuraPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(64.dp)
            ) {
                Text(stringResource(Res.string.math_btn_enter), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AuraOnPrimary)
            }
        }
    }
}

@Composable
fun GameOverScreen(score: Int, onRestart: () -> Unit, onNavigateBack: () -> Unit) {
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
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(60.dp)
        ) {
            Text(stringResource(Res.string.math_btn_again), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AuraOnSecondary)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onNavigateBack,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(60.dp)
        ) {
            Text("메인으로", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GamePlayScreen(
    state: MathGameState,
    onClearInput: () -> Unit,
    onSubmitAnswer: () -> Unit,
    onAppendInput: (String) -> Unit,
    onStopGame: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Boss Encounter HUD
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.math_wave_format, state.currentRound, 5),
                fontWeight = FontWeight.Bold,
                color = AuraSecondary,
                fontSize = 18.sp
            )
            Text(
                text = stringResource(Res.string.math_pts_format, state.sessionScore),
                fontWeight = FontWeight.Bold,
                color = AuraTertiary,
                fontSize = 18.sp
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Progress Bar (Timer simulation)
        val timePassed = (state.timeElapsedMillis / 1000f).coerceAtMost(10f)
        val progress = 1f - (timePassed / 10f) // Just a visual effect assuming 10s is a par time
        LinearProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = AuraPrimary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        val formattedSec = ((state.timeElapsedMillis / 100.0).roundToInt() / 10.0).toString()
        Text(
            text = stringResource(Res.string.math_time_format, formattedSec),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        // Equation (Boss)
        AnimatedContent(
            targetState = state.currentProblem,
            transitionSpec = {
                slideInVertically { height -> -height } + fadeIn() togetherWith
                slideOutVertically { height -> height } + fadeOut()
            },
            label = "problem_animation"
        ) { problem ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                val problemText = if (problem != null) {
                    "${problem.factor1} ${problem.operator.symbol} ${problem.factor2} = ?"
                } else {
                    "..."
                }
                val compactWidth = maxWidth < 360.dp
                val fontSize = when {
                    compactWidth && problemText.length >= 10 -> 36.sp
                    compactWidth -> 42.sp
                    problemText.length >= 10 -> 44.sp
                    problemText.length >= 8 -> 52.sp
                    else -> 64.sp
                }
                val letterSpacing = when {
                    fontSize <= 36.sp -> 0.sp
                    fontSize <= 44.sp -> 1.sp
                    fontSize <= 52.sp -> 2.sp
                    else -> 4.sp
                }

                Text(
                    text = problemText,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Black,
                    color = if (problem != null) MaterialTheme.colorScheme.onSurface else AuraPrimary,
                    letterSpacing = letterSpacing,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        // Answer Input Box with Animation
        val hasResult = state.lastResult != null
        val isCorrect = state.lastResult?.isCorrect ?: false

        val shakeOffset = remember { Animatable(0f) }
        LaunchedEffect(hasResult && !isCorrect) {
            if (hasResult && !isCorrect) {
                repeat(4) {
                    shakeOffset.animateTo(8f, animationSpec = tween(50))
                    shakeOffset.animateTo(-8f, animationSpec = tween(50))
                }
                shakeOffset.animateTo(0f)
            }
        }

        val scaleAnim = remember { Animatable(1f) }
        LaunchedEffect(hasResult && isCorrect) {
            if (hasResult && isCorrect) {
                scaleAnim.animateTo(1.15f, animationSpec = tween(200))
                scaleAnim.animateTo(1f, animationSpec = tween(200))
            }
        }

        Box(
            modifier = Modifier
                .width(200.dp)
                .height(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isCorrect && hasResult) AuraTertiary.copy(alpha = 0.2f)
                    else if (!isCorrect && hasResult) AuraError.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .graphicsLayer(
                    translationX = shakeOffset.value,
                    scaleX = scaleAnim.value,
                    scaleY = scaleAnim.value
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (state.userInput.isEmpty()) stringResource(Res.string.math_input_placeholder) else state.userInput,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = if (state.userInput.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else AuraPrimary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Expected Score or Result Display
        Box(modifier = Modifier.height(80.dp), contentAlignment = Alignment.Center) {
            if (state.lastResult != null) {
                val result = state.lastResult!!
                val isCorrect = result.isCorrect
                val points = result.points

                val text = if (isCorrect) "⚡ CRITICAL HIT!\n(+${points})" else "❌ MISS!"
                val color = if (isCorrect) AuraTertiary else AuraError

                // Pulse animation for correct answer
                val pulseScale = remember { Animatable(1f) }
                LaunchedEffect(Unit) {
                    if (isCorrect) {
                        repeat(2) {
                            pulseScale.animateTo(1.15f, animationSpec = tween(300))
                            pulseScale.animateTo(1f, animationSpec = tween(300))
                        }
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(initialScale = 0.5f) + fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                    exit = scaleOut(targetScale = 0.5f) + fadeOut()
                ) {
                    Text(
                        text = text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = color,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.scale(if (isCorrect) pulseScale.value else 1f)
                    )
                }
            } else if (state.isPlaying) {
                // Show expected score during gameplay
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "예상 점수",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "+${state.expectedScore}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Keypad
        val keys = listOf(
            "1", "2", "3",
            "4", "5", "6",
            "7", "8", "9",
            "C", "0", "GO"
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(24.dp)
        ) {
            for (row in 0..3) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        val key = keys[index]
                        
                        val isActionKey = key == "GO" || key == "C"
                        val bgColor = when (key) {
                            "GO" -> AuraPrimary
                            "C" -> AuraErrorContainer
                            else -> MaterialTheme.colorScheme.surface
                        }
                        val txtColor = when (key) {
                            "GO" -> AuraOnPrimary
                            "C" -> AuraOnErrorContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(bgColor)
                                .clickable {
                                    when (key) {
                                        "C" -> onClearInput()
                                        "GO" -> onSubmitAnswer()
                                        else -> onAppendInput(key)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                fontSize = if (isActionKey) 20.sp else 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = txtColor
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                onClick = onStopGame,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(stringResource(Res.string.math_btn_retreat), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
