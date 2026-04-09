package com.bium.youngssoo.minigame.data.model

import kotlinx.serialization.Serializable

/**
 * 미니게임 정보
 */
@Serializable
data class MiniGame(
    val id: String,
    val name: String,
    val description: String,
    val thumbnailUrl: String,
    val gameUrl: String,           // 게임 HTML URL
    val costType: CostType,        // 비용 타입
    val costAmount: Int,           // 비용 (포인트)
    val playValue: Int,            // 플레이 가능 시간(초) 또는 판수
    val unlockPrice: Int = 0,      // 영구 해금 가격 (0이면 해금 불필요)
    val version: Int = 1           // 게임 HTML 버전
)

enum class CostType {
    TIME,   // 시간 기반 (초 단위)
    PLAYS   // 판수 기반
}

/**
 * 게임 결과
 */
@Serializable
data class GameResult(
    val gameId: String,
    val score: Int,
    val playTime: Long,     // 플레이 시간 (밀리초)
    val isCompleted: Boolean
)
