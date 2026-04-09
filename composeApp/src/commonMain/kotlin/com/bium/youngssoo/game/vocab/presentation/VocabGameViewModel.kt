package com.bium.youngssoo.game.vocab.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bium.youngssoo.core.data.local.QuestionCategory
import com.bium.youngssoo.core.data.local.QuestionEntity
import com.bium.youngssoo.core.data.repository.QuestionRepository
import com.bium.youngssoo.core.media.SoundPlayer
import com.bium.youngssoo.core.media.SoundType
import com.bium.youngssoo.core.presentation.components.PointsBreakdown
import com.bium.youngssoo.game.vocab.domain.model.QuestionType
import com.bium.youngssoo.game.vocab.domain.model.VocabProblem
import com.bium.youngssoo.game.vocab.domain.model.VocabWord
import com.bium.youngssoo.reward.domain.GameType
import com.bium.youngssoo.reward.domain.RewardRepository
import com.bium.youngssoo.core.data.speakTextToSpeech
import com.bium.youngssoo.core.data.stopTextToSpeech
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class VocabGameState(
    val isLoading: Boolean = false,
    val isPlaying: Boolean = false,
    val isGameOver: Boolean = false,
    val currentRound: Int = 1,
    val totalRounds: Int = 10,
    val currentProblem: VocabProblem? = null,
    val selectedOption: String? = null,
    val isCorrectLastAnswer: Boolean? = null,
    val sessionScore: Int = 0,
    val correctAnswers: Int = 0,
    val errorMessage: String? = null,
    // 타이머 및 보너스
    val timeElapsedMillis: Long = 0L,
    val comboCount: Int = 0,
    val lastPointsBreakdown: PointsBreakdown? = null
)

class VocabGameViewModel(
    private val rewardRepository: RewardRepository,
    private val soundPlayer: SoundPlayer,
    private val questionRepository: QuestionRepository
) : ViewModel() {
    private val _state = MutableStateFlow(VocabGameState())
    val state: StateFlow<VocabGameState> = _state.asStateFlow()

    private var questions: List<QuestionEntity> = emptyList()
    private var currentQuestionIndex = 0
    private var timerJob: Job? = null
    private var problemStartTime: Long = 0L

    private val fallbackWords = listOf(
        VocabWord("1", "apple", "사과"),
        VocabWord("2", "banana", "바나나"),
        VocabWord("3", "car", "자동차"),
        VocabWord("4", "bridge", "다리"),
        VocabWord("5", "effort", "노력"),
        VocabWord("6", "success", "성공"),
        VocabWord("7", "failure", "실패"),
        VocabWord("8", "passion", "열정"),
        VocabWord("9", "courage", "용기"),
        VocabWord("10", "dream", "꿈")
    )

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            questionRepository.syncQuestions(QuestionCategory.VOCAB)
            questions = questionRepository.getQuestions(QuestionCategory.VOCAB)
            _state.value = _state.value.copy(isLoading = false, errorMessage = null)
        }
    }

    fun refreshQuestions() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            questionRepository.syncQuestions(QuestionCategory.VOCAB, forceRefresh = true)
            questions = questionRepository.getQuestions(QuestionCategory.VOCAB)
            _state.value = _state.value.copy(isLoading = false, errorMessage = null)
        }
    }

    fun startGame() {
        currentQuestionIndex = 0
        if (questions.isNotEmpty()) questions = questions.shuffled()   // 매 게임마다 순서 섞기
        _state.value = _state.value.copy(
            isPlaying = true,
            isGameOver = false,
            currentRound = 1,
            sessionScore = 0,
            correctAnswers = 0,
            comboCount = 0,
            lastPointsBreakdown = null,
            errorMessage = null
        )
        generateNextProblem()
    }

    private fun generateNextProblem() {
        val currentState = _state.value
        if (currentState.currentRound > currentState.totalRounds) {
            timerJob?.cancel()
            rewardRepository.recordGamePlayed(GameType.VOCAB, currentState.correctAnswers)
            _state.value = currentState.copy(
                isPlaying = false,
                isGameOver = true,
                currentProblem = null,
                selectedOption = null,
                isCorrectLastAnswer = null,
                lastPointsBreakdown = null
            )
            return
        }

        if (questions.isNotEmpty()) {
            generateFromFirebaseData()
        } else {
            generateFromFallbackData()
        }
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        problemStartTime = Clock.System.now().toEpochMilliseconds()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(50)
                val elapsed = Clock.System.now().toEpochMilliseconds() - problemStartTime
                _state.value = _state.value.copy(timeElapsedMillis = elapsed)
            }
        }
    }

    private fun generateFromFirebaseData() {
        val currentState = _state.value
        if (currentQuestionIndex >= questions.size) {
            currentQuestionIndex = 0
            questions = questions.shuffled()
        }
        val questionEntity = questions[currentQuestionIndex]
        currentQuestionIndex++
        val options = questionRepository.parseOptions(questionEntity.options)
        val targetWord = VocabWord(
            id = questionEntity.id,
            english = questionEntity.question,
            korean = questionEntity.answer
        )
        _state.value = currentState.copy(
            currentProblem = VocabProblem(
                targetType = QuestionType.ENG_TO_KOR,
                targetWord = targetWord,
                options = options.shuffled()
            ),
            selectedOption = null,
            isCorrectLastAnswer = null,
            lastPointsBreakdown = null,
            timeElapsedMillis = 0L
        )
        viewModelScope.launch {
            delay(400) // TTS 따뜻해질 시간 및 UI 렌더링 대비 지연
            speakTextToSpeech(targetWord.english)
        }
    }

    private fun generateFromFallbackData() {
        val currentState = _state.value
        val targetWord = fallbackWords.random()
        val type = if (kotlin.random.Random.nextBoolean()) QuestionType.ENG_TO_KOR else QuestionType.KOR_TO_ENG
        val correctOption = if (type == QuestionType.ENG_TO_KOR) targetWord.korean else targetWord.english
        val incorrectOptions = fallbackWords
            .filter { it.id != targetWord.id }
            .shuffled()
            .take(4)
            .map { if (type == QuestionType.ENG_TO_KOR) it.korean else it.english }
        val options = (incorrectOptions + correctOption).shuffled()
        _state.value = currentState.copy(
            currentProblem = VocabProblem(type, targetWord, options),
            selectedOption = null,
            isCorrectLastAnswer = null,
            lastPointsBreakdown = null,
            timeElapsedMillis = 0L
        )
        if (type == QuestionType.ENG_TO_KOR) {
            viewModelScope.launch {
                delay(400)
                speakTextToSpeech(targetWord.english)
            }
        }
    }

    fun submitAnswer(option: String) {
        val currentState = _state.value
        if (!currentState.isPlaying || currentState.currentProblem == null || currentState.selectedOption != null) return

        timerJob?.cancel()

        val problem = currentState.currentProblem
        val isCorrect = if (problem.targetType == QuestionType.ENG_TO_KOR) {
            option == problem.targetWord.korean
        } else {
            option == problem.targetWord.english
        }

        soundPlayer.playSound(if (isCorrect) SoundType.CORRECT else SoundType.INCORRECT)

        val newCombo = if (isCorrect) currentState.comboCount + 1 else 0
        val breakdown = if (isCorrect) {
            val speedBonus = calcSpeedBonus(currentState.timeElapsedMillis)
            val comboBonus = calcComboBonus(newCombo)
            PointsBreakdown(base = 10, speedBonus = speedBonus, comboBonus = comboBonus)
        } else null

        val pointsEarned = breakdown?.total ?: 0
        if (pointsEarned > 0) rewardRepository.addPoints(pointsEarned)

        _state.value = currentState.copy(
            selectedOption = option,
            isCorrectLastAnswer = isCorrect,
            sessionScore = currentState.sessionScore + pointsEarned,
            correctAnswers = if (isCorrect) currentState.correctAnswers + 1 else currentState.correctAnswers,
            comboCount = newCombo,
            lastPointsBreakdown = breakdown
        )

        viewModelScope.launch {
            delay(1500)
            _state.value = _state.value.copy(currentRound = _state.value.currentRound + 1)
            generateNextProblem()
        }
    }

    // 2초 이내 +10, 4초 이내 +5, 6초 이내 +2
    private fun calcSpeedBonus(elapsedMs: Long): Int = when {
        elapsedMs <= 2000L -> 10
        elapsedMs <= 4000L -> 5
        elapsedMs <= 6000L -> 2
        else -> 0
    }

    // 2연속 +3, 3연속 +5, 4연속 +8, 5연속 이상 +12
    private fun calcComboBonus(combo: Int): Int = when {
        combo >= 5 -> 12
        combo == 4 -> 8
        combo == 3 -> 5
        combo == 2 -> 3
        else -> 0
    }

    fun stopGame() {
        stopTextToSpeech()
        timerJob?.cancel()
        _state.value = _state.value.copy(isPlaying = false, isGameOver = false, currentProblem = null)
    }
}
