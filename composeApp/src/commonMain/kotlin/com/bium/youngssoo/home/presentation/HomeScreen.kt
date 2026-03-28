package com.bium.youngssoo.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bium.youngssoo.core.presentation.theme.*
import com.bium.youngssoo.reward.domain.RewardRepository
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import youngsso.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToMath: () -> Unit,
    onNavigateToVocab: () -> Unit,
    onNavigateToHanja: () -> Unit,
    onNavigateToReward: () -> Unit,
    onTestWebView: () -> Unit = {}
) {
    val rewardRepository: RewardRepository = koinInject()

    val level by rewardRepository.level.collectAsState()
    val experience by rewardRepository.experience.collectAsState()
    val currentStreak by rewardRepository.currentStreak.collectAsState()
    val energy by rewardRepository.energy.collectAsState()
    val maxEnergy by rewardRepository.maxEnergy.collectAsState()
    val totalPoints by rewardRepository.totalPoints.collectAsState()

    val levelTitle = rewardRepository.getLevelTitle()
    val levelProgress = rewardRepository.getLevelProgress()
    val requiredExp = rewardRepository.getRequiredExp(level)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Player Profile Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Lv. $level $levelTitle",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "EXP: $experience / $requiredExp",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(levelProgress.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(AuraPrimary, AuraSecondary)
                                )
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Resource Indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ResourceBadge(
                icon = Icons.Default.Star,
                value = "${currentStreak}일 연속",
                color = AuraTertiary
            )
            ResourceBadge(
                icon = Icons.Default.Star,
                value = "$energy/$maxEnergy 에너지",
                color = AuraPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Points indicator
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "보유 포인트",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$totalPoints P",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AuraPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(Res.string.home_select_realm),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Portal Cards
        GameModeCard(
            title = stringResource(Res.string.home_mode_math),
            subtitle = stringResource(Res.string.home_mode_math_desc),
            gradientColors = listOf(Color(0xFF252530), Color(0xFF191922)),
            onClick = onNavigateToMath
        )

        Spacer(modifier = Modifier.height(16.dp))

        GameModeCard(
            title = stringResource(Res.string.home_mode_vocab),
            subtitle = stringResource(Res.string.home_mode_vocab_desc),
            gradientColors = listOf(Color(0xFF252530), Color(0xFF0F0F16)),
            onClick = onNavigateToVocab
        )

        Spacer(modifier = Modifier.height(16.dp))

        GameModeCard(
            title = "한자",
            subtitle = "한자의 뜻을 맞춰보세요",
            gradientColors = listOf(Color(0xFF2D2530), Color(0xFF151018)),
            onClick = onNavigateToHanja
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ResourceBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun GameModeCard(title: String, subtitle: String, gradientColors: List<Color>, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(gradientColors))
            .clickable { onClick() }
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AuraPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
