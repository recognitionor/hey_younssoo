package com.bium.youngssoo.game.math.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val correctAnswers: Int = 0
)

class MathGameViewModel(private val rewardRepository: RewardRepository) : ViewModel() {
    private val _state = MutableStateFlow(MathGameState())
    val state: StateFlow<MathGameState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var problemStartTime: Long = 0L

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
                _state.value = _state.value.copy(
                    timeElapsedMillis = now - problemStartTime
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

        val tier = if (isCorrect) {
            when {
                timeTaken <= 1500L -> RewardTier.HUGE 
                timeTaken <= 2500L -> RewardTier.MEDIUM
                timeTaken <= 3500L -> RewardTier.SMALL
                else -> RewardTier.MINIMAL
            }
        } else {
            RewardTier.NONE
        }

        val result = MathResult(
            problem = currentState.currentProblem,
            isCorrect = isCorrect,
            timeTakenMillis = timeTaken,
            rewardTier = tier
        )
        
        if (tier.points > 0) {
            rewardRepository.addPoints(tier.points)
        }

        val newCorrectCount = if (isCorrect) currentState.correctAnswers + 1 else currentState.correctAnswers

        _state.value = currentState.copy(
            lastResult = result,
            sessionScore = currentState.sessionScore + tier.points,
            correctAnswers = newCorrectCount,
            userInput = ""
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
