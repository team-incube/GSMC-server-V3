package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.score.calculator.ScoreCalculatorFactory
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.CategoryNames
import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.CategoryScoreGroup
import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.ScoreItem
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.GetScoresByCategoryResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.FindScoresByCategoryByMemberIdService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindScoresByCategoryByMemberIdServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
    private val memberExposedRepository: MemberExposedRepository,
) : FindScoresByCategoryByMemberIdService {
    override fun execute(
        memberId: Long,
        status: ScoreStatus?,
    ): GetScoresByCategoryResponse =
        transaction {
            if (!memberExposedRepository.existsById(memberId)) {
                throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)
            }

            val scores =
                scoreExposedRepository
                    .findByMemberIdAndCategoryTypeAndStatus(
                        memberId = memberId,
                        categoryType = null,
                        status = status,
                    ).filter { it.status != ScoreStatus.INCOMPLETE }

            val foreignLanguageCategories = CategoryType.getForeignLanguageCategories()
            val groupingResult = groupScoresByCategory(scores, foreignLanguageCategories)

            val categoryGroups =
                CategoryType.entries
                    .filter { !it.isForeignLanguage || it == groupingResult.foreignRepresentative }
                    .map { categoryType ->
                        val categoryScores = groupingResult.groupedScores[categoryType] ?: emptyList()
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

    private data class GroupingResult(
        val groupedScores: Map<CategoryType, List<Score>>,
        val foreignRepresentative: CategoryType,
    )

    private fun groupScoresByCategory(
        scores: List<Score>,
        foreignLanguageCategories: List<CategoryType>,
    ): GroupingResult {
        val (foreignLanguageScores, otherScores) = scores.partition { it.categoryType in foreignLanguageCategories }

        val grouped = mutableMapOf<CategoryType, List<Score>>()

        val representativeCategory =
            if (foreignLanguageScores.isNotEmpty()) {
                val categoriesInScore = foreignLanguageScores.map { it.categoryType }.toSet()
                when {
                    CategoryType.TOEIC in categoriesInScore -> CategoryType.TOEIC
                    CategoryType.JLPT in categoriesInScore -> CategoryType.JLPT
                    else -> CategoryType.TOEIC
                }.also { grouped[it] = foreignLanguageScores }
            } else {
                CategoryType.TOEIC
            }

        grouped.putAll(otherScores.groupBy { it.categoryType })

        return GroupingResult(
            groupedScores = grouped,
            foreignRepresentative = representativeCategory,
        )
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
