package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.score.calculator.ScoreCalculatorFactory
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.GetTotalScoreResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.CalculateTotalScoreService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CalculateTotalScoreServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : CalculateTotalScoreService {
    override fun execute(includeApprovedOnly: Boolean): GetTotalScoreResponse =
        transaction {
            val member = currentMemberProvider.getCurrentMember()
            val allScores = scoreExposedRepository.findAllByMemberId(member.id)

            val scoresByCategory = allScores.groupBy { it.categoryType }

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
                    val scores = scoresByCategory[categoryType] ?: emptyList()
                    if (scores.isEmpty()) {
                        0
                    } else {
                        val calculator = ScoreCalculatorFactory.getCalculator(categoryType)
                        calculator.calculate(scores, categoryType, includeApprovedOnly)
                    }
                }

            val totalScore = foreignLanguageScore + otherScoresSum
            GetTotalScoreResponse(totalScore = totalScore)
        }

    private fun calculateForeignLanguageScore(
        scores: List<com.team.incube.gsmc.v3.domain.score.dto.Score>,
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
        val jlptScore = if (jlptScores.isNotEmpty()) jlptCalculator.calculate(jlptScores, CategoryType.JLPT, includeApprovedOnly) else 0

        return maxOf(toeicScore, jlptScore)
    }
}
