package com.bium.youngssoo.game.vocab.domain.model

enum class QuestionType {
    ENG_TO_KOR, KOR_TO_ENG
}

data class VocabWord(
    val id: String,
    val english: String,
    val korean: String
)

data class VocabProblem(
    val targetType: QuestionType,
    val targetWord: VocabWord,
    val options: List<String> // 정답 1개 + 랜덤 오답 4개 (총 5개)
)
