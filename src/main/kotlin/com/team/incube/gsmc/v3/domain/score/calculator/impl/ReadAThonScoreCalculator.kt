package com.team.incube.gsmc.v3.domain.score.calculator.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.calculator.CategoryScoreCalculator
import com.team.incube.gsmc.v3.domain.score.dto.Score

/**
 * 빛고을독서마라톤 점수 계산기
 *
 * 단계는 Score.scoreValue에 정수(1-7)로 저장됩니다.
 * 단계 값이 그대로 점수로 사용됩니다.
 * - 1 (거북이코스): 1점
 * - 2 (악어코스): 2점
 * - 3 (토끼코스): 3점
 * - 4 (타조코스): 4점
 * - 5 (사자코스): 5점
 * - 6 (호랑이코스): 6점
 * - 7 (월계관코스): 7점
 *
 * maxRecordCount가 1이므로 레코드는 1개만 존재하며, 해당 단계의 점수를 반환합니다.
 */
class ReadAThonScoreCalculator : CategoryScoreCalculator() {
    override fun calculate(
        scores: List<Score>,
        categoryType: CategoryType,
        includeApprovedOnly: Boolean,
    ): Int {
        val targetScore =
            scores.firstOrNull { score ->
                score.categoryType == categoryType && score.isValidStatus(includeApprovedOnly)
            }

        return targetScore?.scoreValue?.toInt() ?: 0
    }
}
