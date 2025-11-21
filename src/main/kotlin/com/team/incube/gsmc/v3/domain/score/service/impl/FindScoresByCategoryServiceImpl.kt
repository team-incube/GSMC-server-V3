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

            val categoryGroups =
                groupedByCategory.map { (categoryType, categoryScores) ->
                    val recognizedScore = calculateRecognizedScore(categoryScores, categoryType)
                    val isForeignLanguage = CategoryType.getForeignLanguageCategories().contains(categoryType)

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
        val foreignLanguageCategories = CategoryType.getForeignLanguageCategories().map { it }
        val foreignLanguageScores = scores.filter { it.categoryType in foreignLanguageCategories }
        val otherScores = scores.filter { it.categoryType !in foreignLanguageCategories }

        val grouped = mutableMapOf<CategoryType, List<Score>>()

        if (foreignLanguageScores.isNotEmpty()) {
            val representativeCategory =
                foreignLanguageScores
                    .firstOrNull { it.categoryType == CategoryType.TOEIC }
                    ?.categoryType
                    ?: foreignLanguageScores
                        .firstOrNull { it.categoryType == CategoryType.JLPT }
                        ?.categoryType
                    ?: CategoryType.TOEIC

            grouped[representativeCategory] = foreignLanguageScores
        }

        otherScores.groupBy { it.categoryType }.forEach { (categoryType, categoryScores) ->
            grouped[categoryType] = categoryScores
        }

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
