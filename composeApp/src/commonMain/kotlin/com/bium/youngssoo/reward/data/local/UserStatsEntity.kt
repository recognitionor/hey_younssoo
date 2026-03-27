package com.bium.youngssoo.reward.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey
    val id: Int = 1,

    // 포인트 (미니게임에서 사용하는 재화)
    val totalPoints: Int = 0,

    // 오늘 완료한 게임 횟수
    val mathDailyCount: Int = 0,
    val vocabDailyCount: Int = 0,
    val hanjaDailyCount: Int = 0,

    // 주간 통계
    val weeklyTotalCount: Int = 0,
    val isDailyClaimed: Boolean = false,
    val isWeeklyClaimed: Boolean = false,

    // 날짜 추적
    val lastDate: String = "",        // 마지막 활동 날짜 (yyyy-MM-dd)
    val lastWeek: String = "",        // 마지막 주 (yyyy-ww)

    // 연속 출석 (Streak)
    val currentStreak: Int = 0,       // 현재 연속 출석 일수
    val longestStreak: Int = 0,       // 최장 연속 출석 기록
    val lastStreakDate: String = "",  // 마지막 출석 날짜

    // 에너지 시스템 (연속 출석에 따라 증가, 최대 100)
    val energy: Int = 0,              // 현재 에너지 (0부터 시작)
    val maxEnergy: Int = 100,         // 최대 에너지

    // 레벨 시스템
    val level: Int = 1,               // 현재 레벨
    val experience: Int = 0,          // 현재 경험치
    val totalExperience: Int = 0,     // 총 누적 경험치

    // 통계
    val totalGamesPlayed: Int = 0,    // 총 게임 플레이 횟수
    val totalCorrectAnswers: Int = 0, // 총 정답 수
    val createdAt: Long = 0L          // 계정 생성일
)
