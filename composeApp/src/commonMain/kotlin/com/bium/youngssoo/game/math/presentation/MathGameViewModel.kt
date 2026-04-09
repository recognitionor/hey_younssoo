package com.bium.youngssoo.game.math.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bium.youngssoo.core.media.SoundPlayer
import com.bium.youngssoo.core.media.SoundType
import com.bium.youngssoo.core.presentation.components.PointsBreakdown
import com.bium.youngssoo.game.math.domain.model.MathOperator
import com.bium.youngssoo.game.math.domain.model.MathProblem
import com.bium.youngssoo.game.math.domain.model.MathResult
import com.bium.youngssoo.game.math.domain.model.RewardTier
import com.bium.youngssoo.reward.domain.RewardRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class MathGameState(
    val currentProblem: MathProblem? = null,
    val userInput: String = "",
    val timeElapsedMillis: Long = 0L,
    val lastResult: MathResult? = null,
    val isPlaying: Boolean = false,
    val isGameOver: Boolean = false,
    val sessionScore: Int = 0,
    val currentRound: Int = 1,
    val correctAnswers: Int = 0,
    val expectedScore: Int = 0,
    val comboCount: Int = 0,
    val lastPointsBreakdown: PointsBreakdown? = null
)

class MathGameViewModel(
    private val rewardRepository: RewardRepository,
    private val soundPlayer: SoundPlayer
) : ViewModel() {
    private val _state = MutableStateFlow(MathGameState())
    val state: StateFlow<MathGameState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var problemStartTime: Long = 0L

    private fun getTimeLimit(round: Int): Long {
        return when (round) {
            1 -> 5000L   // 5초
            2 -> 6000L   // 6초
            3 -> 7000L   // 7초
            4 -> 10000L  // 10초
            5 -> 12000L  // 12초
            else -> 7000L
        }
    }

    private fun getMaxRewardForRound(round: Int): Int {
        return when (round) {
            1, 2, 3 -> 100
            4 -> 200    // 4라운드: 배점 2배
            5 -> 300    // 5라운드: 배점 3배
            else -> 100
        }
    }

    private fun calculateTierForRound(timeTaken: Long, round: Int): RewardTier {
        return if (timeTaken <= 1500L) {
            RewardTier.HUGE
        } else if (timeTaken <= 3000L) {
            RewardTier.MEDIUM
        } else if (timeTaken <= 5000L) {
            RewardTier.SMALL
        } else {
            RewardTier.MINIMAL
        }
    }

    private fun getPointsForTier(tier: RewardTier, round: Int): Int {
        val basePoints = tier.points
        val multiplier = getMaxRewardForRound(round) / 100
        return basePoints * multiplier
    }

    private fun calculateExpectedScore(timeTaken: Long, round: Int): Int {
        val timeLimit = getTimeLimit(round)
        val tier = calculateTierForRound(timeTaken, round)
        val points = getPointsForTier(tier, round)
        return if (timeTaken < timeLimit) points else 0
    }

    fun startGame() {
        _state.value = _state.value.copy(
            isPlaying = true,
            isGameOver = false,
            sessionScore = 0,
            currentRound = 1,
            correctAnswers = 0,
            lastResult = null
        )
        generateNextProblem()
    }

    private fun generateNextProblem() {
        if (_state.value.currentRound > 5) {
            rewardRepository.recordGamePlayed(
                com.bium.youngssoo.reward.domain.GameType.MATH,
                _state.value.correctAnswers
            )
            _state.value = _state.value.copy(
                isPlaying = false,
                isGameOver = true,
                currentProblem = null,
                lastResult = null
            )
            return
        }

        val round = _state.value.currentRound
        val (factor1, factor2, operator) = when (round) {
            1 -> Triple((2..5).random(), (2..5).random(), MathOperator.MULTIPLY)
            2 -> Triple((2..9).random(), (2..9).random(), MathOperator.MULTIPLY)
            3 -> Triple((6..9).random(), (6..9).random(), MathOperator.MULTIPLY)
            4 -> {
                if (kotlin.random.Random.nextBoolean()) {
                    // 2-digit * 1-digit (medium)
                    Triple((11..49).random(), (2..5).random(), MathOperator.MULTIPLY)
                } else {
                    // 2-digit / 1-digit
                    val divisor = (2..9).random()
                    val answer = (2..15).random()
                    val dividend = divisor * answer
                    Triple(dividend, divisor, MathOperator.DIVIDE)
                }
            }
            else -> {
                // Round 5
                if (kotlin.random.Random.nextBoolean()) {
                    // 2-digit * 1-digit (harder)
                    Triple((50..99).random(), (6..9).random(), MathOperator.MULTIPLY)
                } else {
                    // 3-digit / 1-digit or harder 2-digit / 1-digit
                    val divisor = (6..9).random()
                    val answer = (11..40).random()
                    val dividend = divisor * answer
                    Triple(dividend, divisor, MathOperator.DIVIDE)
                }
            }
        }
        val problem = MathProblem(factor1, factor2, operator)
        
        problemStartTime = Clock.System.now().toEpochMilliseconds()
        
        _state.value = _state.value.copy(
            currentProblem = problem,
            userInput = "",
            timeElapsedMillis = 0L
        )

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(50)
                val now = Clock.System.now().toEpochMilliseconds()
                val elapsed = now - problemStartTime
                val expectedScore = calculateExpectedScore(elapsed, _state.value.currentRound)
                _state.value = _state.value.copy(
                    timeElapsedMillis = elapsed,
                    expectedScore = expectedScore
                )
            }
        }
    }

    fun appendInput(digit: String) {
        if (!_state.value.isPlaying) return
        val current = _state.value.userInput
        if (current.length >= 4) return 
        _state.value = _state.value.copy(userInput = current + digit)
    }

    fun clearInput() {
        if (!_state.value.isPlaying) return
        _state.value = _state.value.copy(userInput = "")
    }

    fun submitAnswer() {
        val currentState = _state.value
        if (!currentState.isPlaying || currentState.currentProblem == null) return

        timerJob?.cancel()

        val inputInt = currentState.userInput.toIntOrNull()
        val isCorrect = inputInt == currentState.currentProblem.correctAnswer
        val timeTaken = currentState.timeElapsedMillis
        val currentRound = currentState.currentRound

        val tier = if (isCorrect) {
            calculateTierForRound(timeTaken, currentRound)
        } else {
            RewardTier.NONE
        }

        val points = if (isCorrect) getPointsForTier(tier, currentRound) else 0

        val result = MathResult(
            problem = currentState.currentProblem,
            isCorrect = isCorrect,
            timeTakenMillis = timeTaken,
            rewardTier = tier,
            points = points
        )

        // 정답/오답 사운드 재생
        soundPlayer.playSound(
            if (isCorrect) SoundType.CORRECT else SoundType.INCORRECT
        )

        if (points > 0) {
            rewardRepository.addPoints(points)
        }

        val newCorrectCount = if (isCorrect) currentState.correctAnswers + 1 else currentState.correctAnswers

        _state.value = currentState.copy(
            lastResult = result,
            sessionScore = currentState.sessionScore + points,
            correctAnswers = newCorrectCount,
            userInput = "",
            expectedScore = 0
        )

        viewModelScope.launch {
            delay(1500)
            _state.value = _state.value.copy(currentRound = _state.value.currentRound + 1)
            generateNextProblem()
        }
    }

    fun stopGame() {
        timerJob?.cancel()
        _state.value = _state.value.copy(isPlaying = false, currentProblem = null)
    }
}
