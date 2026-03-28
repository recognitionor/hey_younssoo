package com.bium.youngssoo.minigame.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bium.youngssoo.minigame.data.local.MiniGameProgressEntity
import com.bium.youngssoo.minigame.data.model.CostType
import com.bium.youngssoo.minigame.data.model.GameResult
import com.bium.youngssoo.minigame.data.model.MiniGame
import com.bium.youngssoo.minigame.data.repository.MiniGameProgressRepository
import com.bium.youngssoo.reward.domain.RewardRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.intOrNull

data class MiniGameListState(
    val isLoading: Boolean = false,
    val games: List<MiniGame> = emptyList(),
    val totalPoints: Int = 0,
    val errorMessage: String? = null,
    val gameProgress: Map<String, Int> = emptyMap() // gameId -> currentStage
)

class MiniGameViewModel(
    private val rewardRepository: RewardRepository,
    private val progressRepository: MiniGameProgressRepository,
    private val httpClient: HttpClient? = null
) : ViewModel() {

    private val _state = MutableStateFlow(MiniGameListState())
    val state: StateFlow<MiniGameListState> = _state.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }
    private val projectId = "heyyoungssoo"

    init {
        loadGames()
        observePoints()
        observeProgress()
    }

    private fun observePoints() {
        viewModelScope.launch {
            rewardRepository.totalPoints.collect { points ->
                _state.value = _state.value.copy(totalPoints = points)
            }
        }
    }

    private fun observeProgress() {
        viewModelScope.launch {
            progressRepository.observeAllProgress().collect { progressList ->
                val progressMap = progressList.associate { it.gameId to it.currentStage }
                _state.value = _state.value.copy(gameProgress = progressMap)
            }
        }
    }

    private fun loadGames() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            println("[MiniGameViewModel] loadGames() called, httpClient=$httpClient")

            if (httpClient == null) {
                println("[MiniGameViewModel] httpClient is null, returning")
                _state.value = _state.value.copy(isLoading = false)
                return@launch
            }

            try {
                val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/mini_games"
                println("[MiniGameViewModel] Fetching from: $url")
                val response: String = httpClient.get(url).body()
                println("[MiniGameViewModel] Response: ${response.take(200)}")
                val jsonResponse = json.parseToJsonElement(response).jsonObject

                val games = jsonResponse["documents"]?.jsonArray?.mapNotNull { docElement ->
                    try {
                        val doc = docElement.jsonObject
                        val fields = doc["fields"]?.jsonObject ?: return@mapNotNull null
                        val name = doc["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
                        val id = name.substringAfterLast("/")

                        MiniGame(
                            id = id,
                            name = fields["name"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                            description = fields["description"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                            thumbnailUrl = fields["thumbnailUrl"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                            gameUrl = fields["gameUrl"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "",
                            costType = when (fields["costType"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content) {
                                "PLAYS" -> CostType.PLAYS
                                else -> CostType.TIME
                            },
                            costAmount = fields["costAmount"]?.jsonObject?.get("integerValue")?.jsonPrimitive?.intOrNull ?: 100,
                            playValue = fields["playValue"]?.jsonObject?.get("integerValue")?.jsonPrimitive?.intOrNull ?: 180
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                println("[MiniGameViewModel] Games loaded: ${games.size} items")
                _state.value = _state.value.copy(isLoading = false, games = games)
            } catch (e: Exception) {
                println("[MiniGameViewModel] Error: ${e.message}")
                e.printStackTrace()
                _state.value = _state.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun purchaseGame(game: MiniGame) {
        viewModelScope.launch {
            if (_state.value.totalPoints >= game.costAmount) {
                rewardRepository.addPoints(-game.costAmount)
            }
        }
    }

    fun onGameResult(result: GameResult) {
        viewModelScope.launch {
            // 게임 결과 처리 (보너스 포인트 등)
            if (result.isCompleted) {
                val bonusPoints = result.score / 10  // 점수의 10%를 보너스로
                rewardRepository.addPoints(bonusPoints)
            }
            // 플레이 기록 저장
            progressRepository.recordPlay(result.gameId, result.playTime)
            progressRepository.updateHighScore(result.gameId, result.score)
        }
    }

    /**
     * 게임 시작 시 진행 데이터를 가져옴
     */
    suspend fun getGameProgress(gameId: String): MiniGameProgressEntity {
        return progressRepository.getOrCreateProgress(gameId)
    }

    /**
     * 스테이지 클리어 시 다음 스테이지로 진행
     */
    fun advanceStage(gameId: String) {
        viewModelScope.launch {
            val newStage = progressRepository.advanceToNextStage(gameId)
            println("MiniGameViewModel: advanceStage($gameId) -> newStage=$newStage")
        }
    }

    /**
     * 커스텀 데이터 저장 (게임별 고유 데이터)
     */
    fun saveCustomData(gameId: String, data: String) {
        viewModelScope.launch {
            progressRepository.updateCustomData(gameId, data)
        }
    }

    fun refreshGames() {
        loadGames()
    }

    /**
     * 테스트용: 포인트 직접 설정
     */
    fun setPointsForTesting(points: Int) {
        viewModelScope.launch {
            rewardRepository.setPointsForTesting(points)
        }
    }

    /**
     * 테스트용: 게임 진행도 초기화
     */
    fun resetGameProgress(gameId: String) {
        viewModelScope.launch {
            progressRepository.saveFullProgress(
                MiniGameProgressEntity(gameId = gameId)
            )
        }
    }

    /**
     * 테스트용: 모든 게임 진행도 초기화
     */
    fun resetAllGameProgress() {
        viewModelScope.launch {
            _state.value.games.forEach { game ->
                progressRepository.saveFullProgress(
                    MiniGameProgressEntity(gameId = game.id)
                )
            }
        }
    }
}
