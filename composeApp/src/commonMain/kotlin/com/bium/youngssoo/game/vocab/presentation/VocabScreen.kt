package com.bium.youngssoo.game.vocab.presentation

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
                    colors = listOf(androidx.compose.ui.graphics.Color(0xFF0D0D15), androidx.compose.ui.graphics.Color(0xFF191922))
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!state.isPlaying && !state.isGameOver) {
            VocabStartScreen(
                onStart = { viewModel.startGame() },
                onBack = onNavigateBack
            )
        } else if (state.isGameOver) {
            VocabGameOverScreen(
                score = state.sessionScore,
                onRestart = { viewModel.startGame() },
                onNavigateBack = onNavigateBack
            )
        } else {
            VocabPlayScreen(
                state = state,
                viewModel = viewModel,
                onStopGame = { viewModel.stopGame() }
            )
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
                        .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(AuraPrimary, AuraSecondary))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AuraOnPrimary, modifier = Modifier.size(48.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            Text(stringResource(Res.string.vocab_title), fontSize = 40.sp, fontWeight = FontWeight.Black, color = AuraPrimary, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(64.dp))
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
        
        Spacer(modifier = Modifier.height(48.dp))
        
        val problem = state.currentProblem
        if (problem != null) {
            Text(
                text = if (problem.targetType == QuestionType.ENG_TO_KOR) stringResource(Res.string.vocab_q_eng_to_kor) else stringResource(Res.string.vocab_q_kor_to_eng),
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            val displayWord = if (problem.targetType == QuestionType.ENG_TO_KOR) problem.targetWord.english else problem.targetWord.korean
            
            AnimatedContent(
                targetState = displayWord,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith 
                    slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "word_animation"
            ) { targetWordStr ->
                Text(
                    text = targetWordStr,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            problem.options.forEach { option ->
                val isSelected = state.selectedOption == option
                val isAnswered = state.selectedOption != null
                
                val scale by animateFloatAsState(if (isSelected) 1.05f else 1.0f)
                val alpha by animateFloatAsState(if (isAnswered && !isSelected) 0.3f else 1.0f)
                
                val color = if (isSelected) {
                    if (state.isCorrectLastAnswer == true) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                } else MaterialTheme.colorScheme.surfaceVariant
                
                Button(
                    onClick = { viewModel.submitAnswer(option) },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(64.dp)
                        .padding(vertical = 6.dp)
                        .scale(scale)
                        .alpha(alpha),
                    colors = ButtonDefaults.buttonColors(containerColor = color),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(option, fontSize = 20.sp, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
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
