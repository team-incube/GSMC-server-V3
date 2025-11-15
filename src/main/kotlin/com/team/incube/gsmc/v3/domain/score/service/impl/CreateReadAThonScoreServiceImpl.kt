package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.CreateReadAThonScoreService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CreateReadAThonScoreServiceImpl(
    scoreExposedRepository: ScoreExposedRepository,
    private val fileExposedRepository: FileExposedRepository,
    currentMemberProvider: CurrentMemberProvider,
) : BaseScoreService(scoreExposedRepository, currentMemberProvider),
    CreateReadAThonScoreService {
    override fun execute(
        grade: Int,
        fileId: Long,
    ): CreateScoreResponse =
        transaction {
            if (fileExposedRepository.existsById(fileId).not()) {
                throw GsmcException(ErrorCode.FILE_NOT_FOUND)
            }

            createOrUpdateScore(
                categoryType = CategoryType.READ_A_THON,
                scoreValue = grade.toDouble(),
                sourceId = fileId,
                activityName = null,
            )
        }
}
