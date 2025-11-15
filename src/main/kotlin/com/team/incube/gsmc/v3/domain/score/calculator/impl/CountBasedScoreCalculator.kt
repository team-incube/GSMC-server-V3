package com.team.incube.gsmc.v3.domain.score.calculator.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.calculator.CategoryScoreCalculator
import com.team.incube.gsmc.v3.domain.score.dto.Score

/**
 * 카운트 기반 점수 계산기
 *
 * 레코드 개수에 weight를 곱하여 점수를 계산합니다.
 * 사용 카테고리:
 * - CERTIFICATE: weight=2, maxRecordCount=7 → 최대 14점
 * - TOEIC_ACADEMY: weight=1, maxRecordCount=1 → 최대 1점 (참여 시)
 *
 * weight가 null인 경우 0점 반환.
 */
class CountBasedScoreCalculator : CategoryScoreCalculator() {
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

        val count = targetScores.size
        val weight = categoryType.weight ?: return 0

        return count * weight
    }
}
