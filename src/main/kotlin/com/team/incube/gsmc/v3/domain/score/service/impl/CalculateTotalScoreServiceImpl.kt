package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.score.calculator.ScoreCalculatorFactory
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.CalculateTotalScoreService
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CalculateTotalScoreServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
) : CalculateTotalScoreService {
    override fun execute(
        memberId: Long,
        includeApprovedOnly: Boolean,
    ): Int =
        transaction {
            val allScores = scoreExposedRepository.findAllByMemberId(memberId)

            val scoresByCategory = allScores.groupBy { it.categoryType }

            val foreignLanguageCategories = listOf(CategoryType.TOEIC, CategoryType.JLPT, CategoryType.TOEIC_ACADEMY)

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
                    val scores = scoresByCategory[categoryType] ?: emptyList()
                    if (scores.isEmpty()) {
                        0
                    } else {
                        val calculator = ScoreCalculatorFactory.getCalculator(categoryType)
                        calculator.calculate(scores, includeApprovedOnly)
                    }
                }

            foreignLanguageScore + otherScoresSum
        }

    private fun calculateForeignLanguageScore(
        scores: List<com.team.incube.gsmc.v3.domain.score.dto.Score>,
        includeApprovedOnly: Boolean,
    ): Int {
        val toeicScores = scores.filter { it.categoryType == CategoryType.TOEIC }
        val jlptScores = scores.filter { it.categoryType == CategoryType.JLPT }

        val toeicCalculator = ScoreCalculatorFactory.getCalculator(CategoryType.TOEIC)
        val jlptCalculator = ScoreCalculatorFactory.getCalculator(CategoryType.JLPT)

        val toeicScore = if (toeicScores.isNotEmpty()) toeicCalculator.calculate(scores, includeApprovedOnly) else 0
        val jlptScore = if (jlptScores.isNotEmpty()) jlptCalculator.calculate(scores, includeApprovedOnly) else 0

        return maxOf(toeicScore, jlptScore)
    }
}