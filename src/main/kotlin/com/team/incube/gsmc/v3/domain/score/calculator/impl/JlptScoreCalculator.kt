package com.team.incube.gsmc.v3.domain.score.calculator.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.calculator.CategoryScoreCalculator
import com.team.incube.gsmc.v3.domain.score.dto.Score
import kotlin.math.min

class JlptScoreCalculator : CategoryScoreCalculator() {
    override fun calculate(scores: List<Score>): Int {
        val approvedScores = scores.filter { it.status == ScoreStatus.APPROVED }
        if (approvedScores.isEmpty()) return 0

        val maxJlptScore =
            approvedScores
                .filter { it.categoryType == CategoryType.JLPT }.maxOfOrNull { convertGradeToScore(it.activityName) } ?: 0

        val hasToeicAcademy =
            approvedScores.any { it.categoryType == CategoryType.TOEIC_ACADEMY }

        val bonusScore = if (hasToeicAcademy) 1 else 0

        return min(maxJlptScore + bonusScore, 10)
    }

    private fun convertGradeToScore(grade: String?): Int {
        if (grade == null) return 0

        return when (grade.uppercase().trim()) {
            "N1" -> 10
            "N2" -> 8
            "N3" -> 6
            "N4" -> 4
            "N5" -> 2
            else -> 0
        }
    }
}
