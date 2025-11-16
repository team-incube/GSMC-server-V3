package com.team.incube.gsmc.v3.domain.score.calculator.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.score.calculator.CategoryScoreCalculator
import com.team.incube.gsmc.v3.domain.score.dto.Score
import kotlin.math.min

/**
 * 봉사활동 점수 계산기
 *
 * 봉사 시간은 Score.scoreValue에 저장됩니다 (시간 단위).
 * 1시간당 1점, 최대 10점
 *
 * maxRecordCount=1이므로 레코드는 1개만 존재합니다.
 */
class VolunteerScoreCalculator : CategoryScoreCalculator() {
    override fun calculate(
        scores: List<Score>,
        categoryType: CategoryType,
        includeApprovedOnly: Boolean,
    ): Int {
        val targetScore =
            scores.firstOrNull { score ->
                score.categoryType == categoryType && score.isValidStatus(includeApprovedOnly)
            }

        val hours = targetScore?.scoreValue?.toInt() ?: 0

        return min(hours, 10)
    }
}
