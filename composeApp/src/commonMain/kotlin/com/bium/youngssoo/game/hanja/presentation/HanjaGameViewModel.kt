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
    val availableGrades: List<String> = emptyList(),
    val selectedGrades: Set<String> = emptySet(),
    val availableQuestionCount: Int = 0,
    val filteredQuestionCount: Int = 0,
    val errorMessage: String? = null
)

class HanjaGameViewModel(
    private val rewardRepository: RewardRepository,
    private val questionRepository: QuestionRepository? = null
) : ViewModel() {

    private val _state = MutableStateFlow(HanjaGameState())
    val state: StateFlow<HanjaGameState> = _state.asStateFlow()

    private var allQuestions: List<QuestionEntity> = emptyList()
    private var questions: List<QuestionEntity> = emptyList()
    private var currentQuestionIndex = 0

    private val gradeOrder = listOf(
        "8급",
        "7급2",
        "7급",
        "6급2",
        "6급",
        "5급2",
        "5급",
        "4급2",
        "4급",
        "3급2",
        "3급",
        "2급",
        "1급"
    )

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
            allQuestions = questionRepository?.getQuestions(QuestionCategory.HANJA) ?: emptyList()
            questions = emptyList()
            updateGradeState()

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
            allQuestions = questionRepository?.getQuestions(QuestionCategory.HANJA) ?: emptyList()
            questions = emptyList()
            updateGradeState()

            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = null
            )
        }
    }

    fun startGame() {
        val currentState = _state.value
        val hasGradeSelection = currentState.availableGrades.isNotEmpty()

        if (hasGradeSelection && currentState.selectedGrades.isEmpty()) {
            _state.value = currentState.copy(errorMessage = "응시할 급수를 하나 이상 선택해 주세요.")
            return
        }

        questions = when {
            allQuestions.isEmpty() -> emptyList()
            hasGradeSelection -> filterQuestionsByGrades(currentState.selectedGrades)
            else -> filterableQuestions().shuffled()
        }

        if (allQuestions.isNotEmpty() && questions.isEmpty()) {
            _state.value = currentState.copy(errorMessage = "선택한 급수에 해당하는 문제가 없습니다.")
            return
        }

        currentQuestionIndex = 0
        _state.value = currentState.copy(
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
            questions = when {
                currentState.availableGrades.isNotEmpty() -> filterQuestionsByGrades(currentState.selectedGrades)
                else -> filterableQuestions().shuffled()
            }
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

    fun toggleGradeSelection(grade: String, selected: Boolean) {
        val currentState = _state.value
        val updatedSelection = if (selected) {
            currentState.selectedGrades + grade
        } else {
            currentState.selectedGrades - grade
        }

        questions = if (updatedSelection.isEmpty()) {
            emptyList()
        } else {
            filterQuestionsByGrades(updatedSelection)
        }

        _state.value = currentState.copy(
            selectedGrades = updatedSelection,
            filteredQuestionCount = if (currentState.availableGrades.isEmpty()) {
                allQuestions.size
            } else {
                questions.size
            },
            errorMessage = null
        )
    }

    fun selectAllGrades() {
        val allGrades = _state.value.availableGrades.toSet()
        questions = if (allGrades.isEmpty()) emptyList() else filterQuestionsByGrades(allGrades)
        _state.value = _state.value.copy(
            selectedGrades = allGrades,
            filteredQuestionCount = if (allGrades.isEmpty()) allQuestions.size else questions.size,
            errorMessage = null
        )
    }

    fun clearGradeSelection() {
        questions = emptyList()
        _state.value = _state.value.copy(
            selectedGrades = emptySet(),
            filteredQuestionCount = 0,
            errorMessage = null
        )
    }

    private fun updateGradeState() {
        val availableGrades = allQuestions
            .map { it.grade }
            .filter { it.isNotBlank() }
            .distinct()
            .sortedWith(compareBy({ gradeSortKey(it) }, { it }))

        val currentSelection = _state.value.selectedGrades
        val preservedSelection = currentSelection.intersect(availableGrades.toSet())
        val selectedGrades = when {
            availableGrades.isEmpty() -> emptySet()
            preservedSelection.isNotEmpty() -> preservedSelection
            else -> availableGrades.toSet()
        }

        questions = when {
            availableGrades.isEmpty() -> filterableQuestions().shuffled()
            selectedGrades.isEmpty() -> emptyList()
            else -> filterQuestionsByGrades(selectedGrades)
        }

        val filterableQuestionCount = if (availableGrades.isEmpty()) {
            filterableQuestions().size
        } else {
            allQuestions.count { it.grade.isNotBlank() }
        }

        _state.value = _state.value.copy(
            availableGrades = availableGrades,
            selectedGrades = selectedGrades,
            availableQuestionCount = filterableQuestionCount,
            filteredQuestionCount = if (availableGrades.isEmpty()) filterableQuestionCount else questions.size
        )
    }

    private fun filterQuestionsByGrades(selectedGrades: Set<String>): List<QuestionEntity> {
        if (selectedGrades.isEmpty()) return emptyList()
        return allQuestions
            .filter { it.grade in selectedGrades }
            .shuffled()
    }

    private fun filterableQuestions(): List<QuestionEntity> {
        val gradedQuestions = allQuestions.filter { it.grade.isNotBlank() }
        return if (gradedQuestions.isNotEmpty()) gradedQuestions else allQuestions
    }

    private fun gradeSortKey(grade: String): Int {
        val index = gradeOrder.indexOf(grade)
        return if (index >= 0) index else Int.MAX_VALUE
    }
}
