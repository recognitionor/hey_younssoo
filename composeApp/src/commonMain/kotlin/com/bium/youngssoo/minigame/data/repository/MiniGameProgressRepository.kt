package com.bium.youngssoo.minigame.data.repository

import com.bium.youngssoo.minigame.data.local.MiniGameProgressDao
import com.bium.youngssoo.minigame.data.local.MiniGameProgressEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import com.bium.youngssoo.currentTimeMillis

class MiniGameProgressRepository(
    private val dao: MiniGameProgressDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun observeProgress(gameId: String): Flow<MiniGameProgressEntity?> {
        return dao.observeProgress(gameId)
    }

    fun observeAllProgress(): Flow<List<MiniGameProgressEntity>> {
        return dao.observeAllProgress()
    }

    suspend fun getProgress(gameId: String): MiniGameProgressEntity {
        return dao.getProgress(gameId) ?: MiniGameProgressEntity(gameId = gameId)
    }

    suspend fun getOrCreateProgress(gameId: String): MiniGameProgressEntity {
        val existing = dao.getProgress(gameId)
        if (existing != null) return existing

        val newProgress = MiniGameProgressEntity(gameId = gameId)
        dao.insertOrUpdate(newProgress)
        return newProgress
    }

    suspend fun updateStage(gameId: String, stage: Int) {
        ensureProgressExists(gameId)
        dao.updateStage(gameId, stage)
    }

    suspend fun advanceToNextStage(gameId: String): Int {
        val progress = getOrCreateProgress(gameId)
        val nextStage = progress.currentStage + 1
        dao.updateStage(gameId, nextStage)
        return nextStage
    }

    suspend fun updateHighScore(gameId: String, score: Int) {
        ensureProgressExists(gameId)
        dao.updateHighScoreIfBetter(gameId, score)
    }

    suspend fun updateCustomData(gameId: String, data: String) {
        ensureProgressExists(gameId)
        dao.updateCustomData(gameId, data)
    }

    suspend fun recordPlay(gameId: String, duration: Long) {
        ensureProgressExists(gameId)
        dao.incrementPlayCount(gameId, currentTimeMillis())
        dao.addPlayTime(gameId, duration)
    }

    suspend fun saveFullProgress(progress: MiniGameProgressEntity) {
        dao.insertOrUpdate(progress)
    }

    suspend fun unlockGame(gameId: String) {
        ensureProgressExists(gameId)
        dao.unlock(gameId, currentTimeMillis())
    }

    suspend fun isUnlocked(gameId: String): Boolean {
        return dao.isUnlocked(gameId) ?: false
    }

    suspend fun updatePlayedVersion(gameId: String, version: Int) {
        ensureProgressExists(gameId)
        dao.updatePlayedVersion(gameId, version)
    }

    private suspend fun ensureProgressExists(gameId: String) {
        if (dao.getProgress(gameId) == null) {
            dao.insertOrUpdate(MiniGameProgressEntity(gameId = gameId))
        }
    }
}

