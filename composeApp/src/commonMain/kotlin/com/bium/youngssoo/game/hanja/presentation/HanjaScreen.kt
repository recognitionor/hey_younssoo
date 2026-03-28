package com.bium.youngssoo.game.hanja.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
                    availableGrades = state.availableGrades,
                    selectedGrades = state.selectedGrades,
                    availableQuestionCount = state.availableQuestionCount,
                    filteredQuestionCount = state.filteredQuestionCount,
                    onToggleGrade = viewModel::toggleGradeSelection,
                    onSelectAllGrades = viewModel::selectAllGrades,
                    onClearGradeSelection = viewModel::clearGradeSelection,
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
    availableGrades: List<String>,
    selectedGrades: Set<String>,
    availableQuestionCount: Int,
    filteredQuestionCount: Int,
    onToggleGrade: (String, Boolean) -> Unit,
    onSelectAllGrades: () -> Unit,
    onClearGradeSelection: () -> Unit,
    errorMessage: String?
) {
    val hasGradeData = availableGrades.isNotEmpty()
    val contentScrollState = rememberScrollState()
    val gradeScrollState = rememberScrollState()

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

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(contentScrollState)
                    .padding(bottom = 112.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(8.dp))
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

                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "한자",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    color = AuraPrimary,
                    letterSpacing = 2.sp
                )

                if (hasGradeData) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                        ),
                        border = BorderStroke(1.dp, AuraPrimary.copy(alpha = 0.24f)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "응시할 급수를 선택하세요",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "체크한 급수의 한자만 섞어서 출제됩니다.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onSelectAllGrades,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("전체 선택")
                                }
                                OutlinedButton(
                                    onClick = onClearGradeSelection,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("선택 해제")
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 140.dp)
                                    .verticalScroll(gradeScrollState),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                availableGrades.chunked(2).forEach { rowGrades ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        rowGrades.forEach { grade ->
                                            GradeCheckboxCard(
                                                grade = grade,
                                                checked = grade in selectedGrades,
                                                modifier = Modifier.weight(1f),
                                                onCheckedChange = { checked -> onToggleGrade(grade, checked) }
                                            )
                                        }
                                        if (rowGrades.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "선택 급수 ${selectedGrades.size}개 · 출제 가능 ${filteredQuestionCount}문제 / 전체 ${availableQuestionCount}문제",
                                fontSize = 13.sp,
                                color = if (selectedGrades.isEmpty()) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    AuraSecondary
                                }
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = AuraPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(64.dp)
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
                enabled = errorMessage == null && (!hasGradeData || selectedGrades.isNotEmpty())
            ) {
                Text("게임 시작", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AuraOnPrimary)
            }
        }
    }
}

@Composable
private fun GradeCheckboxCard(
    grade: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (checked) {
            AuraPrimary.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (checked) AuraPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        onClick = { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = grade,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
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
                text = "이 한자의 음훈은?",
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
