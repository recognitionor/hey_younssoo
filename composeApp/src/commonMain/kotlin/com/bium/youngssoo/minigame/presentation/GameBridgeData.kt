package com.bium.youngssoo.minigame.presentation

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 앱에서 게임으로 전달하는 초기화 데이터
 */
@Serializable
data class GameInitData(
    val stage: Int = 1,
    val highScore: Int = 0,
    val customData: String = "{}"
) {
    fun toJson(): String = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String): GameInitData = Json.decodeFromString(json)
    }
}

/**
 * 게임에서 앱으로 전달하는 완료 데이터
 */
@Serializable
data class GameCompleteData(
    val score: Int = 0,
    val cleared: Boolean = false,
    val nextStage: Int? = null,
    val customData: String? = null,
    val reason: String? = null
) {
    fun toJson(): String = Json.encodeToString(this)

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromJson(jsonStr: String): GameCompleteData {
            return try {
                json.decodeFromString(jsonStr)
            } catch (e: Exception) {
                GameCompleteData(score = 0, cleared = false)
            }
        }
    }
}
