package com.bium.youngssoo.minigame.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bium.youngssoo.core.presentation.theme.AuraPrimary
import com.bium.youngssoo.core.presentation.theme.AuraTertiary

/**
 * 테스트 모드 플로팅 버튼 + 패널
 */
@Composable
fun TestModeFloatingButton(
    currentPoints: Int,
    onSetPoints: (Int) -> Unit,
    onResetProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPanel by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // 플로팅 버튼
        FloatingActionButton(
            onClick = { showPanel = !showPanel },
            containerColor = Color(0xFF333340),
            contentColor = AuraPrimary,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                if (showPanel) Icons.Default.Close else Icons.Default.Build,
                contentDescription = "Test Mode",
                modifier = Modifier.size(24.dp)
            )
        }

        // 테스트 패널
        AnimatedVisibility(
            visible = showPanel,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 56.dp)
        ) {
            TestPanel(
                currentPoints = currentPoints,
                onSetPoints = onSetPoints,
                onResetProgress = onResetProgress,
                onClose = { showPanel = false }
            )
        }
    }
}

@Composable
private fun TestPanel(
    currentPoints: Int,
    onSetPoints: (Int) -> Unit,
    onResetProgress: () -> Unit,
    onClose: () -> Unit
) {
    var pointsInput by remember { mutableStateOf(currentPoints.toString()) }

    Card(
        modifier = Modifier
            .width(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E2E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 헤더
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Build,
                    contentDescription = null,
                    tint = AuraPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "테스트 모드",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuraPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 현재 포인트 표시
            Text(
                text = "현재 포인트: $currentPoints",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 포인트 입력
            OutlinedTextField(
                value = pointsInput,
                onValueChange = { pointsInput = it.filter { c -> c.isDigit() } },
                label = { Text("포인트 설정", fontSize = 12.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AuraPrimary,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = AuraPrimary,
                    cursorColor = AuraPrimary,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 빠른 포인트 설정 버튼들
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickPointButton(
                    text = "+100",
                    onClick = {
                        val newPoints = currentPoints + 100
                        pointsInput = newPoints.toString()
                        onSetPoints(newPoints)
                    },
                    modifier = Modifier.weight(1f)
                )
                QuickPointButton(
                    text = "+1K",
                    onClick = {
                        val newPoints = currentPoints + 1000
                        pointsInput = newPoints.toString()
                        onSetPoints(newPoints)
                    },
                    modifier = Modifier.weight(1f)
                )
                QuickPointButton(
                    text = "+10K",
                    onClick = {
                        val newPoints = currentPoints + 10000
                        pointsInput = newPoints.toString()
                        onSetPoints(newPoints)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 적용 버튼
            Button(
                onClick = {
                    val points = pointsInput.toIntOrNull() ?: 0
                    onSetPoints(points)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AuraPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("포인트 적용", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = Color.Gray.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(12.dp))

            // 게임 진행도 초기화
            OutlinedButton(
                onClick = onResetProgress,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFFF6B6B)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("게임 진행도 초기화", fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun QuickPointButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF333340)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AuraTertiary
            )
        }
    }
}
