package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.BaseCreateOrUpdateBasedScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateNcsScoreService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CreateNcsScoreServiceImpl(
    scoreExposedRepository: ScoreExposedRepository,
    private val fileExposedRepository: FileExposedRepository,
    currentMemberProvider: CurrentMemberProvider,
) : BaseCreateOrUpdateBasedScoreService(scoreExposedRepository, currentMemberProvider),
    CreateNcsScoreService {
    override fun execute(
        value: String,
        fileId: Long,
    ): CreateScoreResponse =
        transaction {
            if (!fileExposedRepository.existsById(fileId)) {
                throw GsmcException(ErrorCode.FILE_NOT_FOUND)
            }

            val doubleValue =
                value.toDoubleOrNull()
                    ?: throw GsmcException(ErrorCode.SCORE_INVALID_VALUE)

            if (doubleValue !in 1.0..5.0) {
                throw GsmcException(ErrorCode.SCORE_VALUE_OUT_OF_RANGE)
            }

            createOrUpdateScore(
                categoryType = CategoryType.NCS,
                scoreValue = doubleValue,
                sourceId = fileId,
            )
        }
}
