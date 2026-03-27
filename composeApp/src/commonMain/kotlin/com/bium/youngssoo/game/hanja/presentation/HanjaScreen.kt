package com.bium.youngssoo.game.hanja.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import com.bium.youngssoo.core.presentation.theme.*

@Composable
fun HanjaScreen(viewModel: HanjaGameViewModel, onNavigateBack: () -> Unit) {
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
        when {
            state.isLoading -> {
                CircularProgressIndicator(color = AuraPrimary)
            }
            !state.isPlaying && !state.isGameOver -> {
                HanjaStartScreen(
                    onStart = { viewModel.startGame() },
                    onBack = onNavigateBack,
                    onRefresh = { viewModel.refreshQuestions() },
                    errorMessage = state.errorMessage
                )
            }
            state.isGameOver -> {
                HanjaGameOverScreen(
                    score = state.sessionScore,
                    onRestart = { viewModel.startGame() },
                    onNavigateBack = onNavigateBack
                )
            }
            else -> {
                HanjaPlayScreen(
                    state = state,
                    viewModel = viewModel,
                    onStopGame = { viewModel.stopGame() }
                )
            }
        }
    }
}

@Composable
fun HanjaStartScreen(
    onStart: () -> Unit,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    errorMessage: String?
) {
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
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = AuraPrimary)
            }
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
                    Text(
                        text = "漢",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraOnPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "한자",
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = AuraPrimary,
                letterSpacing = 2.sp
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(64.dp))
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = AuraPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(0.85f).height(64.dp),
                enabled = errorMessage == null
            ) {
                Text("게임 시작", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AuraOnPrimary)
            }
        }
    }
}

@Composable
fun HanjaGameOverScreen(score: Int, onRestart: () -> Unit, onNavigateBack: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "게임 종료!",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = AuraTertiary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "획득 포인트: $score",
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
            Text("다시 하기", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AuraOnSecondary)
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
fun HanjaPlayScreen(
    state: HanjaGameState,
    viewModel: HanjaGameViewModel,
    onStopGame: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "라운드 ${state.currentRound}/${state.totalRounds}",
                fontWeight = FontWeight.Bold,
                color = AuraSecondary,
                fontSize = 18.sp
            )
            Text(
                text = "${state.sessionScore} pts",
                fontWeight = FontWeight.Bold,
                color = AuraTertiary,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        val problem = state.currentProblem
        if (problem != null) {
            Text(
                text = "이 한자의 뜻은?",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = problem.targetWord.hanja,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "hanja_animation"
            ) { hanjaChar ->
                Text(
                    text = hanjaChar,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            problem.options.forEach { option ->
                val isSelected = state.selectedOption == option
                val isAnswered = state.selectedOption != null
                val isCorrectAnswer = option == problem.targetWord.meaning

                val scale by animateFloatAsState(if (isSelected) 1.05f else 1.0f)
                val alpha by animateFloatAsState(if (isAnswered && !isSelected) 0.3f else 1.0f)

                val color = when {
                    isSelected && state.isCorrectLastAnswer == true -> MaterialTheme.colorScheme.primary
                    isSelected && state.isCorrectLastAnswer == false -> MaterialTheme.colorScheme.error
                    isAnswered && isCorrectAnswer -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                Button(
                    onClick = { viewModel.submitAnswer(option) },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(64.dp)
                        .padding(vertical = 6.dp)
                        .scale(scale)
                        .alpha(alpha),
                    colors = ButtonDefaults.buttonColors(containerColor = color),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isAnswered
                ) {
                    Text(
                        text = option,
                        fontSize = 20.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = onStopGame) {
            Text("그만하기", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
