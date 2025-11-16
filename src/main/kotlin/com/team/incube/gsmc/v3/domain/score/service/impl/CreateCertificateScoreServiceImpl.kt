package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.BaseCountBasedScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateCertificateScoreService
import com.team.incube.gsmc.v3.domain.score.validator.ScoreLimitValidator
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CreateCertificateScoreServiceImpl(
    scoreExposedRepository: ScoreExposedRepository,
    private val fileExposedRepository: FileExposedRepository,
    currentMemberProvider: CurrentMemberProvider,
    private val scoreLimitValidator: ScoreLimitValidator,
) : BaseCountBasedScoreService(scoreExposedRepository, currentMemberProvider),
    CreateCertificateScoreService {
    override fun execute(
        certificateName: String,
        fileId: Long,
    ): CreateScoreResponse =
        transaction {
            val member = currentMemberProvider.getCurrentUser()
            if (!fileExposedRepository.existsById(fileId)) {
                throw GsmcException(ErrorCode.FILE_NOT_FOUND)
            }
            scoreLimitValidator.validateScoreLimit(member.id, CategoryType.CERTIFICATE)

            createScore(
                member = member,
                categoryType = CategoryType.CERTIFICATE,
                activityName = certificateName,
                sourceId = fileId,
            )
        }
}
