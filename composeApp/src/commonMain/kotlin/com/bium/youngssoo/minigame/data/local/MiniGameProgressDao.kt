package com.bium.youngssoo.minigame.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MiniGameProgressDao {

    @Query("SELECT * FROM mini_game_progress WHERE gameId = :gameId")
    suspend fun getProgress(gameId: String): MiniGameProgressEntity?

    @Query("SELECT * FROM mini_game_progress WHERE gameId = :gameId")
    fun observeProgress(gameId: String): Flow<MiniGameProgressEntity?>

    @Query("SELECT * FROM mini_game_progress")
    fun observeAllProgress(): Flow<List<MiniGameProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: MiniGameProgressEntity)

    @Query("UPDATE mini_game_progress SET currentStage = :stage WHERE gameId = :gameId")
    suspend fun updateStage(gameId: String, stage: Int)

    @Query("UPDATE mini_game_progress SET highScore = :score WHERE gameId = :gameId AND highScore < :score")
    suspend fun updateHighScoreIfBetter(gameId: String, score: Int)

    @Query("UPDATE mini_game_progress SET customData = :data WHERE gameId = :gameId")
    suspend fun updateCustomData(gameId: String, data: String)

    @Query("UPDATE mini_game_progress SET totalPlayCount = totalPlayCount + 1, lastPlayedAt = :timestamp WHERE gameId = :gameId")
    suspend fun incrementPlayCount(gameId: String, timestamp: Long)

    @Query("UPDATE mini_game_progress SET totalPlayTime = totalPlayTime + :duration WHERE gameId = :gameId")
    suspend fun addPlayTime(gameId: String, duration: Long)
}
