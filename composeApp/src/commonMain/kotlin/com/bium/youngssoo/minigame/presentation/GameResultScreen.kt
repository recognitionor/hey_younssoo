package com.bium.youngssoo.minigame.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 게임 결과 화면
 */
@Composable
fun GameResultScreen(
    gameName: String,
    stage: Int,
    score: Int,
    targetScore: Int,
    isCleared: Boolean,
    reason: String? = null,
    onExit: () -> Unit
) {
    // 애니메이션
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D0D15),
                        Color(0xFF1A1A2E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .scale(scale),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 결과 아이콘
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCleared) {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD700).copy(alpha = glowAlpha),
                                    Color(0xFFFF9500).copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFF6B6B).copy(alpha = glowAlpha),
                                    Color(0xFFFF4444).copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isCleared) "🎉" else "💥",
                    fontSize = 56.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 결과 타이틀
            Text(
                text = if (isCleared) "Stage Clear!" else "Game Over",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCleared) Color(0xFFFFD700) else Color(0xFFFF6B6B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$gameName - Stage $stage",
                fontSize = 16.sp,
                color = Color.Gray
            )

            reason?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    fontSize = 14.sp,
                    color = Color.Gray.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 점수 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF252540)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "점수",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$score",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00D4FF)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 진행바
                    val progress = (score.toFloat() / targetScore).coerceIn(0f, 1f)
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "목표",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "$targetScore",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1A1A2E))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = if (isCleared) {
                                                listOf(Color(0xFF00D4FF), Color(0xFF00FF88))
                                            } else {
                                                listOf(Color(0xFFFF6B6B), Color(0xFFFF9500))
                                            }
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onExit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCleared) Color(0xFF00D4FF) else Color(0xFFFF6B6B)
                )
            ) {
                Text(
                    text = "나가기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCleared) Color.Black else Color.White
                )
            }
        }
    }
}
