package com.team.incube.gsmc.v3.domain.score.calculator

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus

abstract class CategoryScoreCalculator {
    abstract fun calculate(
        scores: List<Score>,
        categoryType: CategoryType,
        includeApprovedOnly: Boolean,
    ): Int

    protected fun Score.isValidStatus(includeApprovedOnly: Boolean): Boolean =
        if (includeApprovedOnly) {
            status == ScoreStatus.APPROVED
        } else {
            status != ScoreStatus.REJECTED
        }
}
