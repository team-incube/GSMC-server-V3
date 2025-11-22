package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.score.calculator.ScoreCalculatorFactory
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.CategoryNames
import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.CategoryScoreGroup
import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.ScoreItem
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.GetScoresByCategoryResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.FindScoresByCategoryService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindScoresByCategoryServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : FindScoresByCategoryService {
    override fun execute(status: ScoreStatus?): GetScoresByCategoryResponse =
        transaction {
            val member = currentMemberProvider.getCurrentMember()

            val scores =
                scoreExposedRepository.findByMemberIdAndCategoryTypeAndStatus(
                    memberId = member.id,
                    categoryType = null,
                    status = status,
                )

            val groupedByCategory = groupScoresByCategory(scores)
            val foreignLanguageCategories = CategoryType.getForeignLanguageCategories()
            val foreignRepresentative = groupedByCategory.keys.find { it in foreignLanguageCategories } ?: CategoryType.TOEIC

            val categoryGroups =
                CategoryType.entries
                    .filter { !it.isForeignLanguage || it == foreignRepresentative }
                    .map { categoryType ->
                        val categoryScores = groupedByCategory[categoryType] ?: emptyList()
                        val recognizedScore = if (categoryScores.isNotEmpty()) calculateRecognizedScore(categoryScores, categoryType) else 0
                        val isForeignLanguage = categoryType.isForeignLanguage

                        CategoryScoreGroup(
                            categoryType = categoryType,
                            categoryNames =
                                CategoryNames(
                                    koreanName = if (isForeignLanguage) "공인 점수" else categoryType.koreanName,
                                    englishName = if (isForeignLanguage) "Foreign Language" else categoryType.englishName,
                                ),
                            recognizedScore = recognizedScore,
                            scores =
                                categoryScores.map { score ->
                                    ScoreItem(
                                        scoreId = score.id!!,
                                        categoryNames =
                                            CategoryNames(
                                                koreanName = score.categoryType.koreanName,
                                                englishName = score.categoryType.englishName,
                                            ),
                                        scoreStatus = score.status,
                                        activityName = score.activityName,
                                        scoreValue = score.scoreValue,
                                        rejectionReason = score.rejectionReason,
                                    )
                                },
                        )
                    }

            GetScoresByCategoryResponse(categories = categoryGroups)
        }

    private fun groupScoresByCategory(scores: List<Score>): Map<CategoryType, List<Score>> {
        val foreignLanguageCategories = CategoryType.getForeignLanguageCategories()
        val (foreignLanguageScores, otherScores) = scores.partition { it.categoryType in foreignLanguageCategories }

        val grouped = mutableMapOf<CategoryType, List<Score>>()

        if (foreignLanguageScores.isNotEmpty()) {
            val representativeCategory =
                when {
                    foreignLanguageScores.any { it.categoryType == CategoryType.TOEIC } -> CategoryType.TOEIC
                    foreignLanguageScores.any { it.categoryType == CategoryType.JLPT } -> CategoryType.JLPT
                    else -> CategoryType.TOEIC
                }
            grouped[representativeCategory] = foreignLanguageScores
        }

        grouped.putAll(otherScores.groupBy { it.categoryType })

        return grouped
    }

    private fun calculateRecognizedScore(
        scores: List<Score>,
        representativeCategoryType: CategoryType,
    ): Int {
        val calculator = ScoreCalculatorFactory.getCalculator(representativeCategoryType)
        return calculator.calculate(
            scores = scores,
            categoryType = representativeCategoryType,
            includeApprovedOnly = false,
        )
    }
}
