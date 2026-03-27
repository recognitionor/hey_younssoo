package com.bium.youngssoo.game.math.domain.model

enum class MathOperator(val symbol: String) {
    MULTIPLY("×"), DIVIDE("÷")
}

data class MathProblem(
    val factor1: Int,
    val factor2: Int,
    val operator: MathOperator = MathOperator.MULTIPLY,
    val correctAnswer: Int = if (operator == MathOperator.MULTIPLY) factor1 * factor2 else factor1 / factor2
)

enum class RewardTier(val points: Int, val description: String) {
    HUGE(100, "대성공! 1초 이내 정답"),
    MEDIUM(50, "성공! 2초 이내 정답"),
    SMALL(20, "보통! 3초 이내 정답"),
    MINIMAL(1, "지연! 정답이긴 함"),
    NONE(0, "시간 초과 또는 오답")
}

data class MathResult(
    val problem: MathProblem,
    val isCorrect: Boolean,
    val timeTakenMillis: Long,
    val rewardTier: RewardTier
)
