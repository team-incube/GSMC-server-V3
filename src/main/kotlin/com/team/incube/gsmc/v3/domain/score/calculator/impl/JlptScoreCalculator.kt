package com.team.incube.gsmc.v3.domain.score.calculator.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.calculator.CategoryScoreCalculator
import com.team.incube.gsmc.v3.domain.score.dto.Score
import kotlin.math.min

/**
 * JLPT 등급은 Score.activityName에 저장됩니다 (N1, N2, N3, N4, N5).
 * 등급별 점수 변환:
 * - N1: 10점
 * - N2: 8점
 * - N3: 6점
 * - N4: 4점
 * - N5: 2점
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
                .maxOfOrNull { convertGradeToScore(it.activityName) } ?: 0

        val hasToeicAcademy =
            targetScores.any { it.categoryType == CategoryType.TOEIC_ACADEMY }

        val bonusScore = if (hasToeicAcademy) 1 else 0

        return min(maxJlptScore + bonusScore, 10)
    }

    private fun convertGradeToScore(grade: String?): Int {
        if (grade == null) return 0

        return when (grade.uppercase().trim()) {
            "N1" -> 10
            "N2" -> 8
            "N3" -> 6
            "N4" -> 4
            "N5" -> 2
            else -> 0
        }
    }
}
