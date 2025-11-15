package com.team.incube.gsmc.v3.domain.score.calculator.impl

import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.calculator.CategoryScoreCalculator
import com.team.incube.gsmc.v3.domain.score.dto.Score
import kotlin.math.min

class CountBasedScoreCalculator : CategoryScoreCalculator() {
    override fun calculate(
        scores: List<Score>,
        includeApprovedOnly: Boolean,
    ): Int {
        if (scores.isEmpty()) return 0

        val categoryType = scores.first().categoryType

        val targetScores =
            if (includeApprovedOnly) {
                scores.filter { it.status == ScoreStatus.APPROVED }
            } else {
                scores.filter { it.status == ScoreStatus.APPROVED || it.status == ScoreStatus.PENDING }
            }

        val count = targetScores.size

        return if (categoryType.isAccumulated) {
            min(count * categoryType.weight, categoryType.maximumValue)
        } else {
            if (count > 0) categoryType.weight else 0
        }
    }
}
