package com.team.incube.gsmc.v3.domain.score.calculator.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.calculator.CategoryScoreCalculator
import com.team.incube.gsmc.v3.domain.score.dto.Score
import kotlin.math.min

/**
 * JLPT 등급은 Score.scoreValue에 정수(1-5)로 저장됩니다.
 * 등급별 점수 변환:
 * - 1 (N1): 10점
 * - 2 (N2): 8점
 * - 3 (N3): 6점
 * - 4 (N4): 4점
 * - 5 (N5): 2점
 *
 * 토익사관학교 참여 시 +1점 보너스가 추가되며, 최대 점수는 10점입니다.
 */
class JlptScoreCalculator : CategoryScoreCalculator() {
    override fun calculate(
        scores: List<Score>,
        categoryType: CategoryType,
        includeApprovedOnly: Boolean,
    ): Int {
        val targetScores =
            scores.filter { score ->
                if (includeApprovedOnly) {
                    score.status == ScoreStatus.APPROVED
                } else {
                    score.status == ScoreStatus.APPROVED || score.status == ScoreStatus.PENDING
                }
            }

        if (targetScores.isEmpty()) return 0

        val maxJlptScore =
            targetScores
                .filter { it.categoryType == CategoryType.JLPT }
                .maxOfOrNull { convertGradeToScore(it.scoreValue) } ?: 0

        val hasToeicAcademy =
            targetScores.any { it.categoryType == CategoryType.TOEIC_ACADEMY }

        val bonusScore = if (hasToeicAcademy) 1 else 0

        return min(maxJlptScore + bonusScore, 10)
    }

    private fun convertGradeToScore(grade: Int?): Int {
        if (grade == null) return 0

        return when (grade) {
            1 -> 10 // N1

            2 -> 8 // N2

            3 -> 6 // N3

            4 -> 4 // N4

            5 -> 2 // N5

            else -> 0
        }
    }
}
