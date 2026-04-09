package com.bium.youngssoo.minigame.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 미니게임 진행도 저장 엔티티
 */
@Entity(tableName = "mini_game_progress")
data class MiniGameProgressEntity(
    @PrimaryKey
    val gameId: String,              // 게임 ID (예: "puzzle-marble")
    val currentStage: Int = 1,       // 현재 스테이지
    val highScore: Int = 0,          // 최고 점수
    val totalPlayCount: Int = 0,     // 총 플레이 횟수
    val totalPlayTime: Long = 0,     // 총 플레이 시간 (밀리초)
    val lastPlayedAt: Long = 0,      // 마지막 플레이 시간
    val customData: String = "{}",   // 게임별 커스텀 데이터 (JSON)
    val isUnlocked: Boolean = false, // 영구 해금 여부
    val unlockedAt: Long = 0,        // 해금 시각 (타임스탐프)
    val playedVersion: Int = 0       // 마지막 플레이한 게임 버전 (0 = 아직 미플레이)
)
