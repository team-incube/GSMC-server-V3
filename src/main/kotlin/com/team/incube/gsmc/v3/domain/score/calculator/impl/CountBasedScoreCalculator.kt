package com.team.incube.gsmc.v3.domain.score.calculator.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.calculator.CategoryScoreCalculator
import com.team.incube.gsmc.v3.domain.score.dto.Score

class CountBasedScoreCalculator : CategoryScoreCalculator() {
    override fun calculate(
        scores: List<Score>,
        categoryType: CategoryType,
        includeApprovedOnly: Boolean,
    ): Int {
        val targetScores =
            scores
                .filter { it.categoryType == categoryType }
                .filter { score ->
                    if (includeApprovedOnly) {
                        score.status == ScoreStatus.APPROVED
                    } else {
                        score.status == ScoreStatus.APPROVED || score.status == ScoreStatus.PENDING
                    }
                }

        val count = targetScores.size

        return if (categoryType.isAccumulated) {
            count * categoryType.weight!!
        } else {
            if (count > 0) categoryType.weight else 0
        }!!
    }
}
