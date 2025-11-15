package com.team.incube.gsmc.v3.domain.score.calculator.impl

import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.calculator.CategoryScoreCalculator
import com.team.incube.gsmc.v3.domain.score.dto.Score
import kotlin.math.min
import kotlin.math.round

class TopcitScoreCalculator : CategoryScoreCalculator() {
    override fun calculate(scores: List<Score>): Int {
        val approvedScores = scores.filter { it.status == ScoreStatus.APPROVED }
        if (approvedScores.isEmpty()) return 0

        val maxScoreValue = approvedScores.mapNotNull { it.scoreValue }.maxOrNull() ?: return 0

        val convertedScore = round(maxScoreValue / 100.0).toInt()

        return min(convertedScore, 10)
    }
}