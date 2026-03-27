package com.bium.youngssoo.reward.domain

import com.bium.youngssoo.reward.data.local.UserStatsDao
import com.bium.youngssoo.reward.data.local.UserStatsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.min

enum class GameType {
    MATH, VOCAB, HANJA
}

/**
 * 점수/에너지 밸런스 설계:
 *
 * [포인트 획득]
 * - 문제 정답: 10포인트 (기본)
 * - 문제 정답 + 에너지 보너스: 10 * (1 + energy/100) = 최대 20포인트
 * - 게임 완료 보너스: 50포인트
 * - 일일 퀘스트 완료 (3종 각 1회): 300포인트
 * - 주간 퀘스트 완료 (21회): 1000포인트
 *
 * [미니게임 비용]
 * - 30분 플레이: 600포인트
 * - 1시간 플레이: 1000포인트
 *
 * 하루에 수학/영어/한자 각 1회씩 완료하면:
 * - 정답 보너스: ~150 * 3 = ~450포인트 (에너지 0일 때)
 * - 게임 완료 보너스: 50 * 3 = 150포인트
 * - 일일 퀘스트: 300포인트
 * - 합계: ~900포인트 → 약 45분~1시간 미니게임 가능
 *
 * [에너지 시스템]
 * - 기본 에너지: 0 (첫날은 보너스 없음)
 * - 연속 출석 1일당 에너지 +5 (최대 100)
 * - 에너지가 높을수록 포인트 획득량 증가 (최대 2배)
 * - 연속 출석 끊기면 에너지 0으로 리셋
 *
 * [레벨 시스템]
 * - 레벨업 필요 경험치: level * 100
 * - 게임 완료 시 경험치: 50 + (정답 수 * 10)
 */
class RewardRepository(private val dao: UserStatsDao) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 기본 상태
    private val _userStats = MutableStateFlow<UserStatsEntity?>(null)
    val userStats: StateFlow<UserStatsEntity?> = _userStats.asStateFlow()

    // 편의용 StateFlow들
    private val _totalPoints = MutableStateFlow(0)
    val totalPoints: StateFlow<Int> = _totalPoints.asStateFlow()

    private val _mathDailyCount = MutableStateFlow(0)
    val mathDailyCount: StateFlow<Int> = _mathDailyCount.asStateFlow()

    private val _vocabDailyCount = MutableStateFlow(0)
    val vocabDailyCount: StateFlow<Int> = _vocabDailyCount.asStateFlow()

    private val _hanjaDailyCount = MutableStateFlow(0)
    val hanjaDailyCount: StateFlow<Int> = _hanjaDailyCount.asStateFlow()

    private val _weeklyTotalCount = MutableStateFlow(0)
    val weeklyTotalCount: StateFlow<Int> = _weeklyTotalCount.asStateFlow()

    private val _isDailyRewardClaimed = MutableStateFlow(false)
    val isDailyRewardClaimed: StateFlow<Boolean> = _isDailyRewardClaimed.asStateFlow()

    private val _isWeeklyRewardClaimed = MutableStateFlow(false)
    val isWeeklyRewardClaimed: StateFlow<Boolean> = _isWeeklyRewardClaimed.asStateFlow()

    // 새로운 StateFlow들
    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _energy = MutableStateFlow(0)
    val energy: StateFlow<Int> = _energy.asStateFlow()

    private val _maxEnergy = MutableStateFlow(100)
    val maxEnergy: StateFlow<Int> = _maxEnergy.asStateFlow()

    private val _level = MutableStateFlow(1)
    val level: StateFlow<Int> = _level.asStateFlow()

    private val _experience = MutableStateFlow(0)
    val experience: StateFlow<Int> = _experience.asStateFlow()

    private var cachedStats: UserStatsEntity? = null

    init {
        coroutineScope.launch {
            try {
                loadStats()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun loadStats() {
        var stats = dao.getUserStats()
        if (stats != null) {
            // 테스트용: 기존 유저도 포인트가 100 미만이면 10000으로 충전
            if (stats.totalPoints < 100) {
                stats = stats.copy(totalPoints = 10000)
                dao.insertOrUpdate(stats)
            }
            cachedStats = stats
            syncStateFromEntity(stats)
            checkAndResetDates()
        } else {
            // 새 유저 생성 (테스트용 포인트 10000 지급)
            val newStats = UserStatsEntity(
                id = 1,
                totalPoints = 10000,  // 테스트용 초기 포인트
                lastDate = getCurrentDateString(),
                lastWeek = getWeeklyString(),
                lastStreakDate = "",
                createdAt = Clock.System.now().toEpochMilliseconds()
            )
            dao.insertOrUpdate(newStats)
            cachedStats = newStats
            syncStateFromEntity(newStats)
        }
        _userStats.value = cachedStats
    }

    private fun syncStateFromEntity(stats: UserStatsEntity) {
        _totalPoints.value = stats.totalPoints
        _mathDailyCount.value = stats.mathDailyCount
        _vocabDailyCount.value = stats.vocabDailyCount
        _hanjaDailyCount.value = stats.hanjaDailyCount
        _weeklyTotalCount.value = stats.weeklyTotalCount
        _isDailyRewardClaimed.value = stats.isDailyClaimed
        _isWeeklyRewardClaimed.value = stats.isWeeklyClaimed
        _currentStreak.value = stats.currentStreak
        _energy.value = stats.energy
        _maxEnergy.value = stats.maxEnergy
        _level.value = stats.level
        _experience.value = stats.experience
    }

    private fun getCurrentDateString(): String {
        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${localDateTime.year}-${localDateTime.monthNumber.toString().padStart(2, '0')}-${localDateTime.dayOfMonth.toString().padStart(2, '0')}"
    }

    private fun getWeeklyString(): String {
        val now = Clock.System.now()
        val daysSinceEpoch = now.toEpochMilliseconds() / (1000 * 60 * 60 * 24)
        val weekSinceEpoch = (daysSinceEpoch + 4) / 7
        return "W$weekSinceEpoch"
    }

    private fun getYesterday(): String {
        val now = Clock.System.now()
        val yesterday = kotlinx.datetime.Instant.fromEpochMilliseconds(
            now.toEpochMilliseconds() - (24 * 60 * 60 * 1000)
        )
        val localDateTime = yesterday.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${localDateTime.year}-${localDateTime.monthNumber.toString().padStart(2, '0')}-${localDateTime.dayOfMonth.toString().padStart(2, '0')}"
    }

    private fun checkAndResetDates() {
        val today = getCurrentDateString()
        val thisWeek = getWeeklyString()
        val stats = cachedStats ?: return

        var needsUpdate = false

        // 날짜가 바뀌면 일일 카운트 리셋
        if (stats.lastDate != today && stats.lastDate.isNotEmpty()) {
            _mathDailyCount.value = 0
            _vocabDailyCount.value = 0
            _hanjaDailyCount.value = 0
            _isDailyRewardClaimed.value = false
            needsUpdate = true

            // 연속 출석 체크
            val yesterday = getYesterday()
            if (stats.lastStreakDate == yesterday) {
                // 어제 출석했으면 streak 유지 (오늘 게임하면 증가)
            } else if (stats.lastStreakDate != today) {
                // 어제 출석 안했으면 streak 리셋
                _currentStreak.value = 0
                // 에너지도 0으로 리셋
                _energy.value = 0
            }
        }

        // 주가 바뀌면 주간 카운트 리셋
        if (stats.lastWeek != thisWeek && stats.lastWeek.isNotEmpty()) {
            _weeklyTotalCount.value = 0
            _isWeeklyRewardClaimed.value = false
            needsUpdate = true
        }

        if (needsUpdate) {
            cachedStats = stats.copy(
                lastDate = today,
                lastWeek = thisWeek,
                mathDailyCount = _mathDailyCount.value,
                vocabDailyCount = _vocabDailyCount.value,
                hanjaDailyCount = _hanjaDailyCount.value,
                weeklyTotalCount = _weeklyTotalCount.value,
                isDailyClaimed = _isDailyRewardClaimed.value,
                isWeeklyClaimed = _isWeeklyRewardClaimed.value,
                currentStreak = _currentStreak.value,
                energy = _energy.value
            )
            updateStatsInDB()
        }
    }

    private fun updateStatsInDB() {
        coroutineScope.launch {
            try {
                cachedStats?.let { dao.insertOrUpdate(it) }
                _userStats.value = cachedStats
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 포인트 추가 (에너지 보너스 적용)
     */
    fun addPoints(basePoints: Int) {
        checkAndResetDates()

        // 에너지 보너스: 에너지 0 = 1배, 에너지 100 = 2배
        val energyBonus = 1.0 + (_energy.value.toDouble() / 100.0)
        val bonusPoints = (basePoints * energyBonus).toInt()

        _totalPoints.update { it + bonusPoints }

        cachedStats = cachedStats?.copy(
            totalPoints = _totalPoints.value
        )
        updateStatsInDB()
    }

    /**
     * 포인트 사용 (미니게임 플레이)
     */
    fun usePoints(points: Int): Boolean {
        if (_totalPoints.value < points) return false

        _totalPoints.update { it - points }
        cachedStats = cachedStats?.copy(totalPoints = _totalPoints.value)
        updateStatsInDB()
        return true
    }

    /**
     * 게임 완료 기록 (경험치, 연속출석 처리 포함)
     */
    fun recordGamePlayed(type: GameType, correctAnswers: Int = 5) {
        checkAndResetDates()
        val today = getCurrentDateString()
        val stats = cachedStats ?: return

        // 일일 카운트 증가
        when (type) {
            GameType.MATH -> _mathDailyCount.update { it + 1 }
            GameType.VOCAB -> _vocabDailyCount.update { it + 1 }
            GameType.HANJA -> _hanjaDailyCount.update { it + 1 }
        }
        _weeklyTotalCount.update { it + 1 }

        // 게임 완료 보너스 포인트 (에너지 보너스 미적용)
        _totalPoints.update { it + 50 }

        // 경험치 추가
        val expGained = 50 + (correctAnswers * 10)
        addExperience(expGained)

        // 연속 출석 처리 (오늘 처음 게임하는 경우)
        if (stats.lastStreakDate != today) {
            val yesterday = getYesterday()
            if (stats.lastStreakDate == yesterday || stats.lastStreakDate.isEmpty()) {
                // 어제 출석했거나 첫 출석이면 streak 증가
                _currentStreak.update { it + 1 }
            } else {
                // streak이 끊겼으면 1부터 시작
                _currentStreak.value = 1
            }

            // 연속 출석에 따른 에너지 증가 (1일당 +5, 최대 100)
            val newEnergy = min(_currentStreak.value * 5, 100)
            _energy.value = newEnergy

            cachedStats = stats.copy(
                lastStreakDate = today,
                currentStreak = _currentStreak.value,
                longestStreak = maxOf(stats.longestStreak, _currentStreak.value),
                energy = _energy.value
            )
        }

        // 통계 업데이트
        cachedStats = cachedStats?.copy(
            totalPoints = _totalPoints.value,
            mathDailyCount = _mathDailyCount.value,
            vocabDailyCount = _vocabDailyCount.value,
            hanjaDailyCount = _hanjaDailyCount.value,
            weeklyTotalCount = _weeklyTotalCount.value,
            lastDate = today,
            totalGamesPlayed = (cachedStats?.totalGamesPlayed ?: 0) + 1,
            totalCorrectAnswers = (cachedStats?.totalCorrectAnswers ?: 0) + correctAnswers,
            level = _level.value,
            experience = _experience.value,
            totalExperience = cachedStats?.totalExperience?.plus(expGained) ?: expGained
        )
        updateStatsInDB()
    }

    /**
     * 경험치 추가 및 레벨업 처리
     */
    private fun addExperience(exp: Int) {
        var newExp = _experience.value + exp
        var newLevel = _level.value

        // 레벨업 체크 (필요 경험치: level * 100)
        while (newExp >= getRequiredExp(newLevel)) {
            newExp -= getRequiredExp(newLevel)
            newLevel++
        }

        _experience.value = newExp
        _level.value = newLevel
    }

    /**
     * 레벨업에 필요한 경험치
     */
    fun getRequiredExp(level: Int): Int = level * 100

    /**
     * 현재 레벨업까지 남은 경험치
     */
    fun getExpToNextLevel(): Int = getRequiredExp(_level.value) - _experience.value

    /**
     * 현재 레벨 진행률 (0.0 ~ 1.0)
     */
    fun getLevelProgress(): Float {
        val required = getRequiredExp(_level.value)
        return _experience.value.toFloat() / required.toFloat()
    }

    /**
     * 일일 퀘스트 완료 체크 (수학, 영어, 한자 각 1회 이상)
     */
    fun isDailyQuestComplete(): Boolean {
        return _mathDailyCount.value >= 1 &&
               _vocabDailyCount.value >= 1 &&
               _hanjaDailyCount.value >= 1
    }

    /**
     * 일일 보상 수령
     */
    fun claimDailyReward(): Boolean {
        checkAndResetDates()
        if (!isDailyQuestComplete() || _isDailyRewardClaimed.value) return false

        _isDailyRewardClaimed.value = true
        _totalPoints.update { it + 300 }

        cachedStats = cachedStats?.copy(
            isDailyClaimed = true,
            totalPoints = _totalPoints.value
        )
        updateStatsInDB()
        return true
    }

    /**
     * 주간 퀘스트 완료 체크 (일주일 동안 21회 = 하루 3회)
     */
    fun isWeeklyQuestComplete(): Boolean {
        return _weeklyTotalCount.value >= 21
    }

    /**
     * 주간 보상 수령
     */
    fun claimWeeklyReward(): Boolean {
        checkAndResetDates()
        if (!isWeeklyQuestComplete() || _isWeeklyRewardClaimed.value) return false

        _isWeeklyRewardClaimed.value = true
        _totalPoints.update { it + 1000 }

        cachedStats = cachedStats?.copy(
            isWeeklyClaimed = true,
            totalPoints = _totalPoints.value
        )
        updateStatsInDB()
        return true
    }

    /**
     * 레벨 타이틀 반환
     */
    fun getLevelTitle(): String {
        return when (_level.value) {
            in 1..4 -> "초보 학습자"
            in 5..9 -> "열심 학생"
            in 10..14 -> "학자"
            in 15..19 -> "박사"
            in 20..29 -> "석학"
            in 30..49 -> "대학자"
            else -> "전설의 학자"
        }
    }

    /**
     * 미니게임 플레이 가능 시간 계산 (분)
     */
    fun getAvailablePlayTime(): Int {
        // 600포인트 = 30분, 1000포인트 = 60분
        // 1분당 약 16.67포인트
        return (_totalPoints.value / 17).coerceAtMost(120) // 최대 2시간
    }

    /**
     * 테스트용: 포인트 직접 설정
     */
    fun setPointsForTesting(points: Int) {
        _totalPoints.value = points
        cachedStats = cachedStats?.copy(totalPoints = points)
        updateStatsInDB()
    }
}
