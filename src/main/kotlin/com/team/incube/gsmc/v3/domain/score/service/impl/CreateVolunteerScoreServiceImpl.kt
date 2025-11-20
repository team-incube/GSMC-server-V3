package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.BaseCreateOrUpdateBasedScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateVolunteerScoreService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CreateVolunteerScoreServiceImpl(
    scoreExposedRepository: ScoreExposedRepository,
    currentMemberProvider: CurrentMemberProvider,
) : BaseCreateOrUpdateBasedScoreService(scoreExposedRepository, currentMemberProvider),
    CreateVolunteerScoreService {
    override fun execute(value: String): CreateScoreResponse =
        transaction {
            val intValue =
                value.toIntOrNull()
                    ?: throw GsmcException(ErrorCode.SCORE_INVALID_VALUE)

            if (intValue < 1) {
                throw GsmcException(ErrorCode.SCORE_VALUE_OUT_OF_RANGE)
            }

            createOrUpdateScore(
                categoryType = CategoryType.VOLUNTEER,
                scoreValue = intValue.toDouble(),
                sourceId = null,
            )
        }
}
