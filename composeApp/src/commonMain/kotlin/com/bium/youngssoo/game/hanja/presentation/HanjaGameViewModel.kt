package com.bium.youngssoo.game.hanja.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bium.youngssoo.core.data.local.QuestionCategory
import com.bium.youngssoo.core.data.local.QuestionEntity
import com.bium.youngssoo.core.data.repository.QuestionRepository
import com.bium.youngssoo.core.media.SoundPlayer
import com.bium.youngssoo.core.media.SoundType
import com.bium.youngssoo.core.presentation.components.PointsBreakdown
import com.bium.youngssoo.game.hanja.domain.model.HanjaProblem
import com.bium.youngssoo.game.hanja.domain.model.HanjaWord
import com.bium.youngssoo.reward.domain.GameType
import com.bium.youngssoo.reward.domain.RewardRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class HanjaGameState(
    val isLoading: Boolean = false,
    val loadingMessage: String? = null,
    val isPlaying: Boolean = false,
    val isGameOver: Boolean = false,
    val currentRound: Int = 1,
    val totalRounds: Int = 10,
    val currentProblem: HanjaProblem? = null,
    val selectedOption: String? = null,
    val isCorrectLastAnswer: Boolean? = null,
    val sessionScore: Int = 0,
    val correctAnswers: Int = 0,
    val timeElapsedMillis: Long = 0L,
    val comboCount: Int = 0,
    val lastPointsBreakdown: PointsBreakdown? = null,
    val availableGrades: List<String> = emptyList(),
    val selectedGrades: Set<String> = emptySet(),
    val availableQuestionCount: Int = 0,
    val filteredQuestionCount: Int = 0,
    val errorMessage: String? = null
)

class HanjaGameViewModel(
    private val rewardRepository: RewardRepository,
    private val soundPlayer: SoundPlayer,
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HanjaGameState())
    val state: StateFlow<HanjaGameState> = _state.asStateFlow()

    private var allQuestions: List<QuestionEntity> = emptyList()
    private var questions: List<QuestionEntity> = emptyList()
    private var currentQuestionIndex = 0
    private var timerJob: Job? = null
    private var problemStartTime: Long = 0L

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
            _state.value = _state.value.copy(isLoading = true, loadingMessage = null)

            // 로컬 DB에서 문제 가져오기 (초기 동기화 X)
            allQuestions = questionRepository.getQuestions(QuestionCategory.HANJA)
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

            questionRepository.syncQuestions(QuestionCategory.HANJA, forceRefresh = true)
            allQuestions = questionRepository.getQuestions(QuestionCategory.HANJA)
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

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, loadingMessage = "선택하신 한자 데이터를 다운로드 중입니다... ⏳")

            // 선택한 급수별 데이터 다운로드
            questionRepository.syncQuestionsByGrades(QuestionCategory.HANJA, currentState.selectedGrades)

            // 다운로드 완료 후 로컬 갱신
            allQuestions = questionRepository.getQuestions(QuestionCategory.HANJA)
            questions = filterQuestionsByGrades(currentState.selectedGrades)

            if (questions.isEmpty()) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    loadingMessage = null,
                    errorMessage = "선택한 급수에 해당하는 문제가 없습니다."
                )
                return@launch
            }

            currentQuestionIndex = 0
            _state.value = _state.value.copy(
                isLoading = false,
                loadingMessage = null,
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
    }

    private fun generateNextProblem() {
        val currentState = _state.value

        if (currentState.currentRound > currentState.totalRounds) {
            timerJob?.cancel()
            rewardRepository.recordGamePlayed(GameType.HANJA, currentState.correctAnswers)
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

        // Firebase 데이터가 있으면 사용, 없으면 fallback
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
            questions = when {
                currentState.availableGrades.isNotEmpty() -> filterQuestionsByGrades(currentState.selectedGrades)
                else -> filterableQuestions().shuffled()
            }
        }

        val questionEntity = questions[currentQuestionIndex]
        currentQuestionIndex++

        val options = questionRepository.parseOptions(questionEntity.options)
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
            isCorrectLastAnswer = null,
            lastPointsBreakdown = null,
            timeElapsedMillis = 0L
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
            isCorrectLastAnswer = null,
            lastPointsBreakdown = null,
            timeElapsedMillis = 0L
        )
    }

    fun submitAnswer(option: String) {
        val currentState = _state.value
        if (!currentState.isPlaying || currentState.currentProblem == null || currentState.selectedOption != null) return

        timerJob?.cancel()

        val problem = currentState.currentProblem
        val isCorrect = option == problem.targetWord.meaning

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
        timerJob?.cancel()
        _state.value = _state.value.copy(isPlaying = false, isGameOver = false, currentProblem = null)
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
        val availableGrades = gradeOrder // 항상 고정된 급수 목록 표시

        val currentSelection = _state.value.selectedGrades
        val selectedGrades = if (currentSelection.isEmpty()) availableGrades.toSet() else currentSelection

        questions = filterQuestionsByGrades(selectedGrades)

        val filterableQuestionCount = allQuestions.count { it.grade.isNotBlank() }

        _state.value = _state.value.copy(
            availableGrades = availableGrades,
            selectedGrades = selectedGrades,
            availableQuestionCount = filterableQuestionCount,
            filteredQuestionCount = questions.size
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
