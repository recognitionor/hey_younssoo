package com.bium.youngssoo.game.hanja.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bium.youngssoo.core.data.local.QuestionCategory
import com.bium.youngssoo.core.data.local.QuestionEntity
import com.bium.youngssoo.core.data.repository.QuestionRepository
import com.bium.youngssoo.game.hanja.domain.model.HanjaProblem
import com.bium.youngssoo.game.hanja.domain.model.HanjaWord
import com.bium.youngssoo.reward.domain.GameType
import com.bium.youngssoo.reward.domain.RewardRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HanjaGameState(
    val isLoading: Boolean = false,
    val isPlaying: Boolean = false,
    val isGameOver: Boolean = false,
    val currentRound: Int = 1,
    val totalRounds: Int = 5,
    val currentProblem: HanjaProblem? = null,
    val selectedOption: String? = null,
    val isCorrectLastAnswer: Boolean? = null,
    val sessionScore: Int = 0,
    val correctAnswers: Int = 0,
    val errorMessage: String? = null
)

class HanjaGameViewModel(
    private val rewardRepository: RewardRepository,
    private val questionRepository: QuestionRepository? = null
) : ViewModel() {

    private val _state = MutableStateFlow(HanjaGameState())
    val state: StateFlow<HanjaGameState> = _state.asStateFlow()

    private var questions: List<QuestionEntity> = emptyList()
    private var currentQuestionIndex = 0

    // 기본 한자 단어 (Firebase에서 데이터를 못 가져올 경우 대비)
    private val fallbackWords = listOf(
        HanjaWord("1", "山", "산"),
        HanjaWord("2", "水", "물"),
        HanjaWord("3", "火", "불"),
        HanjaWord("4", "木", "나무"),
        HanjaWord("5", "金", "쇠/금"),
        HanjaWord("6", "土", "흙"),
        HanjaWord("7", "日", "해/날"),
        HanjaWord("8", "月", "달"),
        HanjaWord("9", "人", "사람"),
        HanjaWord("10", "天", "하늘")
    )

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // Firebase에서 동기화 시도
            questionRepository?.syncQuestions(QuestionCategory.HANJA)

            // 로컬 DB에서 문제 가져오기
            questions = questionRepository?.getQuestions(QuestionCategory.HANJA) ?: emptyList()

            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = null
            )
        }
    }

    fun refreshQuestions() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            questionRepository?.syncQuestions(QuestionCategory.HANJA, forceRefresh = true)
            questions = questionRepository?.getQuestions(QuestionCategory.HANJA) ?: emptyList()

            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = null
            )
        }
    }

    fun startGame() {
        currentQuestionIndex = 0
        _state.value = _state.value.copy(
            isPlaying = true,
            isGameOver = false,
            currentRound = 1,
            sessionScore = 0,
            correctAnswers = 0,
            errorMessage = null
        )
        generateNextProblem()
    }

    private fun generateNextProblem() {
        val currentState = _state.value

        if (currentState.currentRound > currentState.totalRounds) {
            rewardRepository.recordGamePlayed(GameType.HANJA, currentState.correctAnswers)
            _state.value = currentState.copy(
                isPlaying = false,
                isGameOver = true,
                currentProblem = null,
                selectedOption = null,
                isCorrectLastAnswer = null
            )
            return
        }

        // Firebase 데이터가 있으면 사용, 없으면 fallback
        if (questions.isNotEmpty()) {
            generateFromFirebaseData()
        } else {
            generateFromFallbackData()
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

        val options = questionRepository?.parseOptions(questionEntity.options) ?: emptyList()
        val targetWord = HanjaWord(
            id = questionEntity.id,
            hanja = questionEntity.question,
            meaning = questionEntity.answer
        )

        _state.value = currentState.copy(
            currentProblem = HanjaProblem(
                targetWord = targetWord,
                options = options.shuffled()
            ),
            selectedOption = null,
            isCorrectLastAnswer = null
        )
    }

    private fun generateFromFallbackData() {
        val currentState = _state.value

        val targetWord = fallbackWords.random()
        val correctOption = targetWord.meaning

        val incorrectOptions = fallbackWords
            .filter { it.id != targetWord.id }
            .shuffled()
            .take(4)
            .map { it.meaning }

        val options = (incorrectOptions + correctOption).shuffled()

        _state.value = currentState.copy(
            currentProblem = HanjaProblem(targetWord, options),
            selectedOption = null,
            isCorrectLastAnswer = null
        )
    }

    fun submitAnswer(option: String) {
        val currentState = _state.value
        if (!currentState.isPlaying || currentState.currentProblem == null || currentState.selectedOption != null) return

        val problem = currentState.currentProblem
        val isCorrect = option == problem.targetWord.meaning

        if (isCorrect) {
            rewardRepository.addPoints(10)
        }

        val newScore = if (isCorrect) currentState.sessionScore + 10 else currentState.sessionScore
        val newCorrectCount = if (isCorrect) currentState.correctAnswers + 1 else currentState.correctAnswers

        _state.value = currentState.copy(
            selectedOption = option,
            isCorrectLastAnswer = isCorrect,
            sessionScore = newScore,
            correctAnswers = newCorrectCount
        )

        viewModelScope.launch {
            delay(1500)
            _state.value = _state.value.copy(currentRound = _state.value.currentRound + 1)
            generateNextProblem()
        }
    }

    fun stopGame() {
        _state.value = _state.value.copy(
            isPlaying = false,
            isGameOver = false,
            currentProblem = null
        )
    }
}
