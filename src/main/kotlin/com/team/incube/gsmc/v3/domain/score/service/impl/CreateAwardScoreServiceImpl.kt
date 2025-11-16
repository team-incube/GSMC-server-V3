package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.CategoryNames
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.CreateAwardScoreService
import com.team.incube.gsmc.v3.domain.score.validator.ScoreLimitValidator
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CreateAwardScoreServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
    private val fileExposedRepository: FileExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
    private val scoreLimitValidator: ScoreLimitValidator,
) : CreateAwardScoreService {
    override fun execute(
        awardName: String,
        fileId: Long,
    ): CreateScoreResponse =
        transaction {
            val member = currentMemberProvider.getCurrentUser()
            if (!fileExposedRepository.existsById(fileId)) {
                throw GsmcException(ErrorCode.FILE_NOT_FOUND)
            }
            scoreLimitValidator.validateScoreLimit(member.id, CategoryType.AWARD)
            val savedScore =
                scoreExposedRepository.save(
                    Score(
                        id = null,
                        member = member,
                        categoryType = CategoryType.AWARD,
                        status = ScoreStatus.PENDING,
                        sourceId = fileId,
                        activityName = awardName,
                        scoreValue = null,
                    ),
                )
            CreateScoreResponse(
                scoreId = savedScore.id!!,
                categoryNames =
                    CategoryNames(
                        koreanName = savedScore.categoryType.koreanName,
                        englishName = savedScore.categoryType.englishName,
                    ),
                scoreStatus = savedScore.status,
                sourceId = savedScore.sourceId,
                activityName = savedScore.activityName,
            )
        }
}
