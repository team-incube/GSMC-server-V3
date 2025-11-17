package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.ApproveScoreService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class ApproveScoreServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
) : ApproveScoreService {
    override fun execute(scoreId: Long) {
        transaction {
            if (!scoreExposedRepository.existsById(scoreId)) {
                throw GsmcException(ErrorCode.SCORE_NOT_FOUND)
            }

            scoreExposedRepository.updateStatusAndRejectionReasonByScoreId(
                scoreId = scoreId,
                status = ScoreStatus.APPROVED,
                rejectionReason = null,
            )
        }
    }
}