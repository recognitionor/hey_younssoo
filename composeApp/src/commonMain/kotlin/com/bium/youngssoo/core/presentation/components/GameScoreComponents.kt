package com.bium.youngssoo.core.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bium.youngssoo.core.presentation.theme.*

data class PointsBreakdown(
    val base: Int,
    val speedBonus: Int,
    val comboBonus: Int
) {
    val total: Int get() = base + speedBonus + comboBonus
}

/** 스피드 게이지 바 + 우측 보너스 라벨 */
@Composable
fun SpeedGaugeBar(
    timeElapsedMillis: Long,
    maxTimeMillis: Long = 6000L,
    isAnswered: Boolean,
    modifier: Modifier = Modifier
) {
    val gaugeProgress = if (isAnswered) 0f
    else (1f - (timeElapsedMillis.toFloat() / maxTimeMillis)).coerceIn(0f, 1f)

    val (speedColor, speedLabel) = when {
        isAnswered -> MaterialTheme.colorScheme.surfaceVariant to ""
        timeElapsedMillis <= 2000L -> AuraTertiary to "+10 스피드!"
        timeElapsedMillis <= 4000L -> AuraSecondary to "+5 스피드"
        timeElapsedMillis <= 6000L -> AuraPrimary to "+2 스피드"
        else -> MaterialTheme.colorScheme.onSurfaceVariant to ""
    }

    Row(
        modifier = modifier.fillMaxWidth(),
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
            text = speedLabel,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = speedColor,
            modifier = Modifier.width(84.dp),
            textAlign = TextAlign.End
        )
    }
}

/** 연속 정답 콤보 배지 */
@Composable
fun ComboBadge(comboCount: Int) {
    AnimatedVisibility(
        visible = comboCount >= 2,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        val comboColor = when {
            comboCount >= 5 -> Color(0xFFFF6B35)
            comboCount >= 3 -> AuraSecondary
            else -> AuraPrimary
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(comboColor.copy(alpha = 0.15f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${comboCount}콤보 🔥",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = comboColor
            )
        }
    }
}

/** 정답 후 점수 내역 팝업 (정답: 합계+세부 / 오답: 콤보 초기화 메시지) */
@Composable
fun PointsPopup(
    breakdown: PointsBreakdown?,
    isIncorrect: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.height(72.dp), contentAlignment = Alignment.Center) {
        when {
            breakdown != null -> {
                val popupScale = remember { Animatable(0.5f) }
                LaunchedEffect(breakdown) {
                    popupScale.animateTo(1.1f, tween(200))
                    popupScale.animateTo(1f, tween(100))
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.scale(popupScale.value)
                ) {
                    Text(
                        text = "+${breakdown.total}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = AuraTertiary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("+${breakdown.base} 기본", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (breakdown.speedBonus > 0) {
                            Text("+${breakdown.speedBonus} 스피드⚡", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AuraTertiary)
                        }
                        if (breakdown.comboBonus > 0) {
                            Text("+${breakdown.comboBonus} 콤보🔥", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AuraSecondary)
                        }
                    }
                }
            }
            isIncorrect -> {
                Text(
                    "❌ 오답!  콤보 초기화",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuraError
                )
            }
        }
    }
}
