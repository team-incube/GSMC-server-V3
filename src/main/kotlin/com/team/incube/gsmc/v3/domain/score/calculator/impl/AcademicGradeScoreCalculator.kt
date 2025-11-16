package com.team.incube.gsmc.v3.domain.score.calculator.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.score.calculator.CategoryScoreCalculator
import com.team.incube.gsmc.v3.domain.score.dto.Score
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 교과성적 점수 계산기
 *
 * 평균등급은 Score.scoreValue에 저장됩니다 (실수).
 * 학년에 따라 계산 방식이 다릅니다:
 * - 1학년: 1~5등급 → 9, 8, 7, 6, 5점
 * - 2~3학년: 1~9등급 → 9, 8, 7, 6, 5, 4, 3, 2, 1점 (10 - 반올림등급)
 *
 * maxRecordCount=1이므로 레코드는 1개만 존재합니다.
 */
class AcademicGradeScoreCalculator : CategoryScoreCalculator() {
    override fun calculate(
        scores: List<Score>,
        categoryType: CategoryType,
        includeApprovedOnly: Boolean,
    ): Int {
        val targetScore =
            scores.firstOrNull { score ->
                score.categoryType == categoryType && score.isValidStatus(includeApprovedOnly)
            } ?: return 0

        val averageGrade = targetScore.scoreValue ?: return 0
        val memberGrade = targetScore.member.grade ?: return 0

        return if (memberGrade == 1) {
            // 1학년: 1~5등급
            calculateFirstGradeScore(averageGrade)
        } else {
            // 2~3학년: 1~9등급
            calculateSecondThirdGradeScore(averageGrade)
        }
    }

    private fun calculateFirstGradeScore(averageGrade: Double): Int {
        val roundedGrade = averageGrade.roundToInt()
        return when (roundedGrade) {
            1 -> 9
            2 -> 8
            3 -> 7
            4 -> 6
            5 -> 5
            else -> 0
        }
    }

    private fun calculateSecondThirdGradeScore(averageGrade: Double): Int {
        val roundedGrade = averageGrade.roundToInt()
        val score = 10 - roundedGrade
        return min(max(score, 1), 9)
    }
}
