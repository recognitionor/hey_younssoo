@file:OptIn(ExperimentalMaterial3Api::class)

package com.bium.youngssoo

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.bium.youngssoo.game.math.presentation.MathScreen
import com.bium.youngssoo.game.math.presentation.MathGameViewModel
import com.bium.youngssoo.game.vocab.presentation.VocabScreen
import com.bium.youngssoo.game.vocab.presentation.VocabGameViewModel
import com.bium.youngssoo.game.hanja.presentation.HanjaScreen
import com.bium.youngssoo.game.hanja.presentation.HanjaGameViewModel
import androidx.compose.runtime.rememberCoroutineScope
import com.bium.youngssoo.minigame.data.model.GameScreenOrientation
import com.bium.youngssoo.minigame.data.model.MiniGame
import com.bium.youngssoo.minigame.presentation.GameInitData
import com.bium.youngssoo.minigame.presentation.MiniGameListScreen
import com.bium.youngssoo.minigame.presentation.MiniGameViewModel
import com.bium.youngssoo.minigame.presentation.WebViewGameScreen
import kotlinx.coroutines.launch
import com.bium.youngssoo.reward.presentation.RewardScreen
import com.bium.youngssoo.reward.presentation.RewardViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import youngsso.composeapp.generated.resources.*

// 메인 탭
enum class MainTab {
    LEARN,      // 학습 (수학, 영어, 한자)
    MINIGAME,   // 미니게임
    MYPAGE      // 마이페이지/보상
}

// 학습 게임 화면
enum class LearnScreen {
    HOME,
    MATH,
    VOCAB,
    HANJA
}

@Composable
fun App() {
    com.bium.youngssoo.core.presentation.theme.AuraNocturnaTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                var selectedTab by remember { mutableStateOf(MainTab.LEARN) }
                var learnScreen by remember { mutableStateOf(LearnScreen.HOME) }
                var playingGame by remember { mutableStateOf<MiniGame?>(null) }
                var gameInitData by remember { mutableStateOf<GameInitData?>(null) }
                var showTestWebView by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()

                PlatformOrientationEffect(orientation = GameScreenOrientation.PORTRAIT)

                // 미니게임 플레이 중일 때
                if (playingGame != null) {
                    val miniGameViewModel = koinViewModel<MiniGameViewModel>()
                    WebViewGameScreen(
                        game = playingGame!!,
                        gameInitData = gameInitData,
                        playTime = playingGame!!.playValue,
                        onGameEnd = { result ->
                            miniGameViewModel.onGameResult(result)
                        },
                        onStageCleared = {
                            miniGameViewModel.advanceStage(playingGame!!.id)
                        },
                        onSaveCustomData = { data ->
                            miniGameViewModel.saveCustomData(playingGame!!.id, data)
                        },
                        onClose = {
                            playingGame = null
                            gameInitData = null
                        }
                    )
                } else {
                    // 일반 화면
                    Scaffold(
                        contentWindowInsets = if (learnScreen == LearnScreen.HOME) {
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Horizontal + WindowInsetsSides.Top
                            )
                        } else {
                            WindowInsets.safeDrawing
                        },
                        bottomBar = {
                            // 학습 게임 화면에서는 탭바 숨김
                            if (learnScreen == LearnScreen.HOME) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ) {
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.Home, contentDescription = "학습") },
                                        label = { Text(stringResource(Res.string.home_tab_home)) },
                                        selected = selectedTab == MainTab.LEARN,
                                        onClick = { selectedTab = MainTab.LEARN }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.List, contentDescription = "미니게임") },
                                        label = { Text(stringResource(Res.string.home_tab_guild)) },
                                        selected = selectedTab == MainTab.MINIGAME,
                                        onClick = { selectedTab = MainTab.MINIGAME }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.Person, contentDescription = "마이페이지") },
                                        label = { Text(stringResource(Res.string.home_tab_hero)) },
                                        selected = selectedTab == MainTab.MYPAGE,
                                        onClick = { selectedTab = MainTab.MYPAGE }
                                    )
                                }
                            }
                        }
                    ) { paddingValues ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            when (selectedTab) {
                                MainTab.LEARN -> {
                                    LearnTabContent(
                                        currentScreen = learnScreen,
                                        onScreenChange = { learnScreen = it },
                                        onTestWebView = { showTestWebView = true }
                                    )
                                }
                                MainTab.MINIGAME -> {
                                    val miniGameViewModel = koinViewModel<MiniGameViewModel>()
                                    MiniGameListScreen(
                                        onPlayGame = { game ->
                                            coroutineScope.launch {
                                                val progress = miniGameViewModel.getGameProgress(game.id)
                                                gameInitData = GameInitData(
                                                    stage = progress.currentStage,
                                                    highScore = progress.highScore,
                                                    customData = progress.customData
                                                )
                                                playingGame = game
                                            }
                                        }
                                    )
                                }
                                MainTab.MYPAGE -> {
                                    val viewModel = koinViewModel<RewardViewModel>()
                                    RewardScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LearnTabContent(
    currentScreen: LearnScreen,
    onScreenChange: (LearnScreen) -> Unit,
    onTestWebView: () -> Unit = {}
) {
    when (currentScreen) {
        LearnScreen.HOME -> {
            com.bium.youngssoo.home.presentation.HomeScreen(
                onNavigateToMath = { onScreenChange(LearnScreen.MATH) },
                onNavigateToVocab = { onScreenChange(LearnScreen.VOCAB) },
                onNavigateToHanja = { onScreenChange(LearnScreen.HANJA) },
                onNavigateToReward = { },  // 탭에서 처리
                onTestWebView = onTestWebView
            )
        }
        LearnScreen.MATH -> {
            val viewModel = koinViewModel<MathGameViewModel>()
            PlatformBackHandler(true) {
                viewModel.stopGame()
                onScreenChange(LearnScreen.HOME)
            }
            MathScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    viewModel.stopGame()
                    onScreenChange(LearnScreen.HOME)
                }
            )
        }
        LearnScreen.VOCAB -> {
            val viewModel = koinViewModel<VocabGameViewModel>()
            PlatformBackHandler(true) {
                viewModel.stopGame()
                onScreenChange(LearnScreen.HOME)
            }
            VocabScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    viewModel.stopGame()
                    onScreenChange(LearnScreen.HOME)
                }
            )
        }
        LearnScreen.HANJA -> {
            val viewModel = koinViewModel<HanjaGameViewModel>()
            PlatformBackHandler(true) {
                viewModel.stopGame()
                onScreenChange(LearnScreen.HOME)
            }
            HanjaScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    viewModel.stopGame()
                    onScreenChange(LearnScreen.HOME)
                }
            )
        }
    }
}
