package com.team.incube.gsmc.v3.domain.score.calculator.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.calculator.CategoryScoreCalculator
import com.team.incube.gsmc.v3.domain.score.dto.Score
import kotlin.math.min
import kotlin.math.round

/**
 * TOEIC 점수 계산기
 *
 * TOEIC 점수는 Score.scoreValue에 저장됩니다 (10-990).
 * 점수 변환: round(scoreValue / 100), 최대 10점
 * 토익사관학교 참여 시 +1점 보너스 (최대 10점)
 *
 * 호출 시 TOEIC과 TOEIC_ACADEMY 레코드가 함께 전달됩니다.
 */
class ToeicScoreCalculator : CategoryScoreCalculator() {
    override fun calculate(
        scores: List<Score>,
        categoryType: CategoryType,
        includeApprovedOnly: Boolean,
    ): Int {
        val targetScores =
            scores.filter { score ->
                if (includeApprovedOnly) {
                    score.status == ScoreStatus.APPROVED
                } else {
                    score.status == ScoreStatus.APPROVED || score.status == ScoreStatus.PENDING
                }
            }

        if (targetScores.isEmpty()) return 0

        // TOEIC 점수만 추출 (TOEIC_ACADEMY는 scoreValue가 없음)
        val maxToeicScore =
            targetScores
                .filter { it.categoryType == CategoryType.TOEIC }
                .mapNotNull { it.scoreValue }
                .maxOrNull() ?: 0.0

        val convertedScore = round(maxToeicScore / 100.0).toInt()

        // TOEIC_ACADEMY 보너스 체크
        val hasToeicAcademy =
            targetScores.any { it.categoryType == CategoryType.TOEIC_ACADEMY }

        val bonusScore = if (hasToeicAcademy) 1 else 0

        return min(convertedScore + bonusScore, 10)
    }
}
