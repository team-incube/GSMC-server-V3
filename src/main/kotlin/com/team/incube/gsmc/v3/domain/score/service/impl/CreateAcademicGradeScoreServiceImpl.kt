package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.BaseCreateOrUpdateBasedScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateAcademicGradeScoreService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CreateAcademicGradeScoreServiceImpl(
    scoreExposedRepository: ScoreExposedRepository,
    currentMemberProvider: CurrentMemberProvider,
) : BaseCreateOrUpdateBasedScoreService(scoreExposedRepository, currentMemberProvider),
    CreateAcademicGradeScoreService {
    override fun execute(value: String): CreateScoreResponse =
        transaction {
            val doubleValue =
                value.toDoubleOrNull()
                    ?: throw GsmcException(ErrorCode.SCORE_INVALID_VALUE)

            if (doubleValue !in 1.0..9.0) {
                throw GsmcException(ErrorCode.SCORE_VALUE_OUT_OF_RANGE)
            }

            createOrUpdateScore(
                categoryType = CategoryType.ACADEMIC_GRADE,
                scoreValue = doubleValue,
                sourceId = null,
            )
        }
}
