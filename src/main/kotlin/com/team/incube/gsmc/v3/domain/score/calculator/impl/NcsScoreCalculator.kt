package com.team.incube.gsmc.v3.domain.score.calculator.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.score.calculator.CategoryScoreCalculator
import com.team.incube.gsmc.v3.domain.score.dto.Score
import kotlin.math.roundToInt

/**
 * 직업기초능력평가(NCS) 점수 계산기
 *
 * 등급은 Score.scoreValue에 저장됩니다 (1~5등급, 1등급이 가장 높음).
 * 점수 변환: 6 - round(등급), 최대 5점
 * 예시:
 * - 1.2등급 → 5점
 * - 2.3등급 → 4점
 * - 3.5등급 → 3점
 * - 4.6등급 → 1점
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
                score.categoryType == categoryType && score.isValidStatus(includeApprovedOnly)
            }

        val grade = targetScore?.scoreValue ?: return 0

        val convertedScore = 6 - grade.roundToInt()

        return convertedScore.coerceIn(0, 5)
    }
}
