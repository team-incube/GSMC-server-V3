package com.team.incube.gsmc.v3.domain.score.calculator

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.score.dto.Score
import org.springframework.stereotype.Component

@Component
class TotalScoreCalculator {
    fun calculate(
        scores: List<Score>,
        includeApprovedOnly: Boolean,
    ): Int {
        val scoresByCategory = scores.groupBy { it.categoryType }

        val foreignLanguageCategories = CategoryType.getForeignLanguageCategories()

        val foreignLanguageScores =
            foreignLanguageCategories.flatMap { categoryType ->
                scoresByCategory[categoryType] ?: emptyList()
            }

        val foreignLanguageScore =
            if (foreignLanguageScores.isNotEmpty()) {
                calculateForeignLanguageScore(foreignLanguageScores, includeApprovedOnly)
            } else {
                0
            }

        val otherCategories = CategoryType.getAllCategories() - foreignLanguageCategories.toSet()

        val otherScoresSum =
            otherCategories.sumOf { categoryType ->
                val categoryScores = scoresByCategory[categoryType] ?: emptyList()
                if (categoryScores.isEmpty()) {
                    0
                } else {
                    val calculator = ScoreCalculatorFactory.getCalculator(categoryType)
                    calculator.calculate(categoryScores, categoryType, includeApprovedOnly)
                }
            }

        return foreignLanguageScore + otherScoresSum
    }

    private fun calculateForeignLanguageScore(
        scores: List<Score>,
        includeApprovedOnly: Boolean,
    ): Int {
        val toeicScores = scores.filter { it.categoryType == CategoryType.TOEIC || it.categoryType == CategoryType.TOEIC_ACADEMY }
        val jlptScores = scores.filter { it.categoryType == CategoryType.JLPT || it.categoryType == CategoryType.TOEIC_ACADEMY }

        val toeicCalculator = ScoreCalculatorFactory.getCalculator(CategoryType.TOEIC)
        val jlptCalculator = ScoreCalculatorFactory.getCalculator(CategoryType.JLPT)

        val toeicScore =
            if (toeicScores.isNotEmpty()) {
                toeicCalculator.calculate(
                    toeicScores,
                    CategoryType.TOEIC,
                    includeApprovedOnly,
                )
            } else {
                0
            }

        val jlptScore =
            if (jlptScores.isNotEmpty()) {
                jlptCalculator.calculate(
                    jlptScores,
                    CategoryType.JLPT,
                    includeApprovedOnly,
                )
            } else {
                0
            }

        return maxOf(toeicScore, jlptScore)
    }
}
