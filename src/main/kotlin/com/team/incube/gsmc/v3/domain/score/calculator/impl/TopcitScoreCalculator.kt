package com.team.incube.gsmc.v3.domain.score.calculator.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.calculator.CategoryScoreCalculator
import com.team.incube.gsmc.v3.domain.score.dto.Score
import kotlin.math.min
import kotlin.math.round

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

        if (targetScores.isEmpty()) return 0

        val maxScoreValue = targetScores.mapNotNull { it.scoreValue }.maxOrNull() ?: return 0

        val convertedScore = round(maxScoreValue / 100.0).toInt()

        return min(convertedScore, 10)
    }
}
