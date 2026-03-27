package com.bium.youngssoo.game.hanja.domain.model

data class HanjaWord(
    val id: String,
    val hanja: String,      // 한자
    val meaning: String     // 한글 뜻
)

data class HanjaProblem(
    val targetWord: HanjaWord,
    val options: List<String>  // 정답 1개 + 오답들
)
