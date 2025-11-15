package com.team.incube.gsmc.v3.domain.score.calculator.impl

import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.calculator.CategoryScoreCalculator
import com.team.incube.gsmc.v3.domain.score.dto.Score
import kotlin.math.min
import kotlin.math.round

class TopcitScoreCalculator : CategoryScoreCalculator() {
    override fun calculate(
        scores: List<Score>,
        includeApprovedOnly: Boolean,
    ): Int {
        val targetScores =
            if (includeApprovedOnly) {
                scores.filter { it.status == ScoreStatus.APPROVED }
            } else {
                scores.filter { it.status == ScoreStatus.APPROVED || it.status == ScoreStatus.PENDING }
            }

        if (targetScores.isEmpty()) return 0

        val maxScoreValue = targetScores.mapNotNull { it.scoreValue }.maxOrNull() ?: return 0

        val convertedScore = round(maxScoreValue / 100.0).toInt()

        return min(convertedScore, 10)
    }
}