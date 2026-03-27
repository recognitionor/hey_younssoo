package com.bium.youngssoo.game.vocab.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bium.youngssoo.core.data.local.QuestionCategory
import com.bium.youngssoo.core.data.local.QuestionEntity
import com.bium.youngssoo.core.data.repository.QuestionRepository
import com.bium.youngssoo.game.vocab.domain.model.QuestionType
import com.bium.youngssoo.game.vocab.domain.model.VocabProblem
import com.bium.youngssoo.game.vocab.domain.model.VocabWord
import com.bium.youngssoo.reward.domain.GameType
import com.bium.youngssoo.reward.domain.RewardRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VocabGameState(
    val isLoading: Boolean = false,
    val isPlaying: Boolean = false,
    val isGameOver: Boolean = false,
    val currentRound: Int = 1,
    val totalRounds: Int = 5,
    val currentProblem: VocabProblem? = null,
    val selectedOption: String? = null,
    val isCorrectLastAnswer: Boolean? = null,
    val sessionScore: Int = 0,
    val correctAnswers: Int = 0,
    val errorMessage: String? = null
)

class VocabGameViewModel(
    private val rewardRepository: RewardRepository,
    private val questionRepository: QuestionRepository? = null
) : ViewModel() {
    private val _state = MutableStateFlow(VocabGameState())
    val state: StateFlow<VocabGameState> = _state.asStateFlow()

    private var questions: List<QuestionEntity> = emptyList()
    private var currentQuestionIndex = 0

    // 기본 단어 (Firebase에서 데이터를 못 가져올 경우 대비)
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

            // Firebase에서 동기화 시도
            questionRepository?.syncQuestions(QuestionCategory.VOCAB)

            // 로컬 DB에서 문제 가져오기
            questions = questionRepository?.getQuestions(QuestionCategory.VOCAB) ?: emptyList()

            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = null
            )
        }
    }

    fun refreshQuestions() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            questionRepository?.syncQuestions(QuestionCategory.VOCAB, forceRefresh = true)
            questions = questionRepository?.getQuestions(QuestionCategory.VOCAB) ?: emptyList()

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
            rewardRepository.recordGamePlayed(GameType.VOCAB, currentState.correctAnswers)
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
        val targetWord = VocabWord(
            id = questionEntity.id,
            english = questionEntity.question,
            korean = questionEntity.answer
        )

        // 영어 → 한글 문제만 출제 (Firebase 데이터는 이 형식)
        _state.value = currentState.copy(
            currentProblem = VocabProblem(
                targetType = QuestionType.ENG_TO_KOR,
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
            isCorrectLastAnswer = null
        )
    }

    fun submitAnswer(option: String) {
        val currentState = _state.value
        if (!currentState.isPlaying || currentState.currentProblem == null || currentState.selectedOption != null) return

        val problem = currentState.currentProblem
        val isCorrect = if (problem.targetType == QuestionType.ENG_TO_KOR) {
            option == problem.targetWord.korean
        } else {
            option == problem.targetWord.english
        }

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
        _state.value = _state.value.copy(isPlaying = false, isGameOver = false, currentProblem = null)
    }
}
