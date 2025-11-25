package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.UpdateExternalActivityScoreService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class UpdateExternalActivityScoreServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
    private val fileExposedRepository: FileExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : UpdateExternalActivityScoreService {
    override fun execute(
        scoreId: Long,
        value: String,
        fileId: Long,
    ) {
        transaction {
            val currentMember = currentMemberProvider.getCurrentMember()

            val score =
                scoreExposedRepository.findById(scoreId)
                    ?: throw GsmcException(ErrorCode.SCORE_NOT_FOUND)

            if (score.member.id != currentMember.id) {
                throw GsmcException(ErrorCode.SCORE_NOT_OWNED)
            }

            if (score.categoryType != CategoryType.EXTERNAL_ACTIVITY) {
                throw GsmcException(ErrorCode.SCORE_INVALID_CATEGORY)
            }

            if (!fileExposedRepository.existsById(fileId)) {
                throw GsmcException(ErrorCode.FILE_NOT_FOUND)
            }

            scoreExposedRepository.update(
                score.copy(
                    activityName = value,
                    sourceId = fileId,
                    status = ScoreStatus.PENDING,
                ),
            )
        }
    }
}
