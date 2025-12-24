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
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
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

            val scoresByCategory = scores.groupBy { it.categoryType }

            val categoryGroups =
                CategoryType.entries.map { categoryType ->
                    val categoryScores = scoresByCategory[categoryType] ?: emptyList()
                    val recognizedScore = if (categoryScores.isNotEmpty()) calculateRecognizedScore(categoryScores, categoryType) else 0

                    CategoryScoreGroup(
                        categoryType = categoryType,
                        categoryNames =
                            CategoryNames(
                                koreanName = categoryType.koreanName,
                                englishName = categoryType.englishName,
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
                                    updatedAt = score.updatedAt,
                                )
                            },
                    )
                }

            GetScoresByCategoryResponse(categories = categoryGroups)
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
