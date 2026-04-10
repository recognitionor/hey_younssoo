package com.bium.youngssoo.reward.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RewardScreen(viewModel: RewardViewModel) {
    val points by viewModel.totalPoints.collectAsState()
    val mathCount by viewModel.mathDailyCount.collectAsState()
    val vocabCount by viewModel.vocabDailyCount.collectAsState()
    val weeklyCount by viewModel.weeklyTotalCount.collectAsState()

    val isDailyClaimed by viewModel.isDailyRewardClaimed.collectAsState()
    val isWeeklyClaimed by viewModel.isWeeklyRewardClaimed.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "마이페이지",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("나의 보상 (Gold)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$points G",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = tertiaryColor
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Daily Missions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("일일 미션", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("모두 달성 시 500 G 자동 지급", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    MissionProgressRow("수학 던전 클리어", mathCount, 3, secondaryColor)
                    Spacer(modifier = Modifier.height(16.dp))
                    MissionProgressRow("영단어 아레나 50점", vocabCount, 3, secondaryColor)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    val isDailyComplete = mathCount >= 3 && vocabCount >= 3
                    Button(
                        onClick = { viewModel.claimDailyReward() },
                        enabled = isDailyComplete && !isDailyClaimed,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = secondaryColor,
                            disabledContainerColor = if (isDailyClaimed) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f)
                        )
                    ) {
                        if (isDailyClaimed) {
                            Text("수령 완료", color = MaterialTheme.colorScheme.onSurface)
                        } else if (isDailyComplete) {
                            Text("500 G 보상 받기", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
                        } else {
                            Text("미션 진행 중", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Weekly Missions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("주간 미션", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = tertiaryColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("누적 30회 플레이 달성 시 2000 G 자동 지급", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    MissionProgressRow("총 플레이 횟수", weeklyCount, 30, tertiaryColor)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    val isWeeklyComplete = weeklyCount >= 30
                    Button(
                        onClick = { viewModel.claimWeeklyReward() },
                        enabled = isWeeklyComplete && !isWeeklyClaimed,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tertiaryColor,
                            disabledContainerColor = if (isWeeklyClaimed) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f)
                        )
                    ) {
                        if (isWeeklyClaimed) {
                            Text("수령 완료", color = MaterialTheme.colorScheme.onSurface)
                        } else if (isWeeklyComplete) {
                            Text("2000 G 보상 받기", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiary)
                        } else {
                            Text("미션 진행 중", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MissionProgressRow(title: String, current: Int, target: Int, color: androidx.compose.ui.graphics.Color) {
    val progress = (current.toFloat() / target).coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("$current / $target", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(MaterialTheme.colorScheme.surface)) {
            Box(modifier = Modifier.fillMaxWidth(progress).height(10.dp).clip(RoundedCornerShape(5.dp)).background(color))
        }
    }
}
