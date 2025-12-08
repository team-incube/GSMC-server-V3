package com.team.incube.gsmc.v3.domain.score.service

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.CategoryNames
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider

abstract class BaseCreateOrUpdateBasedScoreService(
    protected val scoreExposedRepository: ScoreExposedRepository,
    protected val currentMemberProvider: CurrentMemberProvider,
) {
    protected fun createOrUpdateScore(
        categoryType: CategoryType,
        scoreValue: Double?,
        sourceId: Long?,
        activityName: String? = null,
    ): CreateScoreResponse {
        val member = currentMemberProvider.getCurrentMember()

        val existingScore =
            scoreExposedRepository.findByMemberIdAndCategoryType(
                memberId = member.id,
                categoryType = categoryType,
            )

        val savedScore =
            existingScore?.let {
                scoreExposedRepository.update(
                    it.copy(
                        status = ScoreStatus.PENDING,
                        sourceId = sourceId,
                        scoreValue = scoreValue,
                        activityName = activityName,
                    ),
                )
            } ?: scoreExposedRepository.save(
                Score(
                    id = null,
                    member = member,
                    categoryType = categoryType,
                    status = ScoreStatus.PENDING,
                    sourceId = sourceId,
                    activityName = activityName,
                    scoreValue = scoreValue,
                    rejectionReason = null,
                ),
            )

        return CreateScoreResponse(
            scoreId = savedScore.id!!,
            categoryNames =
                CategoryNames(
                    koreanName = savedScore.categoryType.koreanName,
                    englishName = savedScore.categoryType.englishName,
                ),
            scoreStatus = savedScore.status,
            activityName = savedScore.activityName,
        )
    }
}
