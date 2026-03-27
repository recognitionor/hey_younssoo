package com.bium.youngssoo.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 문제 유형
 * VOCAB: 영어 단어
 * HANJA: 한자
 */
enum class QuestionCategory {
    VOCAB,
    HANJA
}

/**
 * 문제 데이터 Entity
 * Firebase에서 가져온 문제를 로컬 DB에 캐싱
 */
@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey
    val id: String,
    val category: QuestionCategory,
    val question: String,      // 문제 (영어 단어 또는 한자)
    val answer: String,        // 정답 (한글 뜻)
    val options: String,       // JSON 형태의 선택지 리스트
    val difficulty: Int = 1,   // 난이도 (1: 쉬움, 2: 보통, 3: 어려움)
    val updatedAt: Long = 0    // 마지막 업데이트 시간
)
