package com.team.incube.gsmc.v3.domain.score.calculator.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.calculator.CategoryScoreCalculator
import com.team.incube.gsmc.v3.domain.score.dto.Score
import kotlin.math.min
import kotlin.math.round

/**
 * 직업기초능력평가(NCS) 점수 계산기
 *
 * 평균 점수는 Score.scoreValue에 저장됩니다 (예: 4.6, 3.2).
 * 점수 변환: round(scoreValue), 최대 5점
 * 예시:
 * - 4.6 → 5점
 * - 3.2 → 3점
 * - 4.4 → 4점
 *
 * maxRecordCount=1이므로 레코드는 1개만 존재합니다.
 */
class NcsScoreCalculator : CategoryScoreCalculator() {
    override fun calculate(
        scores: List<Score>,
        categoryType: CategoryType,
        includeApprovedOnly: Boolean,
    ): Int {
        val targetScore =
            scores.firstOrNull { score ->
                score.categoryType == categoryType &&
                    if (includeApprovedOnly) {
                        score.status == ScoreStatus.APPROVED
                    } else {
                        score.status == ScoreStatus.APPROVED || score.status == ScoreStatus.PENDING
                    }
            }

        val averageScore = targetScore?.scoreValue ?: return 0

        val convertedScore = round(averageScore).toInt()

        return min(convertedScore, 5)
    }
}
