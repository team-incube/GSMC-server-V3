package com.team.incube.gsmc.v3.domain.score.calculator.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.score.calculator.CategoryScoreCalculator
import com.team.incube.gsmc.v3.domain.score.dto.Score
import kotlin.math.roundToInt

/**
 * TOPCIT 점수 계산기
 *
 * TOPCIT 점수는 Score.scoreValue에 저장됩니다 (1-1000).
 * 점수 변환: round(scoreValue / 100), 최대 10점
 *
 * maxRecordCount=1이므로 레코드는 1개만 존재합니다.
 */
class TopcitScoreCalculator : CategoryScoreCalculator() {
    override fun calculate(
        scores: List<Score>,
        categoryType: CategoryType,
        includeApprovedOnly: Boolean,
    ): Int {
        val targetScore =
            scores.firstOrNull { score ->
                score.categoryType == categoryType && score.isValidStatus(includeApprovedOnly)
            }

        val scoreValue = targetScore?.scoreValue ?: return 0

        val convertedScore = (scoreValue / 100.0).roundToInt()

        return convertedScore.coerceIn(0, 10)
    }
}
