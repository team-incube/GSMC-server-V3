package com.team.incube.gsmc.v3.domain.score.calculator.impl

import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.calculator.CategoryScoreCalculator
import com.team.incube.gsmc.v3.domain.score.dto.Score
import kotlin.math.min

class CountBasedScoreCalculator : CategoryScoreCalculator() {
    override fun calculate(scores: List<Score>): Int {
        if (scores.isEmpty()) return 0

        val categoryType = scores.first().categoryType
        val approvedCount = scores.count { it.status == ScoreStatus.APPROVED }

        return if (categoryType.isAccumulated) {
            min(approvedCount * categoryType.weight, categoryType.maximumValue)
        } else {
            if (approvedCount > 0) categoryType.weight else 0
        }
    }
}