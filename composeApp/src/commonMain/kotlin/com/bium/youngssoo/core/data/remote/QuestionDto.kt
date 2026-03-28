package com.bium.youngssoo.core.data.remote

import kotlinx.serialization.Serializable

/**
 * Firebase Firestore에서 가져오는 문제 DTO
 */
@Serializable
data class QuestionDto(
    val id: String = "",
    val category: String = "",      // "VOCAB" 또는 "HANJA"
    val question: String = "",      // 문제 (영어 단어 또는 한자)
    val answer: String = "",        // 정답 (한글 뜻)
    val options: List<String> = emptyList(),  // 선택지 리스트
    val grade: String = "",         // 한자 급수
    val difficulty: Int = 1,        // 난이도
    val updatedAt: Long = 0         // 마지막 업데이트 시간
)
