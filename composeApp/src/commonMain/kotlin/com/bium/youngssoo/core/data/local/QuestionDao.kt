package com.bium.youngssoo.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuestionDao {

    @Query("SELECT * FROM questions WHERE category = :category")
    suspend fun getQuestionsByCategory(category: QuestionCategory): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE category = :category AND difficulty = :difficulty")
    suspend fun getQuestionsByCategoryAndDifficulty(
        category: QuestionCategory,
        difficulty: Int
    ): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getQuestionById(id: String): QuestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity)

    @Query("DELETE FROM questions WHERE category = :category")
    suspend fun deleteQuestionsByCategory(category: QuestionCategory)

    @Query("SELECT COUNT(*) FROM questions WHERE category = :category")
    suspend fun getQuestionCount(category: QuestionCategory): Int

    @Query("SELECT MAX(updatedAt) FROM questions WHERE category = :category")
    suspend fun getLastUpdatedTime(category: QuestionCategory): Long?
}
