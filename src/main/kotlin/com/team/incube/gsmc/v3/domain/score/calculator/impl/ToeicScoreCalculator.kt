package com.team.incube.gsmc.v3.domain.score.calculator.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.calculator.CategoryScoreCalculator
import com.team.incube.gsmc.v3.domain.score.dto.Score
import kotlin.math.min
import kotlin.math.round

class ToeicScoreCalculator : CategoryScoreCalculator() {
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

        val maxToeicScore =
            targetScores
                .filter { it.categoryType == CategoryType.TOEIC }
                .mapNotNull { it.scoreValue }
                .maxOrNull() ?: 0

        val convertedScore = round(maxToeicScore / 100.0).toInt()

        val hasToeicAcademy =
            targetScores.any { it.categoryType == CategoryType.TOEIC_ACADEMY }

        val bonusScore = if (hasToeicAcademy) 1 else 0

        return min(convertedScore + bonusScore, 10)
    }
}
