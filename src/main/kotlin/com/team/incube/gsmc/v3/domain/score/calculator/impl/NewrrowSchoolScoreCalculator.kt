package com.team.incube.gsmc.v3.domain.score.calculator.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.score.calculator.CategoryScoreCalculator
import com.team.incube.gsmc.v3.domain.score.dto.Score
import kotlin.math.min

/**
 * 뉴로우스쿨 참여 점수 계산기
 *
 * 회고온도는 Score.scoreValue에 저장됩니다 (0-100도).
 * 점수 변환: temperature / 20, 최대 5점
 *
 * maxRecordCount=1이므로 레코드는 1개만 존재합니다.
 */
class NewrrowSchoolScoreCalculator : CategoryScoreCalculator() {
    override fun calculate(
        scores: List<Score>,
        categoryType: CategoryType,
        includeApprovedOnly: Boolean,
    ): Int {
        val targetScore =
            scores.firstOrNull { score ->
                score.categoryType == categoryType && score.isValidStatus(includeApprovedOnly)
            }

        val temperature = targetScore?.scoreValue ?: return 0

        val convertedScore = (temperature / 20.0).toInt()

        return min(convertedScore, 5)
    }
}
